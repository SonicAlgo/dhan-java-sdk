package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.request.ModifyForeverOrderParams
import io.github.sonicalgo.dhan.model.request.PlaceForeverOrderParams
import io.github.sonicalgo.dhan.model.response.ForeverOrderBookItem
import io.github.sonicalgo.dhan.model.response.OrderAction

/**
 * API module for Forever Order (GTT) operations.
 *
 * Forever Orders are Good Till Triggered orders that remain active
 * until the trigger condition is met or the order is cancelled.
 * Supports SINGLE and OCO (One Cancels Other) order types.
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
 * // Place a Forever Order
 * val response = dhan.getForeverOrdersApi().placeForeverOrder(PlaceForeverOrderParams(
 *     dhanClientId = "1000000132",
 *     orderFlag = ForeverOrderFlag.SINGLE,
 *     transactionType = TransactionType.BUY,
 *     exchangeSegment = ExchangeSegment.NSE_EQ,
 *     productType = ProductType.CNC,
 *     orderType = OrderType.LIMIT,
 *     validity = Validity.DAY,
 *     securityId = "1333",
 *     quantity = 10,
 *     price = 1428.0,
 *     triggerPrice = 1427.0
 * ))
 * println("Forever Order placed: ${response.orderId}")
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
 */
class ForeverOrdersApi internal constructor(
    private val apiClient: ApiClient,
    private val config: DhanConfig
) {

    /**
     * Places a new Forever Order.
     *
     * Supports SINGLE orders (triggered once) and OCO orders (One Cancels Other
     * with two legs - one for target and one for stop loss).
     *
     * Note: Requires static IP whitelisting.
     *
     * @param request Forever Order placement parameters
     * @return [OrderAction] with order ID and status
     * @throws DhanApiException if order placement fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
     */
    fun placeForeverOrder(params: PlaceForeverOrderParams): OrderAction {
        val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
        return apiClient.post(
            endpoint = Endpoints.FOREVER_ORDERS,
            body = request
        )
    }

    /**
     * Modifies an existing Forever Order.
     *
     * Allows modifying price, quantity, order type, disclosed quantity,
     * trigger price, and validity.
     *
     * Note: Requires static IP whitelisting.
     *
     * @param orderId Order ID to modify
     * @param params Modification parameters
     * @return [OrderAction] with modified order status
     * @throws DhanApiException if modification fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
     */
    fun modifyForeverOrder(orderId: String, params: ModifyForeverOrderParams): OrderAction {
        val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
        return apiClient.put(
            endpoint = "${Endpoints.FOREVER_ORDERS}/$orderId",
            body = request
        )
    }

    /**
     * Cancels an existing Forever Order.
     *
     * Note: Requires static IP whitelisting.
     *
     * @param orderId Order ID to cancel
     * @return [OrderAction] with cancelled order status
     * @throws DhanApiException if cancellation fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
     */
    fun cancelForeverOrder(orderId: String): OrderAction {
        return apiClient.delete(
            endpoint = "${Endpoints.FOREVER_ORDERS}/$orderId"
        )
    }

    /**
     * Gets all Forever Orders.
     *
     * Returns all Forever Orders (pending and executed).
     *
     * @return List of [ForeverOrderBookItem]
     * @throws DhanApiException if retrieval fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
     */
    fun getForeverOrderBook(): List<ForeverOrderBookItem> {
        return apiClient.get(
            endpoint = Endpoints.FOREVER_ALL
        )
    }

    internal object Endpoints {
        const val FOREVER_ORDERS = "/forever/orders"
        const val FOREVER_ALL = "/forever/all"
    }
}
