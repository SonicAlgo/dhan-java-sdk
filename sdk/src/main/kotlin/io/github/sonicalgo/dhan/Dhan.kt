package io.github.sonicalgo.dhan

import io.github.sonicalgo.core.client.HttpClientProvider
import io.github.sonicalgo.dhan.common.*
import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.AuthApiClient
import io.github.sonicalgo.dhan.config.DhanConfig
import io.github.sonicalgo.dhan.config.DhanConstants
import io.github.sonicalgo.dhan.config.DhanHeaderProvider
import io.github.sonicalgo.dhan.config.DhanWebSocketConfig
import io.github.sonicalgo.dhan.usecase.*
import io.github.sonicalgo.dhan.websocket.marketFeed.MarketFeedClient
import io.github.sonicalgo.dhan.websocket.order.OrderStreamClient
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Main entry point for the Dhan SDK.
 *
 * Provides access to all Dhan trading APIs including orders, portfolio,
 * market data, and WebSocket clients for real-time updates.
 *
 * ## Getting Started
 *
 * Create an instance using the builder:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("your-client-id")
 *     .accessToken("your-access-token")
 *     .loggingEnabled(true)
 *     .build()
 *
 * // Get user profile
 * val profile = dhan.getProfile()
 * println("Client: ${profile.dhanClientId}")
 * ```
 *
 * ## Placing Orders
 *
 * ```kotlin
 * // dhanClientId is auto-injected from config
 * val response = dhan.placeOrder(PlaceOrderParams {
 *     transactionType = TransactionType.BUY
 *     exchangeSegment = ExchangeSegment.NSE_EQ
 *     productType = ProductType.CNC
 *     orderType = OrderType.LIMIT
 *     validity = Validity.DAY
 *     securityId = "1333"
 *     quantity = 10
 *     price = 1428.0
 * })
 * println("Order ID: ${response.orderId}")
 * ```
 *
 * ## Real-Time Market Data
 *
 * ```kotlin
 * val feedClient = dhan.createMarketFeedClient()
 * feedClient.addListener(object : MarketFeedListener {
 *     override fun onTickerData(data: TickerData) {
 *         println("${data.securityId}: ${data.ltp}")
 *     }
 * })
 *
 * feedClient.connect()
 * feedClient.subscribe(
 *     listOf(Instrument(ExchangeSegment.NSE_EQ, "1333")),
 *     FeedMode.TICKER
 * )
 * ```
 *
 * ## Real-Time Order Updates
 *
 * ```kotlin
 * val orderClient = dhan.createOrderStreamClient()
 * orderClient.addListener(object : OrderStreamListener {
 *     override fun onOrderUpdate(update: OrderUpdate) {
 *         println("Order ${update.orderId}: ${update.orderStatus}")
 *     }
 * })
 *
 * orderClient.connect()
 * ```
 *
 * ## Configuration via Builder
 *
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("your-client-id")
 *     .accessToken("your-access-token")    // Optional at build, can set later
 *     .loggingEnabled(true)                // Enable HTTP logging
 *     .rateLimitRetries(3)                 // Auto-retry on rate limit
 *     .build()
 *
 * // Access token can be updated later (e.g., for token refresh)
 * dhan.setAccessToken("new-token")
 * ```
 *
 * ## Multiple Instances
 *
 * Each `build()` call creates an independent instance with its own
 * HTTP client and configuration:
 *
 * ```kotlin
 * val client1 = Dhan.builder().clientId("client1").accessToken("token1").build()
 * val client2 = Dhan.builder().clientId("client2").accessToken("token2").build()
 * ```
 *
 * ## Thread Safety
 *
 * All API methods are thread-safe and can be called from any thread.
 * WebSocket callbacks are invoked on background threads.
 *
 * ## Resource Management
 *
 * The SDK implements [Closeable] for easy resource cleanup. Call [close]
 * to release all resources including HTTP clients and WebSocket connections:
 *
 * ```kotlin
 * // Manual close
 * val dhan = Dhan.builder().clientId("id").accessToken("token").build()
 * val feed = dhan.createMarketFeedClient()
 * // ... use SDK ...
 * dhan.close() // Closes all WebSocket clients and HTTP resources
 *
 * // Or use try-with-resources (Kotlin)
 * Dhan.builder().clientId("id").accessToken("token").build().use { dhan ->
 *     val feed = dhan.createMarketFeedClient()
 *     // ... use SDK ...
 * } // Auto-closes when block exits
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/">DhanHQ API Documentation</a>
 */
class Dhan private constructor(internal val config: DhanConfig) : Closeable {

    // Per-instance infrastructure
    private val headerProvider = DhanHeaderProvider(config)
    private val clientProvider = HttpClientProvider(config, headerProvider, DhanConstants.SHUTDOWN_TIMEOUT_SECONDS)
    private val apiClient = ApiClient(config, clientProvider)

    // Auth API client for consent-based authentication (uses per-call credentials)
    private val authApiClient = AuthApiClient(config)

    // Track WebSocket clients for unified lifecycle management
    private val webSocketClients = CopyOnWriteArrayList<Closeable>()

    // ==================== Mutable State ====================

    /**
     * Sets the OAuth access token for API authentication.
     *
     * Can be called after initialization to update the token (e.g., for token refresh).
     *
     * @param token Access token from DhanHQ
     * @return This instance for chaining
     * @throws IllegalArgumentException if token is blank
     */
    fun setAccessToken(token: String): Dhan {
        require(token.isNotBlank()) { "Access token cannot be blank" }
        config.accessToken = token
        return this
    }

    /**
     * Gets the current access token.
     *
     * @return Current access token or empty string if not set
     */
    fun getAccessToken(): String = config.accessToken

    /**
     * Gets the client ID.
     *
     * @return Client ID configured at build time
     */
    fun getClientId(): String = config.clientId

    /**
     * Checks if HTTP logging is enabled.
     *
     * @return true if logging is enabled
     */
    fun isLoggingEnabled(): Boolean = config.loggingEnabled

    /**
     * Gets the rate limit retry count.
     *
     * @return Number of retries configured
     */
    fun getRateLimitRetries(): Int = config.rateLimitRetries

    // ==================== Orders ====================

    /**
     * Places a new order.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = PlaceOrderParams {
     *     transactionType = TransactionType.BUY
     *     exchangeSegment = ExchangeSegment.NSE_EQ
     *     productType = ProductType.CNC
     *     orderType = OrderType.LIMIT
     *     validity = Validity.DAY
     *     securityId = "1333"
     *     quantity = 10
     *     price = 1428.0
     * }
     * val result = dhan.placeOrder(params)
     * println("Order ID: ${result.orderId}")
     * ```
     *
     * ## Java Example
     * ```java
     * PlaceOrderParams params = PlaceOrderParams.builder()
     *     .transactionType(TransactionType.BUY)
     *     .exchangeSegment(ExchangeSegment.NSE_EQ)
     *     .productType(ProductType.CNC)
     *     .orderType(OrderType.LIMIT)
     *     .validity(Validity.DAY)
     *     .securityId("1333")
     *     .quantity(10)
     *     .price(1428.0)
     *     .build();
     * OrderAction result = dhan.placeOrder(params);
     * ```
     *
     * @param params Order placement parameters
     * @return [OrderAction] with order ID and status
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun placeOrder(params: PlaceOrderParams): OrderAction =
        executePlaceOrder(apiClient, config, params)

    /**
     * Places a slicing order that splits large orders into smaller parts.
     *
     * Useful for F&O orders exceeding freeze quantity limits.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = SlicingOrderParams {
     *     transactionType = TransactionType.BUY
     *     exchangeSegment = ExchangeSegment.NSE_FNO
     *     productType = ProductType.INTRADAY
     *     orderType = OrderType.MARKET
     *     validity = Validity.DAY
     *     securityId = "52175"
     *     quantity = 5000
     * }
     * val results = dhan.placeSlicingOrder(params)
     * ```
     *
     * ## Java Example
     * ```java
     * SlicingOrderParams params = SlicingOrderParams.builder()
     *     .transactionType(TransactionType.BUY)
     *     .exchangeSegment(ExchangeSegment.NSE_FNO)
     *     .productType(ProductType.INTRADAY)
     *     .orderType(OrderType.MARKET)
     *     .validity(Validity.DAY)
     *     .securityId("52175")
     *     .quantity(5000)
     *     .build();
     * List<OrderAction> results = dhan.placeSlicingOrder(params);
     * ```
     *
     * @param params Slicing order placement parameters
     * @return List of [OrderAction] for each slice
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun placeSlicingOrder(params: SlicingOrderParams): List<OrderAction> =
        executePlaceSlicingOrder(apiClient, config, params)

    /**
     * Modifies an existing pending order.
     *
     * Only orders in PENDING or PART_TRADED status can be modified.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = ModifyOrderParams {
     *     orderId = "112111182198"
     *     orderType = OrderType.LIMIT
     *     validity = Validity.DAY
     *     quantity = 15
     *     price = 1450.0
     * }
     * val result = dhan.modifyOrder("112111182198", params)
     * ```
     *
     * ## Java Example
     * ```java
     * ModifyOrderParams params = ModifyOrderParams.builder()
     *     .orderId("112111182198")
     *     .orderType(OrderType.LIMIT)
     *     .validity(Validity.DAY)
     *     .quantity(15)
     *     .price(1450.0)
     *     .build();
     * OrderAction result = dhan.modifyOrder("112111182198", params);
     * ```
     *
     * @param orderId Order ID to modify
     * @param params Modification parameters
     * @return [OrderAction] with modified order status
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun modifyOrder(orderId: String, params: ModifyOrderParams): OrderAction =
        executeModifyOrder(apiClient, config, orderId, params)

    /**
     * Cancels a pending order.
     *
     * Only orders in PENDING status can be cancelled.
     *
     * ## Kotlin Example
     * ```kotlin
     * val result = dhan.cancelOrder("112111182198")
     * println("Status: ${result.orderStatus}")
     * ```
     *
     * ## Java Example
     * ```java
     * OrderAction result = dhan.cancelOrder("112111182198");
     * System.out.println("Status: " + result.getOrderStatus());
     * ```
     *
     * @param orderId Order ID to cancel
     * @return [OrderAction] with cancelled order status
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun cancelOrder(orderId: String): OrderAction =
        executeCancelOrder(apiClient, orderId)

    /**
     * Gets all orders for the day (order book).
     *
     * ## Kotlin Example
     * ```kotlin
     * val orders = dhan.getOrders()
     * orders.forEach { println("${it.orderId}: ${it.tradingSymbol}") }
     * ```
     *
     * ## Java Example
     * ```java
     * List<OrderBookItem> orders = dhan.getOrders();
     * for (OrderBookItem order : orders) {
     *     System.out.println(order.getOrderId() + ": " + order.getTradingSymbol());
     * }
     * ```
     *
     * @return List of [OrderBookItem] for the day
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun getOrders(): List<OrderBookItem> =
        executeGetOrders(apiClient)

    /**
     * Gets order details by order ID.
     *
     * ## Kotlin Example
     * ```kotlin
     * val order = dhan.getOrderById("112111182198")
     * println("${order.tradingSymbol}: ${order.orderStatus}")
     * ```
     *
     * ## Java Example
     * ```java
     * OrderBookItem order = dhan.getOrderById("112111182198");
     * System.out.println(order.getTradingSymbol() + ": " + order.getOrderStatus());
     * ```
     *
     * @param orderId Order ID to retrieve
     * @return [OrderBookItem] with order details
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun getOrderById(orderId: String): OrderBookItem =
        executeGetOrderById(apiClient, orderId)

    /**
     * Gets order details by correlation ID.
     *
     * Use this to track orders using your own reference ID.
     *
     * ## Kotlin Example
     * ```kotlin
     * val order = dhan.getOrderByCorrelationId("my-order-ref-123")
     * println("Order ID: ${order.orderId}")
     * ```
     *
     * ## Java Example
     * ```java
     * OrderBookItem order = dhan.getOrderByCorrelationId("my-order-ref-123");
     * System.out.println("Order ID: " + order.getOrderId());
     * ```
     *
     * @param correlationId Correlation ID to search
     * @return [OrderBookItem] matching the correlation ID
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun getOrderByCorrelationId(correlationId: String): OrderBookItem =
        executeGetOrderByCorrelationId(apiClient, correlationId)

    // ==================== Trades ====================

    /**
     * Gets all trades for the day (trade book).
     *
     * ## Kotlin Example
     * ```kotlin
     * val trades = dhan.getTrades()
     * trades.forEach { println("${it.tradingSymbol}: ${it.tradedQuantity} @ ${it.tradedPrice}") }
     * ```
     *
     * ## Java Example
     * ```java
     * List<TradeBookItem> trades = dhan.getTrades();
     * for (TradeBookItem trade : trades) {
     *     System.out.println(trade.getTradingSymbol() + ": " + trade.getTradedQuantity());
     * }
     * ```
     *
     * @return List of [TradeBookItem] for the day
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun getTrades(): List<TradeBookItem> =
        executeGetTrades(apiClient)

    /**
     * Gets trades for a specific order.
     *
     * ## Kotlin Example
     * ```kotlin
     * val trades = dhan.getTradesByOrderId("112111182198")
     * trades.forEach { println("${it.tradedQuantity} @ ${it.tradedPrice}") }
     * ```
     *
     * ## Java Example
     * ```java
     * List<TradeBookItem> trades = dhan.getTradesByOrderId("112111182198");
     * for (TradeBookItem trade : trades) {
     *     System.out.println(trade.getTradedQuantity() + " @ " + trade.getTradedPrice());
     * }
     * ```
     *
     * @param orderId Order ID to get trades for
     * @return List of [TradeBookItem] for the order
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun getTradesByOrderId(orderId: String): List<TradeBookItem> =
        executeGetTradesByOrderId(apiClient, orderId)

    // ==================== Super Orders ====================

    /**
     * Places a Super Order (entry + target + stop loss + trailing).
     *
     * Super Orders combine entry, target, and stop loss legs with optional trailing stop loss.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = PlaceSuperOrderParams {
     *     transactionType = TransactionType.BUY
     *     exchangeSegment = ExchangeSegment.NSE_EQ
     *     productType = ProductType.INTRADAY
     *     orderType = OrderType.LIMIT
     *     securityId = "1333"
     *     quantity = 10
     *     price = 1428.0
     *     targetPrice = 1450.0
     *     stopLossPrice = 1410.0
     *     trailingJump = 5.0
     * }
     * val result = dhan.placeSuperOrder(params)
     * println("Order ID: ${result.orderId}")
     * ```
     *
     * ## Java Example
     * ```java
     * PlaceSuperOrderParams params = PlaceSuperOrderParams.builder()
     *     .transactionType(TransactionType.BUY)
     *     .exchangeSegment(ExchangeSegment.NSE_EQ)
     *     .productType(ProductType.INTRADAY)
     *     .orderType(OrderType.LIMIT)
     *     .securityId("1333")
     *     .quantity(10)
     *     .price(1428.0)
     *     .targetPrice(1450.0)
     *     .stopLossPrice(1410.0)
     *     .trailingJump(5.0)
     *     .build();
     * OrderAction result = dhan.placeSuperOrder(params);
     * ```
     *
     * @param params Super Order placement parameters
     * @return [OrderAction] with order ID and status
     * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
 */
    fun placeSuperOrder(params: PlaceSuperOrderParams): OrderAction =
        executePlaceSuperOrder(apiClient, config, params)

    /**
     * Modifies a Super Order leg.
     *
     * Use legName to specify which leg to modify: ENTRY_LEG, TARGET_LEG, or STOP_LOSS_LEG.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = ModifySuperOrderParams {
     *     legName = LegName.STOP_LOSS_LEG
     *     stopLossPrice = 1405.0
     *     trailingJump = 10.0
     * }
     * val result = dhan.modifySuperOrder("112111182198", params)
     * println("Modified: ${result.orderStatus}")
     * ```
     *
     * ## Java Example
     * ```java
     * ModifySuperOrderParams params = ModifySuperOrderParams.builder()
     *     .legName(LegName.STOP_LOSS_LEG)
     *     .stopLossPrice(1405.0)
     *     .trailingJump(10.0)
     *     .build();
     * OrderAction result = dhan.modifySuperOrder("112111182198", params);
     * ```
     *
     * @param orderId Order ID to modify
     * @param params Modification parameters with legName
     * @return [OrderAction] with modified order status
     * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
     */
    fun modifySuperOrder(orderId: String, params: ModifySuperOrderParams): OrderAction =
        executeModifySuperOrder(apiClient, config, orderId, params)

    /**
     * Cancels a Super Order or specific leg.
     *
     * Use legName to specify which leg to cancel.
     *
     * ## Kotlin Example
     * ```kotlin
     * // Cancel the entry leg
     * val result = dhan.cancelSuperOrder("112111182198", LegName.ENTRY_LEG)
     * println("Status: ${result.orderStatus}")
     * ```
     *
     * ## Java Example
     * ```java
     * // Cancel the entry leg
     * OrderAction result = dhan.cancelSuperOrder("112111182198", LegName.ENTRY_LEG);
     * System.out.println("Status: " + result.getOrderStatus());
     * ```
     *
     * @param orderId Order ID to cancel
     * @param legName Leg to cancel: ENTRY_LEG, TARGET_LEG, or STOP_LOSS_LEG
     * @return [OrderAction] with cancelled order status
     * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
     */
    fun cancelSuperOrder(orderId: String, legName: LegName): OrderAction =
        executeCancelSuperOrder(apiClient, orderId, legName)

    /**
     * Gets all Super Orders for the day.
     *
     * ## Kotlin Example
     * ```kotlin
     * val orders = dhan.getSuperOrders()
     * orders.forEach { order ->
     *     println("${order.orderId}: ${order.tradingSymbol} - ${order.orderStatus}")
     *     order.legDetails?.forEach { leg ->
     *         println("  ${leg.legName}: ${leg.orderStatus}")
     *     }
     * }
     * ```
     *
     * ## Java Example
     * ```java
     * List<SuperOrderBookItem> orders = dhan.getSuperOrders();
     * for (SuperOrderBookItem order : orders) {
     *     System.out.println(order.getOrderId() + ": " + order.getTradingSymbol());
     *     if (order.getLegDetails() != null) {
     *         for (SuperOrderLegDetail leg : order.getLegDetails()) {
     *             System.out.println("  " + leg.getLegName() + ": " + leg.getOrderStatus());
     *         }
     *     }
     * }
     * ```
     *
     * @return List of [SuperOrderBookItem] for the day
     * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
     */
    fun getSuperOrders(): List<SuperOrderBookItem> =
        executeGetSuperOrders(apiClient)

    // ==================== Forever Orders (GTT) ====================

    /**
     * Places a Forever Order (GTT - Good Till Triggered).
     *
     * Forever Orders remain active until the trigger condition is met or cancelled.
     * Supports SINGLE and OCO (One Cancels Other) order types.
     *
     * ## Kotlin Example
     * ```kotlin
     * // Single Forever Order
     * val params = PlaceForeverOrderParams {
     *     orderFlag = ForeverOrderFlag.SINGLE
     *     transactionType = TransactionType.BUY
     *     exchangeSegment = ExchangeSegment.NSE_EQ
     *     productType = ProductType.CNC
     *     orderType = OrderType.LIMIT
     *     validity = Validity.DAY
     *     securityId = "1333"
     *     quantity = 10
     *     price = 1400.0
     *     triggerPrice = 1395.0
     * }
     * val result = dhan.placeForeverOrder(params)
     * println("Order ID: ${result.orderId}")
     * ```
     *
     * ## Java Example
     * ```java
     * PlaceForeverOrderParams params = PlaceForeverOrderParams.builder()
     *     .orderFlag(ForeverOrderFlag.SINGLE)
     *     .transactionType(TransactionType.BUY)
     *     .exchangeSegment(ExchangeSegment.NSE_EQ)
     *     .productType(ProductType.CNC)
     *     .orderType(OrderType.LIMIT)
     *     .validity(Validity.DAY)
     *     .securityId("1333")
     *     .quantity(10)
     *     .price(1400.0)
     *     .triggerPrice(1395.0)
     *     .build();
     * OrderAction result = dhan.placeForeverOrder(params);
     * ```
     *
     * @param params Forever Order placement parameters
     * @return [OrderAction] with order ID and status
     * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
     */
    fun placeForeverOrder(params: PlaceForeverOrderParams): OrderAction =
        executePlaceForeverOrder(apiClient, config, params)

    /**
     * Modifies an existing Forever Order.
     *
     * Use legName to specify which leg to modify: TARGET_LEG or STOP_LOSS_LEG.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = ModifyForeverOrderParams {
     *     orderFlag = ForeverOrderFlag.SINGLE
     *     orderType = OrderType.LIMIT
     *     legName = LegName.TARGET_LEG
     *     quantity = 15
     *     price = 1450.0
     *     triggerPrice = 1445.0
     *     validity = Validity.DAY
     * }
     * val result = dhan.modifyForeverOrder("112111182198", params)
     * ```
     *
     * ## Java Example
     * ```java
     * ModifyForeverOrderParams params = ModifyForeverOrderParams.builder()
     *     .orderFlag(ForeverOrderFlag.SINGLE)
     *     .orderType(OrderType.LIMIT)
     *     .legName(LegName.TARGET_LEG)
     *     .quantity(15)
     *     .price(1450.0)
     *     .triggerPrice(1445.0)
     *     .validity(Validity.DAY)
     *     .build();
     * OrderAction result = dhan.modifyForeverOrder("112111182198", params);
     * ```
     *
     * @param orderId Order ID to modify
     * @param params Modification parameters
     * @return [OrderAction] with modified order status
     * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
     */
    fun modifyForeverOrder(orderId: String, params: ModifyForeverOrderParams): OrderAction =
        executeModifyForeverOrder(apiClient, config, orderId, params)

    /**
     * Cancels a Forever Order.
     *
     * ## Kotlin Example
     * ```kotlin
     * val result = dhan.cancelForeverOrder("112111182198")
     * println("Status: ${result.orderStatus}")
     * ```
     *
     * ## Java Example
     * ```java
     * OrderAction result = dhan.cancelForeverOrder("112111182198");
     * System.out.println("Status: " + result.getOrderStatus());
     * ```
     *
     * @param orderId Order ID to cancel
     * @return [OrderAction] with cancelled order status
     * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
     */
    fun cancelForeverOrder(orderId: String): OrderAction =
        executeCancelForeverOrder(apiClient, orderId)

    /**
     * Gets all Forever Orders.
     *
     * ## Kotlin Example
     * ```kotlin
     * val orders = dhan.getForeverOrders()
     * orders.forEach { order ->
     *     println("${order.orderId}: ${order.tradingSymbol} - ${order.orderStatus}")
     * }
     * ```
     *
     * ## Java Example
     * ```java
     * List<ForeverOrderBookItem> orders = dhan.getForeverOrders();
     * for (ForeverOrderBookItem order : orders) {
     *     System.out.println(order.getOrderId() + ": " + order.getTradingSymbol());
     * }
     * ```
     *
     * @return List of [ForeverOrderBookItem]
     * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
     */
    fun getForeverOrders(): List<ForeverOrderBookItem> =
        executeGetForeverOrders(apiClient)

    // ==================== Portfolio ====================

    /**
     * Gets all holdings from demat account.
     *
     * ## Kotlin Example
     * ```kotlin
     * val holdings = dhan.getHoldings()
     * holdings.forEach { holding ->
     *     println("${holding.tradingSymbol}: ${holding.totalQuantity} @ ${holding.averageCostPrice}")
     * }
     * ```
     *
     * ## Java Example
     * ```java
     * List<Holding> holdings = dhan.getHoldings();
     * for (Holding holding : holdings) {
     *     System.out.println(holding.getTradingSymbol() + ": " + holding.getTotalQuantity());
     * }
     * ```
     *
     * @return List of [Holding] in demat account
     * @see <a href="https://dhanhq.co/docs/v2/portfolio/">DhanHQ Portfolio API</a>
     */
    fun getHoldings(): List<Holding> =
        executeGetHoldings(apiClient)

    /**
     * Gets all open positions for the trading day.
     *
     * ## Kotlin Example
     * ```kotlin
     * val positions = dhan.getPositions()
     * positions.forEach { pos ->
     *     println("${pos.tradingSymbol}: ${pos.netQuantity} P&L: ${pos.unrealizedProfit}")
     * }
     * ```
     *
     * ## Java Example
     * ```java
     * List<Position> positions = dhan.getPositions();
     * for (Position pos : positions) {
     *     System.out.println(pos.getTradingSymbol() + ": " + pos.getNetQuantity());
     * }
     * ```
     *
     * @return List of [Position] open positions
     * @see <a href="https://dhanhq.co/docs/v2/portfolio/">DhanHQ Portfolio API</a>
     */
    fun getPositions(): List<Position> =
        executeGetPositions(apiClient)

    /**
     * Converts position between product types.
     *
     * Use this to convert between INTRADAY and CNC/MARGIN.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = ConvertPositionParams {
     *     fromProductType = ProductType.INTRADAY
     *     toProductType = ProductType.CNC
     *     exchangeSegment = ExchangeSegment.NSE_EQ
     *     positionType = PositionType.LONG
     *     securityId = "1333"
     *     convertQty = 10
     * }
     * dhan.convertPosition(params)
     * ```
     *
     * ## Java Example
     * ```java
     * ConvertPositionParams params = ConvertPositionParams.builder()
     *     .fromProductType(ProductType.INTRADAY)
     *     .toProductType(ProductType.CNC)
     *     .exchangeSegment(ExchangeSegment.NSE_EQ)
     *     .positionType(PositionType.LONG)
     *     .securityId("1333")
     *     .convertQty(10)
     *     .build();
     * dhan.convertPosition(params);
     * ```
     *
     * @param params Position conversion parameters
     * @see <a href="https://dhanhq.co/docs/v2/portfolio/">DhanHQ Portfolio API</a>
     */
    fun convertPosition(params: ConvertPositionParams) =
        executeConvertPosition(apiClient, config, params)

    // ==================== Market Quote ====================

    /**
     * Gets Last Traded Price for instruments.
     *
     * Max 1000 instruments per request. Rate limit: 1 request/second.
     *
     * ## Kotlin Example
     * ```kotlin
     * val instruments = mapOf(
     *     "NSE_EQ" to listOf(1333, 11536),
     *     "BSE_EQ" to listOf(500180)
     * )
     * val ltpData = dhan.getLtp(instruments)
     * ltpData["NSE_EQ"]?.forEach { (secId, ltp) ->
     *     println("$secId: ${ltp.lastPrice}")
     * }
     * ```
     *
     * ## Java Example
     * ```java
     * Map<String, List<Integer>> instruments = Map.of(
     *     "NSE_EQ", List.of(1333, 11536)
     * );
     * Map<String, Map<String, Ltp>> ltpData = dhan.getLtp(instruments);
     * ltpData.get("NSE_EQ").forEach((secId, ltp) ->
     *     System.out.println(secId + ": " + ltp.getLastPrice())
     * );
     * ```
     *
     * @param instruments Map of exchange segment to list of security IDs
     * @return Nested map of exchange segment to security ID to [Ltp]
     * @see <a href="https://dhanhq.co/docs/v2/market-quote/">DhanHQ Market Quote API</a>
     */
    fun getLtp(instruments: Map<String, List<Int>>): Map<String, Map<String, Ltp>> =
        executeGetLtp(apiClient, instruments)

    /**
     * Gets OHLC data for instruments.
     *
     * Max 1000 instruments per request. Rate limit: 1 request/second.
     *
     * ## Kotlin Example
     * ```kotlin
     * val instruments = mapOf("NSE_EQ" to listOf(1333))
     * val ohlcData = dhan.getOhlc(instruments)
     * ohlcData["NSE_EQ"]?.get("1333")?.let { quote ->
     *     println("LTP: ${quote.lastPrice}, Open: ${quote.ohlc.open}")
     * }
     * ```
     *
     * ## Java Example
     * ```java
     * Map<String, List<Integer>> instruments = Map.of("NSE_EQ", List.of(1333));
     * Map<String, Map<String, OhlcQuote>> ohlcData = dhan.getOhlc(instruments);
     * OhlcQuote quote = ohlcData.get("NSE_EQ").get("1333");
     * System.out.println("LTP: " + quote.getLastPrice());
     * ```
     *
     * @param instruments Map of exchange segment to list of security IDs
     * @return Nested map of exchange segment to security ID to [OhlcQuote]
     * @see <a href="https://dhanhq.co/docs/v2/market-quote/">DhanHQ Market Quote API</a>
     */
    fun getOhlc(instruments: Map<String, List<Int>>): Map<String, Map<String, OhlcQuote>> =
        executeGetOhlc(apiClient, instruments)

    /**
     * Gets full quote data with market depth.
     *
     * Includes LTP, OHLC, volume, OI, and 5-level market depth.
     * Max 1000 instruments per request. Rate limit: 1 request/second.
     *
     * ## Kotlin Example
     * ```kotlin
     * val instruments = mapOf("NSE_EQ" to listOf(1333))
     * val quotes = dhan.getQuote(instruments)
     * quotes["NSE_EQ"]?.get("1333")?.let { quote ->
     *     println("LTP: ${quote.lastPrice}, Volume: ${quote.volume}")
     *     quote.depth?.buy?.forEach { level ->
     *         println("Buy: ${level.quantity} @ ${level.price}")
     *     }
     * }
     * ```
     *
     * ## Java Example
     * ```java
     * Map<String, List<Integer>> instruments = Map.of("NSE_EQ", List.of(1333));
     * Map<String, Map<String, FullQuote>> quotes = dhan.getQuote(instruments);
     * FullQuote quote = quotes.get("NSE_EQ").get("1333");
     * System.out.println("LTP: " + quote.getLastPrice());
     * ```
     *
     * @param instruments Map of exchange segment to list of security IDs
     * @return Nested map of exchange segment to security ID to [FullQuote]
     * @see <a href="https://dhanhq.co/docs/v2/market-quote/">DhanHQ Market Quote API</a>
     */
    fun getQuote(instruments: Map<String, List<Int>>): Map<String, Map<String, FullQuote>> =
        executeGetQuote(apiClient, instruments)

    // ==================== Historical Data ====================

    /**
     * Gets daily historical OHLCV data.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = DailyHistoryParams {
     *     securityId = "1333"
     *     exchangeSegment = ExchangeSegment.NSE_EQ
     *     instrument = InstrumentType.EQUITY
     *     fromDate = "2024-01-01"
     *     toDate = "2024-02-01"
     * }
     * val history = dhan.getDailyHistory(params)
     * history.close.forEachIndexed { i, close ->
     *     println("${history.timestamp[i]}: $close")
     * }
     * ```
     *
     * ## Java Example
     * ```java
     * GetDailyHistoryParams params = GetDailyHistoryParams.builder()
     *     .securityId("1333")
     *     .exchangeSegment(ExchangeSegment.NSE_EQ)
     *     .instrument(InstrumentType.EQUITY)
     *     .fromDate("2024-01-01")
     *     .toDate("2024-02-01")
     *     .build();
     * HistoricalData history = dhan.getDailyHistory(params);
     * ```
     *
     * @param params Daily historical data parameters
     * @return [HistoricalData] with OHLCV arrays
     * @see <a href="https://dhanhq.co/docs/v2/historical-data/">DhanHQ Historical Data API</a>
     */
    fun getDailyHistory(params: GetDailyHistoryParams): HistoricalData =
        executeGetDailyHistory(apiClient, params)

    /**
     * Gets intraday historical OHLCV data.
     *
     * Limited to 90-day queries per request.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = IntradayHistoryParams {
     *     securityId = "1333"
     *     exchangeSegment = ExchangeSegment.NSE_EQ
     *     instrument = InstrumentType.EQUITY
     *     interval = ChartInterval.MINUTE_5
     *     fromDate = "2024-01-15 09:15:00"
     *     toDate = "2024-01-15 15:30:00"
     * }
     * val history = dhan.getIntradayHistory(params)
     * ```
     *
     * ## Java Example
     * ```java
     * GetIntradayHistoryParams params = GetIntradayHistoryParams.builder()
     *     .securityId("1333")
     *     .exchangeSegment(ExchangeSegment.NSE_EQ)
     *     .instrument(InstrumentType.EQUITY)
     *     .interval(ChartInterval.MINUTE_5)
     *     .fromDate("2024-01-15 09:15:00")
     *     .toDate("2024-01-15 15:30:00")
     *     .build();
     * HistoricalData history = dhan.getIntradayHistory(params);
     * ```
     *
     * @param params Intraday historical data parameters
     * @return [HistoricalData] with OHLCV arrays
     * @see <a href="https://dhanhq.co/docs/v2/historical-data/">DhanHQ Historical Data API</a>
     */
    fun getIntradayHistory(params: GetIntradayHistoryParams): HistoricalData =
        executeGetIntradayHistory(apiClient, params)

    // ==================== Option Chain ====================

    /**
     * Gets available expiry dates for an underlying.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = ExpiryListParams {
     *     underlyingSecurityId = "13"
     *     underlyingInstrumentType = InstrumentType.INDEX
     * }
     * val expiries = dhan.getExpiryList(params)
     * expiries.forEach { println("Expiry: $it") }
     * ```
     *
     * ## Java Example
     * ```java
     * GetExpiryListParams params = GetExpiryListParams.builder()
     *     .underlyingSecurityId("13")
     *     .underlyingInstrumentType(InstrumentType.INDEX)
     *     .build();
     * List<String> expiries = dhan.getExpiryList(params);
     * ```
     *
     * @param params Expiry list parameters
     * @return List of expiry dates in YYYY-MM-DD format
     * @see <a href="https://dhanhq.co/docs/v2/option-chain/">DhanHQ Option Chain API</a>
     */
    fun getExpiryList(params: GetExpiryListParams): List<String> =
        executeGetExpiryList(apiClient, params)

    /**
     * Gets option chain data for an underlying.
     *
     * Rate limit: 1 request per 3 seconds.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = OptionChainParams {
     *     underlyingSecurityId = "13"
     *     underlyingInstrumentType = InstrumentType.INDEX
     *     expiryDate = "2024-01-25"
     * }
     * val chain = dhan.getOptionChain(params)
     * chain.data.forEach { strike ->
     *     println("Strike: ${strike.strikePrice}")
     * }
     * ```
     *
     * ## Java Example
     * ```java
     * GetOptionChainParams params = GetOptionChainParams.builder()
     *     .underlyingSecurityId("13")
     *     .underlyingInstrumentType(InstrumentType.INDEX)
     *     .expiryDate("2024-01-25")
     *     .build();
     * OptionChain chain = dhan.getOptionChain(params);
     * ```
     *
     * @param params Option chain parameters
     * @return [OptionChain] with strikes and options
     * @see <a href="https://dhanhq.co/docs/v2/option-chain/">DhanHQ Option Chain API</a>
     */
    fun getOptionChain(params: GetOptionChainParams): OptionChain =
        executeGetOptionChain(apiClient, params)

    // ==================== Funds ====================

    /**
     * Gets fund limit and margin details.
     *
     * ## Kotlin Example
     * ```kotlin
     * val funds = dhan.getFundLimits()
     * println("Available: ${funds.availableBalance}")
     * println("Utilized: ${funds.utilizedAmount}")
     * ```
     *
     * ## Java Example
     * ```java
     * FundLimit funds = dhan.getFundLimits();
     * System.out.println("Available: " + funds.getAvailableBalance());
     * ```
     *
     * @return [FundLimit] with balance and margin details
     * @see <a href="https://dhanhq.co/docs/v2/funds/">DhanHQ Funds API</a>
     */
    fun getFundLimits(): FundLimit =
        executeGetFundLimits(apiClient)

    /**
     * Calculates margin requirements for an order.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = CalculateMarginParams {
     *     exchangeSegment = ExchangeSegment.NSE_FNO
     *     transactionType = TransactionType.BUY
     *     quantity = 50
     *     productType = ProductType.INTRADAY
     *     securityId = "52175"
     *     price = 500.0
     * }
     * val margin = dhan.calculateMargin(params)
     * println("Total margin: ${margin.totalMargin}")
     * ```
     *
     * ## Java Example
     * ```java
     * CalculateMarginParams params = CalculateMarginParams.builder()
     *     .exchangeSegment(ExchangeSegment.NSE_FNO)
     *     .transactionType(TransactionType.BUY)
     *     .quantity(50)
     *     .productType(ProductType.INTRADAY)
     *     .securityId("52175")
     *     .price(500.0)
     *     .build();
     * MarginCalculation margin = dhan.calculateMargin(params);
     * ```
     *
     * @param params Margin calculation parameters
     * @return [MarginCalculation] with margin requirements
     * @see <a href="https://dhanhq.co/docs/v2/funds/">DhanHQ Funds API</a>
     */
    fun calculateMargin(params: CalculateMarginParams): MarginCalculation =
        executeCalculateMargin(apiClient, config, params)

    // ==================== Auth ====================

    /**
     * Generates a consent app ID for the authentication flow.
     *
     * This is the first step in the consent-based authentication flow for individual traders.
     * The returned consentAppId serves as a temporary session identifier required for
     * the subsequent browser-based login step.
     *
     * Users can generate up to 25 consent app IDs daily.
     *
     * ## Kotlin Example
     * ```kotlin
     * val consent = dhan.generateConsent(
     *     appId = "your-app-id",
     *     appSecret = "your-app-secret"
     * )
     * println("Consent App ID: ${consent.consentAppId}")
     * // Redirect user to: https://login.dhan.co?consent_id=${consent.consentAppId}
     * ```
     *
     * ## Java Example
     * ```java
     * GenerateConsentResult consent = dhan.generateConsent("your-app-id", "your-app-secret");
     * System.out.println("Consent App ID: " + consent.getConsentAppId());
     * ```
     *
     * @param appId API Key generated from Dhan
     * @param appSecret API Secret generated from Dhan
     * @return [GenerateConsentResult] with consentAppId for browser login
     * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication API</a>
     */
    fun generateConsent(appId: String, appSecret: String): GenerateConsentResult =
        executeGenerateConsent(authApiClient, config.clientId, appId, appSecret)

    /**
     * Consumes the consent token to obtain access credentials.
     *
     * This is the final step in the consent-based authentication flow. After the user
     * completes browser login and grants consent, use this method with the tokenId
     * received from the callback to obtain the access token.
     *
     * ## Kotlin Example
     * ```kotlin
     * val credentials = dhan.consumeConsent(
     *     tokenId = "token-from-callback",
     *     appId = "your-app-id",
     *     appSecret = "your-app-secret"
     * )
     * println("Access Token: ${credentials.accessToken}")
     * println("Expires: ${credentials.expiryTime}")
     *
     * // Use the access token for trading APIs
     * dhan.setAccessToken(credentials.accessToken)
     * ```
     *
     * ## Java Example
     * ```java
     * ConsumeConsentResult credentials = dhan.consumeConsent(
     *     "token-from-callback",
     *     "your-app-id",
     *     "your-app-secret"
     * );
     * System.out.println("Access Token: " + credentials.getAccessToken());
     * dhan.setAccessToken(credentials.getAccessToken());
     * ```
     *
     * @param tokenId User-specific token obtained from browser login callback
     * @param appId API Key generated from Dhan
     * @param appSecret API Secret generated from Dhan
     * @return [ConsumeConsentResult] with accessToken and user details
     * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication API</a>
     */
    fun consumeConsent(tokenId: String, appId: String, appSecret: String): ConsumeConsentResult =
        executeConsumeConsent(authApiClient, tokenId, appId, appSecret)

    /**
     * Gets user profile and token validity.
     *
     * ## Kotlin Example
     * ```kotlin
     * val profile = dhan.getProfile()
     * println("Client: ${profile.dhanClientId}")
     * println("Name: ${profile.clientName}")
     * ```
     *
     * ## Java Example
     * ```java
     * UserProfile profile = dhan.getProfile();
     * System.out.println("Client: " + profile.getDhanClientId());
     * ```
     *
     * @return [UserProfile] with user details
     * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication API</a>
     */
    fun getProfile(): UserProfile =
        executeGetProfile(apiClient)

    /**
     * Sets static IP for order API whitelisting.
     *
     * Required before placing orders through API.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = SetIpParams {
     *     ipFlag = IpFlag.CUSTOM
     *     ipAddresses = listOf("192.168.1.1")
     * }
     * val result = dhan.setIp(params)
     * ```
     *
     * @param params IP configuration parameters
     * @return [SetIpResult] with confirmation
     * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication API</a>
     */
    fun setIp(params: SetIpParams): SetIpResult =
        executeSetIp(apiClient, config, params)

    /**
     * Modifies static IP configuration.
     *
     * Allowed once every 7 days.
     *
     * @param params IP configuration parameters
     * @return [SetIpResult] with confirmation
     * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication API</a>
     */
    fun modifyIp(params: SetIpParams): SetIpResult =
        executeModifyIp(apiClient, config, params)

    /**
     * Gets current static IP configuration.
     *
     * ## Kotlin Example
     * ```kotlin
     * val config = dhan.getIpConfiguration()
     * config.whitelistedIps?.forEach { println("IP: $it") }
     * ```
     *
     * @return [IpConfiguration] with whitelisted IPs
     * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication API</a>
     */
    fun getIpConfiguration(): IpConfiguration =
        executeGetIpConfiguration(apiClient)

    /**
     * Renews the access token.
     *
     * Extends token validity for 24 hours.
     *
     * ## Kotlin Example
     * ```kotlin
     * dhan.renewToken()
     * println("Token renewed successfully")
     * ```
     *
     * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication API</a>
     */
    fun renewToken() =
        executeRenewToken(apiClient)

    // ==================== EDIS ====================

    /**
     * Generates T-PIN for EDIS authentication.
     *
     * T-PIN is sent to the registered mobile number.
     *
     * ## Kotlin Example
     * ```kotlin
     * dhan.generateTpin()
     * // T-PIN sent to registered mobile
     * ```
     *
     * @see <a href="https://dhanhq.co/docs/v2/edis/">DhanHQ EDIS API</a>
     */
    fun generateTpin() =
        executeGenerateTpin(apiClient)

    /**
     * Generates EDIS form for T-PIN entry and stock marking.
     *
     * ## Kotlin Example
     * ```kotlin
     * val params = GenerateEdisFormParams {
     *     isin = "INE040A01034"
     *     quantity = 10
     * }
     * val form = dhan.generateEdisForm(params)
     * // Use form.edisFormHtml to display the form
     * ```
     *
     * @param params EDIS form parameters
     * @return [EdisForm] with HTML form
     * @see <a href="https://dhanhq.co/docs/v2/edis/">DhanHQ EDIS API</a>
     */
    fun generateEdisForm(params: GenerateEdisFormParams): EdisForm =
        executeGenerateEdisForm(apiClient, params)

    /**
     * Inquires EDIS approval status for a security.
     *
     * Use "ALL" as isin to check status for all holdings.
     *
     * ## Kotlin Example
     * ```kotlin
     * val status = dhan.inquireEdisStatus("INE040A01034")
     * status.data?.forEach { println("${it.isin}: ${it.edisStatus}") }
     * ```
     *
     * @param isin Security identifier or "ALL"
     * @return [EdisStatusResponse] with approval status
     * @see <a href="https://dhanhq.co/docs/v2/edis/">DhanHQ EDIS API</a>
     */
    fun inquireEdisStatus(isin: String): EdisStatusResponse =
        executeInquireEdisStatus(apiClient, isin)

    // ==================== Traders Control ====================

    /**
     * Activates or deactivates the kill switch.
     *
     * When activated, all pending orders are cancelled and no new orders can be placed.
     *
     * ## Kotlin Example
     * ```kotlin
     * val result = dhan.setKillSwitch(KillSwitchStatus.ACTIVATE)
     * println("Kill switch: ${result.killSwitchStatus}")
     * ```
     *
     * @param status ACTIVATE or DEACTIVATE
     * @return [KillSwitchResult] with confirmation
     * @see <a href="https://dhanhq.co/docs/v2/traders-control/">DhanHQ Traders Control API</a>
     */
    fun setKillSwitch(status: KillSwitchStatus): KillSwitchResult =
        executeSetKillSwitch(apiClient, status)

    // ==================== Statement ====================

    /**
     * Gets ledger entries for a date range.
     *
     * ## Kotlin Example
     * ```kotlin
     * val entries = dhan.getLedger("2024-01-01", "2024-01-31")
     * entries.forEach { entry ->
     *     println("${entry.date}: ${entry.narration} - ${entry.amount}")
     * }
     * ```
     *
     * ## Java Example
     * ```java
     * List<LedgerEntry> entries = dhan.getLedger("2024-01-01", "2024-01-31");
     * ```
     *
     * @param fromDate Start date in YYYY-MM-DD format
     * @param toDate End date in YYYY-MM-DD format
     * @return List of [LedgerEntry]
     * @see <a href="https://dhanhq.co/docs/v2/statements/">DhanHQ Statement API</a>
     */
    fun getLedger(fromDate: String, toDate: String): List<LedgerEntry> =
        executeGetLedger(apiClient, fromDate, toDate)

    /**
     * Gets trade history for a date range.
     *
     * ## Kotlin Example
     * ```kotlin
     * val trades = dhan.getTradeHistory("2024-01-01", "2024-01-31")
     * trades.forEach { trade ->
     *     println("${trade.tradingSymbol}: ${trade.quantity} @ ${trade.price}")
     * }
     * ```
     *
     * ## Java Example
     * ```java
     * List<TradeHistory> trades = dhan.getTradeHistory("2024-01-01", "2024-01-31", 0);
     * ```
     *
     * @param fromDate Start date in YYYY-MM-DD format
     * @param toDate End date in YYYY-MM-DD format
     * @param page Page number (0-indexed)
     * @return List of [TradeHistory]
     * @see <a href="https://dhanhq.co/docs/v2/statements/">DhanHQ Statement API</a>
     */
    fun getTradeHistory(fromDate: String, toDate: String, page: Int = 0): List<TradeHistory> =
        executeGetTradeHistory(apiClient, fromDate, toDate, page)

    // ==================== Instruments ====================

    /**
     * Gets instruments for a specific exchange segment.
     *
     * ## Kotlin Example
     * ```kotlin
     * val instruments = dhan.getInstruments(ExchangeSegment.NSE_EQ)
     * // Parse CSV data
     * ```
     *
     * @param segment Exchange segment
     * @return Raw instrument data string (CSV format)
     * @see <a href="https://dhanhq.co/docs/v2/instruments/">DhanHQ Instruments API</a>
     */
    fun getInstruments(segment: ExchangeSegment): String =
        executeGetInstruments(apiClient, segment)

    /**
     * Gets the URL for the compact instruments CSV.
     *
     * @return URL to download compact CSV
     * @see <a href="https://dhanhq.co/docs/v2/instruments/">DhanHQ Instruments API</a>
     */
    fun getCompactCsvUrl(): String = io.github.sonicalgo.dhan.usecase.getCompactCsvUrl()

    /**
     * Gets the URL for the detailed instruments CSV.
     *
     * @return URL to download detailed CSV
     * @see <a href="https://dhanhq.co/docs/v2/instruments/">DhanHQ Instruments API</a>
     */
    fun getDetailedCsvUrl(): String = io.github.sonicalgo.dhan.usecase.getDetailedCsvUrl()

    // ==================== WebSocket Clients ====================

    /**
     * Creates a new Market Feed WebSocket client.
     *
     * The client is automatically tracked and will be closed when [close] is called.
     *
     * ## Kotlin Example
     * ```kotlin
     * val client = dhan.createMarketFeedClient()
     * client.addListener(object : MarketFeedListener {
     *     override fun onTickerData(data: TickerData) {
     *         println("${data.securityId}: ${data.ltp}")
     *     }
     * })
     * client.connect()
     * client.subscribe(listOf(Instrument(ExchangeSegment.NSE_EQ, "1333")), FeedMode.TICKER)
     * ```
     *
     * ## Java Example
     * ```java
     * MarketFeedClient client = dhan.createMarketFeedClient();
     * client.addListener(new MarketFeedListener() {
     *     @Override public void onTickerData(TickerData data) {
     *         System.out.println(data.getSecurityId() + ": " + data.getLtp());
     *     }
     * });
     * client.connect();
     * ```
     *
     * @param maxReconnectAttempts Maximum reconnection attempts, 1-20 (default: 5)
     * @param autoReconnectEnabled Enable automatic reconnection (default: true)
     * @param autoResubscribeEnabled Enable auto-resubscription after reconnect (default: true)
     * @return New [MarketFeedClient] instance
     * @throws IllegalArgumentException if maxReconnectAttempts is not between 1 and 20
     * @see <a href="https://dhanhq.co/docs/v2/live-market-feed/">DhanHQ Live Market Feed</a>
     */
    fun createMarketFeedClient(
        maxReconnectAttempts: Int = DhanConstants.WEBSOCKET_DEFAULT_MAX_RECONNECT_ATTEMPTS,
        autoReconnectEnabled: Boolean = true,
        autoResubscribeEnabled: Boolean = true
    ): MarketFeedClient {
        require(maxReconnectAttempts in 1..20) { "maxReconnectAttempts must be between 1 and 20" }
        val wsConfig = DhanWebSocketConfig(
            maxReconnectAttempts = maxReconnectAttempts,
            autoReconnectEnabled = autoReconnectEnabled,
            autoResubscribeEnabled = autoResubscribeEnabled
        )
        val client = MarketFeedClient(config, wsConfig, clientProvider.getWsHttpClient(DhanConstants.WEBSOCKET_PING_INTERVAL_MS))
        webSocketClients.add(client)
        return client
    }

    /**
     * Creates a new Order Update WebSocket client.
     *
     * The client is automatically tracked and will be closed when [close] is called.
     *
     * ## Kotlin Example
     * ```kotlin
     * val client = dhan.createOrderStreamClient()
     * client.addListener(object : OrderStreamListener {
     *     override fun onOrderUpdate(update: OrderUpdate) {
     *         println("Order ${update.orderId}: ${update.orderStatus}")
     *     }
     * })
     * client.connect()
     * ```
     *
     * ## Java Example
     * ```java
     * OrderStreamClient client = dhan.createOrderStreamClient();
     * client.addListener(new OrderStreamListener() {
     *     @Override public void onOrderUpdate(OrderUpdate update) {
     *         System.out.println("Order " + update.getOrderId() + ": " + update.getOrderStatus());
     *     }
     * });
     * client.connect();
     * ```
     *
     * @param maxReconnectAttempts Maximum reconnection attempts, 1-20 (default: 5)
     * @param autoReconnectEnabled Enable automatic reconnection (default: true)
     * @return New [OrderStreamClient] instance
     * @throws IllegalArgumentException if maxReconnectAttempts is not between 1 and 20
     * @see <a href="https://dhanhq.co/docs/v2/order-update/">DhanHQ Order Update</a>
     */
    fun createOrderStreamClient(
        maxReconnectAttempts: Int = DhanConstants.WEBSOCKET_DEFAULT_MAX_RECONNECT_ATTEMPTS,
        autoReconnectEnabled: Boolean = true
    ): OrderStreamClient {
        require(maxReconnectAttempts in 1..20) { "maxReconnectAttempts must be between 1 and 20" }
        val wsConfig = DhanWebSocketConfig(
            maxReconnectAttempts = maxReconnectAttempts,
            autoReconnectEnabled = autoReconnectEnabled,
            autoResubscribeEnabled = false // Not applicable for order updates
        )
        val client = OrderStreamClient(config, wsConfig, clientProvider.getWsHttpClient(DhanConstants.WEBSOCKET_PING_INTERVAL_MS))
        webSocketClients.add(client)
        return client
    }

    // ==================== Lifecycle ====================

    /**
     * Closes this SDK instance and releases all resources.
     *
     * This method:
     * 1. Closes all WebSocket clients created by this instance
     * 2. Shuts down the HTTP client and connection pool
     *
     * After calling this method, the SDK instance should not be used.
     *
     * Supports try-with-resources:
     * ```kotlin
     * Dhan.builder().clientId("id").accessToken("token").build().use { dhan ->
     *     // SDK auto-closes when block exits
     * }
     * ```
     */
    override fun close() {
        // Close all tracked WebSocket clients (ignore errors to ensure all are attempted)
        webSocketClients.forEach { client ->
            runCatching { client.close() }
        }
        webSocketClients.clear()
        // Release HTTP resources
        clientProvider.shutdown()
    }

    // ==================== Builder ====================

    /**
     * Builder for creating [Dhan] instances.
     *
     * Example:
     * ```kotlin
     * val dhan = Dhan.builder()
     *     .clientId("your-client-id")        // Required
     *     .accessToken("your-token")         // Optional at build
     *     .loggingEnabled(true)              // Optional
     *     .rateLimitRetries(3)               // Optional
     *     .build()
     * ```
     */
    class Builder {
        private var clientId: String? = null
        private var accessToken: String = ""
        private var loggingEnabled: Boolean = false
        private var rateLimitRetries: Int = 0

        /**
         * Sets the Dhan client ID (required).
         *
         * @param clientId Your Dhan client ID
         * @return This builder for chaining
         * @throws IllegalArgumentException if clientId is blank
         */
        fun clientId(clientId: String): Builder = apply {
            require(clientId.isNotBlank()) { "clientId cannot be blank" }
            this.clientId = clientId
        }

        /**
         * Sets the OAuth access token (optional at build time).
         *
         * Can be set later using [Dhan.setAccessToken].
         *
         * @param token Access token from DhanHQ
         * @return This builder for chaining
         */
        fun accessToken(token: String): Builder = apply {
            this.accessToken = token
        }

        /**
         * Enables or disables HTTP request/response logging (default: false).
         *
         * Useful for debugging API calls. Logs at BODY level.
         *
         * @param enabled true to enable logging
         * @return This builder for chaining
         */
        fun loggingEnabled(enabled: Boolean): Builder = apply {
            this.loggingEnabled = enabled
        }

        /**
         * Sets the number of automatic retries for rate-limited requests (default: 0).
         *
         * When a rate limit (HTTP 429) is encountered, the SDK will wait
         * and retry up to this many times with exponential backoff.
         *
         * @param retries Number of retries (0-5)
         * @return This builder for chaining
         * @throws IllegalArgumentException if retries is not between 0 and 5
         */
        fun rateLimitRetries(retries: Int): Builder = apply {
            require(retries in 0..5) { "rateLimitRetries must be between 0 and 5" }
            this.rateLimitRetries = retries
        }

        /**
         * Builds and returns a new [Dhan] instance.
         *
         * @return New [Dhan] instance
         * @throws IllegalStateException if clientId is not set
         */
        fun build(): Dhan {
            val validClientId = requireNotNull(clientId) { "clientId is required" }

            val config = DhanConfig(
                clientId = validClientId,
                accessToken = accessToken,
                loggingEnabled = loggingEnabled,
                rateLimitRetries = rateLimitRetries
            )

            return Dhan(config)
        }
    }

    /**
     * Factory methods for creating [Dhan] SDK instances.
     *
     * Use [builder] to configure and create a new SDK instance:
     *
     * ```kotlin
     * val dhan = Dhan.builder()
     *     .clientId("your-client-id")
     *     .accessToken("your-access-token")
     *     .loggingEnabled(true)
     *     .build()
     * ```
     *
     * @see Dhan
     * @see Builder
     */
    companion object {
        /**
         * Creates a new builder for configuring a [Dhan] instance.
         *
         * Example:
         * ```kotlin
         * val dhan = Dhan.builder()
         *     .clientId("your-client-id")      // Required
         *     .accessToken("your-token")       // Optional at build time
         *     .loggingEnabled(true)            // Enable HTTP logging
         *     .rateLimitRetries(3)             // Auto-retry on rate limit
         *     .build()
         *
         * // Access APIs directly
         * val profile = dhan.getProfile()
         * val orders = dhan.getOrders()
         *
         * // Create WebSocket clients (config passed at creation time)
         * val marketFeed = dhan.createMarketFeedClient(maxReconnectAttempts = 10)
         * val orderUpdates = dhan.createOrderStreamClient()
         * ```
         *
         * @return New [Builder] instance
         * @see Builder
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
