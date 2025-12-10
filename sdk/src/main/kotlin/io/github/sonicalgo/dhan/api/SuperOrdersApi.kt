package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.enums.LegName
import io.github.sonicalgo.dhan.model.request.ModifySuperOrderParams
import io.github.sonicalgo.dhan.model.request.PlaceSuperOrderParams
import io.github.sonicalgo.dhan.model.response.OrderAction
import io.github.sonicalgo.dhan.model.response.SuperOrderBookItem

/**
 * API module for Super Order operations.
 *
 * Super Orders combine entry, target, and stop loss legs into a single order
 * with trailing stop loss functionality.
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
 * // Place a Super Order
 * val response = dhan.getSuperOrdersApi().placeSuperOrder(PlaceSuperOrderParams(
 *     dhanClientId = "1000000132",
 *     transactionType = TransactionType.BUY,
 *     exchangeSegment = ExchangeSegment.NSE_EQ,
 *     productType = ProductType.CNC,
 *     orderType = OrderType.LIMIT,
 *     securityId = "1333",
 *     quantity = 10,
 *     price = 1500.0,
 *     targetPrice = 1550.0,
 *     stopLossPrice = 1400.0,
 *     trailingJump = 10.0
 * ))
 * println("Super Order placed: ${response.orderId}")
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
 */
class SuperOrdersApi internal constructor(
    private val apiClient: ApiClient,
    private val config: DhanConfig
) {

    /**
     * Places a new Super Order.
     *
     * Creates an order with entry leg, target leg, and stop loss leg
     * with trailing stop loss.
     *
     * Note: Requires static IP whitelisting.
     *
     * @param params Super Order placement parameters
     * @return [OrderAction] with order ID and status
     * @throws DhanApiException if order placement fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
     */
    fun placeSuperOrder(params: PlaceSuperOrderParams): OrderAction {
        val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
        return apiClient.post(
            endpoint = Endpoints.SUPER_ORDERS,
            body = request
        )
    }

    /**
     * Modifies a Super Order leg.
     *
     * Use legName in the request to specify which leg to modify:
     * - ENTRY_LEG: Modify entry order (when status is PENDING or PART_TRADED)
     * - TARGET_LEG: Modify target price for profit booking
     * - STOP_LOSS_LEG: Modify stop loss price and trailing jump
     *
     * Note: Requires static IP whitelisting.
     *
     * @param orderId Order ID to modify
     * @param params Modification parameters with legName specifying which leg
     * @return [OrderAction] with modified order status
     * @throws DhanApiException if modification fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
     */
    fun modifySuperOrder(orderId: String, params: ModifySuperOrderParams): OrderAction {
        val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
        return apiClient.put(
            endpoint = "${Endpoints.SUPER_ORDERS}/$orderId",
            body = request
        )
    }

    /**
     * Cancels a Super Order or specific leg.
     *
     * Cancelling the main order cancels all legs. If a specific leg is cancelled,
     * it cannot be added again.
     *
     * Note: Requires static IP whitelisting.
     *
     * @param orderId Order ID to cancel
     * @param legName Leg to cancel: ENTRY_LEG, TARGET_LEG, or STOP_LOSS_LEG
     * @return [OrderAction] with cancelled order status
     * @throws DhanApiException if cancellation fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
     */
    fun cancelSuperOrder(orderId: String, legName: LegName): OrderAction {
        return apiClient.delete(
            endpoint = "${Endpoints.SUPER_ORDERS}/$orderId/${legName.name}"
        )
    }

    /**
     * Gets all Super Orders for the day.
     *
     * Returns complete details of all Super Orders placed today.
     *
     * @return List of [SuperOrderBookItem] for the day
     * @throws DhanApiException if retrieval fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
     */
    fun getSuperOrderBook(): List<SuperOrderBookItem> {
        return apiClient.get(
            endpoint = Endpoints.SUPER_ORDERS
        )
    }

    internal object Endpoints {
        const val SUPER_ORDERS = "/super/orders"
    }
}
