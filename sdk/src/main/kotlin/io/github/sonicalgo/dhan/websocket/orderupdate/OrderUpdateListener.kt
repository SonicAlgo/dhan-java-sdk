package io.github.sonicalgo.dhan.websocket.orderupdate

import io.github.sonicalgo.dhan.model.ws.*

/**
 * Listener interface for order update WebSocket events.
 *
 * Implement this interface to receive real-time order and trade updates.
 * All callbacks are invoked on a background thread, so perform any
 * UI updates on the appropriate thread.
 *
 * ## Basic Usage
 *
 * ```kotlin
 * val listener = object : OrderUpdateListener {
 *     override fun onConnected(isReconnect: Boolean) {
 *         println("Connected to order updates")
 *     }
 *
 *     override fun onOrderUpdate(update: OrderUpdate) {
 *         println("Order ${update.orderId}: ${update.orderStatus}")
 *         if (update.hasError) {
 *             println("Error: ${update.omsErrorDescription}")
 *         }
 *     }
 *
 *     override fun onTradeUpdate(update: TradeUpdate) {
 *         println("Trade: ${update.tradedQuantity} @ ${update.tradedPrice}")
 *     }
 *
 *     override fun onError(error: Throwable) {
 *         println("Error: ${error.message}")
 *     }
 * }
 * ```
 *
 * ## Full Example with Order Status Handling
 *
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("your-client-id")
 *     .accessToken("your-token")
 *     .build()
 *
 * val client = dhan.createOrderUpdateClient()
 *
 * client.addListener(object : OrderUpdateListener {
 *     override fun onConnected(isReconnect: Boolean) {
 *         println("Order update connection established")
 *     }
 *
 *     override fun onOrderUpdate(update: OrderUpdate) {
 *         when (update.orderStatus) {
 *             "PENDING" -> println("Order ${update.orderId} pending")
 *             "TRADED" -> println("Order ${update.orderId} fully executed")
 *             "PART_TRADED" -> println("Order ${update.orderId} partially filled: ${update.tradedQuantity}/${update.quantity}")
 *             "REJECTED" -> println("Order ${update.orderId} rejected: ${update.omsErrorDescription}")
 *             "CANCELLED" -> println("Order ${update.orderId} cancelled")
 *             else -> println("Order ${update.orderId}: ${update.orderStatus}")
 *         }
 *     }
 *
 *     override fun onTradeUpdate(update: TradeUpdate) {
 *         println("Trade executed for order ${update.orderId}")
 *         println("  ${update.tradedQuantity} shares @ ${update.tradedPrice}")
 *         println("  Exchange Trade ID: ${update.exchangeTradeId}")
 *     }
 *
 *     override fun onConnectionStatus(status: ConnectionStatus) {
 *         println("Connection status: ${status.status}")
 *     }
 *
 *     override fun onReconnecting(attempt: Int, delayMs: Long) {
 *         println("Reconnecting attempt $attempt in ${delayMs}ms")
 *     }
 *
 *     override fun onError(error: Throwable) {
 *         System.err.println("Order update error: ${error.message}")
 *     }
 * })
 *
 * client.connect()
 * ```
 *
 * @see DhanOrderUpdateClient
 * @see <a href="https://dhanhq.co/docs/v2/order-update/">DhanHQ Live Order Updates</a>
 */
interface OrderUpdateListener {

    /**
     * Called when the WebSocket connection is established.
     *
     * @param isReconnect true if this is a reconnection, false if first connection
     */
    fun onConnected(isReconnect: Boolean) {}

    /**
     * Called when the WebSocket connection is closed.
     *
     * @param code WebSocket close code
     * @param reason Close reason description
     */
    fun onDisconnected(code: Int, reason: String) {}

    /**
     * Called when attempting to reconnect after disconnection.
     *
     * @param attempt Current reconnection attempt number
     * @param delayMs Delay before next attempt in milliseconds
     */
    fun onReconnecting(attempt: Int, delayMs: Long) {}

    /**
     * Called when an error occurs.
     *
     * @param error The error that occurred
     */
    fun onError(error: Throwable) {}

    /**
     * Called when an order update is received.
     *
     * This is called for all order status changes including:
     * - Order placement confirmation
     * - Order modification confirmation
     * - Order cancellation
     * - Partial fills
     * - Complete fills
     * - Rejections
     *
     * @param update Order update data
     */
    fun onOrderUpdate(update: OrderUpdate) {}

    /**
     * Called when a trade execution update is received.
     *
     * This is called when an order is executed (partially or fully).
     *
     * @param update Trade update data
     */
    fun onTradeUpdate(update: TradeUpdate) {}

    /**
     * Called when connection status changes.
     *
     * This includes authentication success/failure messages.
     *
     * @param status Connection status data
     */
    fun onConnectionStatus(status: ConnectionStatus) {}
}
