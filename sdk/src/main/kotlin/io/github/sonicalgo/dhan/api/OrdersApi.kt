package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.request.ModifyOrderParams
import io.github.sonicalgo.dhan.model.request.PlaceOrderParams
import io.github.sonicalgo.dhan.model.request.SlicingOrderParams
import io.github.sonicalgo.dhan.model.response.OrderAction
import io.github.sonicalgo.dhan.model.response.OrderBookItem
import io.github.sonicalgo.dhan.model.response.TradeBookItem

/**
 * API module for order management operations.
 *
 * Provides methods for placing, modifying, cancelling orders, and retrieving
 * order/trade information.
 *
 * Note: Order placement, modification, and cancellation APIs require
 * static IP whitelisting. Configure your IP at web.dhan.co.
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Place an order
 * val response = dhan.getOrdersApi().placeOrder(PlaceOrderParams(
 *     dhanClientId = "1000000132",
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
 *
 * // Get all orders
 * val orders = dhan.getOrdersApi().getOrderBook()
 * orders.forEach { order ->
 *     println("${order.orderId}: ${order.tradingSymbol} - ${order.orderStatus}")
 * }
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
 */
class OrdersApi internal constructor(
    private val apiClient: ApiClient,
    private val config: DhanConfig
) {

    /**
     * Places a single order.
     *
     * Supports all order types including LIMIT, MARKET, STOP_LOSS, STOP_LOSS_MARKET
     * across NSE, BSE, and MCX exchanges.
     *
     * Note: Requires static IP whitelisting.
     *
     * @param request Order placement parameters
     * @return [OrderAction] with order ID and status
     * @throws DhanApiException if order placement fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun placeOrder(params: PlaceOrderParams): OrderAction {
        val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
        return apiClient.post(
            endpoint = Endpoints.PLACE_ORDER,
            body = request
        )
    }

    /**
     * Places a slicing order for large quantities.
     *
     * Automatically splits the order into multiple smaller orders
     * to stay within exchange freeze limits.
     *
     * Note: Requires static IP whitelisting.
     *
     * @param params Slicing order parameters
     * @return List of [OrderAction] for each sliced order
     * @throws DhanApiException if order placement fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun placeSlicingOrder(params: SlicingOrderParams): List<OrderAction> {
        val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
        return apiClient.post(
            endpoint = Endpoints.SLICING_ORDER,
            body = request
        )
    }

    /**
     * Modifies an existing pending order.
     *
     * Only orders in PENDING or PART_TRADED status can be modified.
     * Maximum 25 modifications allowed per order.
     *
     * Note: Requires static IP whitelisting.
     *
     * @param orderId Order ID to modify
     * @param params Modification parameters
     * @return [OrderAction] with modified order status
     * @throws DhanApiException if modification fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun modifyOrder(orderId: String, params: ModifyOrderParams): OrderAction {
        val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
        return apiClient.put(
            endpoint = "${Endpoints.ORDERS}/$orderId",
            body = request
        )
    }

    /**
     * Cancels a pending order.
     *
     * Only orders in PENDING status can be cancelled.
     *
     * Note: Requires static IP whitelisting.
     *
     * @param orderId Order ID to cancel
     * @return [OrderAction] with cancelled order status
     * @throws DhanApiException if cancellation fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun cancelOrder(orderId: String): OrderAction {
        return apiClient.delete(
            endpoint = "${Endpoints.ORDERS}/$orderId"
        )
    }

    /**
     * Gets all orders for the day (order book).
     *
     * Returns complete order details for all orders placed today.
     *
     * @return List of [OrderBookItem] for the day
     * @throws DhanApiException if retrieval fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun getOrderBook(): List<OrderBookItem> {
        return apiClient.get(
            endpoint = Endpoints.ORDERS
        )
    }

    /**
     * Gets details of a specific order by order ID.
     *
     * @param orderId Order ID to retrieve
     * @return [OrderBookItem] with order details
     * @throws DhanApiException if retrieval fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun getOrderById(orderId: String): OrderBookItem {
        return apiClient.get(
            endpoint = "${Endpoints.ORDERS}/$orderId"
        )
    }

    /**
     * Gets order details by correlation ID.
     *
     * Retrieves order using the user-provided tracking identifier.
     *
     * @param correlationId User-provided tracking ID
     * @return [OrderBookItem] with order details
     * @throws DhanApiException if retrieval fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun getOrderByCorrelationId(correlationId: String): OrderBookItem {
        return apiClient.get(
            endpoint = "${Endpoints.ORDERS_EXTERNAL}/$correlationId"
        )
    }

    /**
     * Gets all trades for the day (trade book).
     *
     * Returns all executed trades for today.
     *
     * @return List of [TradeBookItem] for the day
     * @throws DhanApiException if retrieval fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun getTradeBook(): List<TradeBookItem> {
        return apiClient.get(
            endpoint = Endpoints.TRADES
        )
    }

    /**
     * Gets trades for a specific order.
     *
     * @param orderId Order ID to get trades for
     * @return List of [TradeBookItem] for the order
     * @throws DhanApiException if retrieval fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
     */
    fun getTradesByOrderId(orderId: String): List<TradeBookItem> {
        return apiClient.get(
            endpoint = "${Endpoints.TRADES}/$orderId"
        )
    }

    internal object Endpoints {
        const val ORDERS = "/orders"
        const val PLACE_ORDER = "/orders"
        const val SLICING_ORDER = "/orders/slicing"
        const val ORDERS_EXTERNAL = "/orders/external"
        const val TRADES = "/trades"
    }
}
