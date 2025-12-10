package io.github.sonicalgo.dhan

import io.github.sonicalgo.core.client.HttpClientProvider
import io.github.sonicalgo.dhan.api.*
import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig
import io.github.sonicalgo.dhan.config.DhanConstants
import io.github.sonicalgo.dhan.config.DhanHeaderProvider
import io.github.sonicalgo.dhan.config.DhanWebSocketConfig
import io.github.sonicalgo.dhan.websocket.marketfeed.DhanMarketFeedClient
import io.github.sonicalgo.dhan.websocket.orderupdate.DhanOrderUpdateClient
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
 * val profile = dhan.getAuthApi().getProfile()
 * println("Client: ${profile.dhanClientId}")
 * ```
 *
 * ## Placing Orders
 *
 * ```kotlin
 * val ordersApi = dhan.getOrdersApi()
 *
 * // dhanClientId is auto-injected from config
 * val response = ordersApi.placeOrder(PlaceOrderParams(
 *     transactionType = TransactionType.BUY,
 *     exchangeSegment = ExchangeSegment.NSE_EQ,
 *     productType = ProductType.CNC,
 *     orderType = OrderType.LIMIT,
 *     validity = Validity.DAY,
 *     securityId = "1333",
 *     quantity = 10,
 *     price = 1428.0
 * ))
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
 *     listOf(Instrument.nseEquity("1333")),
 *     FeedMode.TICKER
 * )
 * ```
 *
 * ## Real-Time Order Updates
 *
 * ```kotlin
 * val orderClient = dhan.createOrderUpdateClient()
 * orderClient.addListener(object : OrderUpdateListener {
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
 * All API modules are thread-safe and can be called from any thread.
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

    // Track WebSocket clients for unified lifecycle management
    private val webSocketClients = CopyOnWriteArrayList<Closeable>()

    // Lazy per-instance API modules
    private val _ordersApi by lazy { OrdersApi(apiClient, config) }
    private val _superOrdersApi by lazy { SuperOrdersApi(apiClient, config) }
    private val _foreverOrdersApi by lazy { ForeverOrdersApi(apiClient, config) }
    private val _portfolioApi by lazy { PortfolioApi(apiClient, config) }
    private val _fundsApi by lazy { FundsApi(apiClient, config) }
    private val _edisApi by lazy { EdisApi(apiClient) }
    private val _tradersControlApi by lazy { TradersControlApi(apiClient) }
    private val _statementApi by lazy { StatementApi(apiClient) }
    private val _marketQuoteApi by lazy { MarketQuoteApi(apiClient) }
    private val _historicalDataApi by lazy { HistoricalDataApi(apiClient) }
    private val _optionChainApi by lazy { OptionChainApi(apiClient) }
    private val _instrumentsApi by lazy { InstrumentsApi(apiClient) }
    private val _authApi by lazy { AuthApi(apiClient, config) }

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

    // ==================== REST API Modules ====================

    /**
     * Gets the Orders API module.
     *
     * Provides order placement, modification, cancellation, and retrieval.
     *
     * @return [OrdersApi] instance
     */
    fun getOrdersApi(): OrdersApi = _ordersApi

    /**
     * Gets the Super Order API module.
     *
     * Provides multi-leg order (entry, target, stop-loss) operations.
     *
     * @return [SuperOrdersApi] instance
     */
    fun getSuperOrdersApi(): SuperOrdersApi = _superOrdersApi

    /**
     * Gets the Forever Order API module.
     *
     * Provides GTT (Good Till Triggered) order operations.
     *
     * @return [ForeverOrdersApi] instance
     */
    fun getForeverOrdersApi(): ForeverOrdersApi = _foreverOrdersApi

    /**
     * Gets the Portfolio API module.
     *
     * Provides holdings, positions, and conversion operations.
     *
     * @return [PortfolioApi] instance
     */
    fun getPortfolioApi(): PortfolioApi = _portfolioApi

    /**
     * Gets the Funds API module.
     *
     * Provides fund limit and margin calculator operations.
     *
     * @return [FundsApi] instance
     */
    fun getFundsApi(): FundsApi = _fundsApi

    /**
     * Gets the EDIS API module.
     *
     * Provides CDSL T-PIN and authorization operations.
     *
     * @return [EdisApi] instance
     */
    fun getEdisApi(): EdisApi = _edisApi

    /**
     * Gets the Trader's Control API module.
     *
     * Provides kill switch operations.
     *
     * @return [TradersControlApi] instance
     */
    fun getTradersControlApi(): TradersControlApi = _tradersControlApi

    /**
     * Gets the Statement API module.
     *
     * Provides ledger and trade history operations.
     *
     * @return [StatementApi] instance
     */
    fun getStatementApi(): StatementApi = _statementApi

    /**
     * Gets the Market Quote API module.
     *
     * Provides LTP, OHLC, and full quote data.
     *
     * @return [MarketQuoteApi] instance
     */
    fun getMarketQuoteApi(): MarketQuoteApi = _marketQuoteApi

    /**
     * Gets the Historical Data API module.
     *
     * Provides daily and intraday OHLCV data.
     *
     * @return [HistoricalDataApi] instance
     */
    fun getHistoricalDataApi(): HistoricalDataApi = _historicalDataApi

    /**
     * Gets the Option Chain API module.
     *
     * Provides option chain and expiry data.
     *
     * @return [OptionChainApi] instance
     */
    fun getOptionChainApi(): OptionChainApi = _optionChainApi

    /**
     * Gets the Instruments API module.
     *
     * Provides instrument master data access.
     *
     * @return [InstrumentsApi] instance
     */
    fun getInstrumentsApi(): InstrumentsApi = _instrumentsApi

    /**
     * Gets the Auth API module.
     *
     * Provides profile, IP configuration, and token operations.
     *
     * @return [AuthApi] instance
     */
    fun getAuthApi(): AuthApi = _authApi

    // ==================== WebSocket Clients ====================

    /**
     * Creates a new Market Feed WebSocket client.
     *
     * The client is automatically tracked and will be closed when [close] is called.
     *
     * Example:
     * ```kotlin
     * val client = dhan.createMarketFeedClient()
     * client.addListener(myListener)
     * client.connect()
     * client.subscribe(instruments, FeedMode.TICKER)
     *
     * // All clients are closed automatically when dhan.close() is called
     * ```
     *
     * @param maxReconnectAttempts Maximum reconnection attempts, 1-20 (default: 5)
     * @param autoReconnectEnabled Enable automatic reconnection (default: true)
     * @param autoResubscribeEnabled Enable auto-resubscription after reconnect (default: true)
     * @return New [DhanMarketFeedClient] instance
     * @throws IllegalArgumentException if maxReconnectAttempts is not between 1 and 20
     */
    fun createMarketFeedClient(
        maxReconnectAttempts: Int = DhanConstants.WEBSOCKET_DEFAULT_MAX_RECONNECT_ATTEMPTS,
        autoReconnectEnabled: Boolean = true,
        autoResubscribeEnabled: Boolean = true
    ): DhanMarketFeedClient {
        require(maxReconnectAttempts in 1..20) { "maxReconnectAttempts must be between 1 and 20" }
        val wsConfig = DhanWebSocketConfig(
            maxReconnectAttempts = maxReconnectAttempts,
            autoReconnectEnabled = autoReconnectEnabled,
            autoResubscribeEnabled = autoResubscribeEnabled
        )
        val client = DhanMarketFeedClient(config, wsConfig, clientProvider.getWsHttpClient(DhanConstants.WEBSOCKET_PING_INTERVAL_MS))
        webSocketClients.add(client)
        return client
    }

    /**
     * Creates a new Order Update WebSocket client.
     *
     * The client is automatically tracked and will be closed when [close] is called.
     *
     * Example:
     * ```kotlin
     * val client = dhan.createOrderUpdateClient()
     * client.addListener(myListener)
     * client.connect()
     *
     * // All clients are closed automatically when dhan.close() is called
     * ```
     *
     * @param maxReconnectAttempts Maximum reconnection attempts, 1-20 (default: 5)
     * @param autoReconnectEnabled Enable automatic reconnection (default: true)
     * @return New [DhanOrderUpdateClient] instance
     * @throws IllegalArgumentException if maxReconnectAttempts is not between 1 and 20
     */
    fun createOrderUpdateClient(
        maxReconnectAttempts: Int = DhanConstants.WEBSOCKET_DEFAULT_MAX_RECONNECT_ATTEMPTS,
        autoReconnectEnabled: Boolean = true
    ): DhanOrderUpdateClient {
        require(maxReconnectAttempts in 1..20) { "maxReconnectAttempts must be between 1 and 20" }
        val wsConfig = DhanWebSocketConfig(
            maxReconnectAttempts = maxReconnectAttempts,
            autoReconnectEnabled = autoReconnectEnabled,
            autoResubscribeEnabled = false // Not applicable for order updates
        )
        val client = DhanOrderUpdateClient(config, wsConfig, clientProvider.getWsHttpClient(DhanConstants.WEBSOCKET_PING_INTERVAL_MS))
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
        // Close all tracked WebSocket clients
        webSocketClients.forEach { it.close() }
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
            requireNotNull(clientId) { "clientId is required" }

            val config = DhanConfig(
                clientId = clientId!!,
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
         * // Access APIs
         * val profile = dhan.getAuthApi().getProfile()
         * val orders = dhan.getOrdersApi().getOrderBook()
         *
         * // Create WebSocket clients (config passed at creation time)
         * val marketFeed = dhan.createMarketFeedClient(maxReconnectAttempts = 10)
         * val orderUpdates = dhan.createOrderUpdateClient()
         * ```
         *
         * @return New [Builder] instance
         * @see Builder
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
