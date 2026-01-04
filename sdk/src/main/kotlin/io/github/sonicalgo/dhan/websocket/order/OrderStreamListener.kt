package io.github.sonicalgo.dhan.websocket.order

/**
 * Listener interface for order stream WebSocket events.
 *
 * Implement this interface to receive real-time order and trade updates.
 * All callbacks are invoked on a background thread, so perform any
 * UI updates on the appropriate thread.
 *
 * ## Required Callbacks
 *
 * The following callbacks must be implemented:
 * - [onConnected] - Called when connection is established
 * - [onDisconnected] - Called when connection is closed
 * - [onError] - Called when an error occurs
 *
 * ## Basic Usage
 *
 * ```kotlin
 * val listener = object : OrderStreamListener {
 *     override fun onConnected() {
 *         println("Connected to order updates")
 *     }
 *
 *     override fun onDisconnected(code: Int, reason: String) {
 *         println("Disconnected: $reason")
 *     }
 *
 *     override fun onError(error: Throwable) {
 *         println("Error: ${error.message}")
 *     }
 *
 *     override fun onOrderUpdate(update: OrderUpdate) {
 *         println("Order ${update.orderId}: ${update.orderStatus}")
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
 * val client = dhan.createOrderStreamClient()
 *
 * client.addListener(object : OrderStreamListener {
 *     override fun onConnected() {
 *         println("Order update connection established")
 *     }
 *
 *     override fun onReconnected() {
 *         println("Reconnected to order updates")
 *     }
 *
 *     override fun onDisconnected(code: Int, reason: String) {
 *         println("Disconnected: $code - $reason")
 *     }
 *
 *     override fun onError(error: Throwable) {
 *         error.printStackTrace()
 *     }
 *
 *     override fun onOrderUpdate(update: OrderUpdate) {
 *         when (update.orderStatus) {
 *             "PENDING" -> println("Order ${update.orderId} pending")
 *             "TRADED" -> println("Order ${update.orderId} fully executed")
 *             "PART_TRADED" -> println("Order ${update.orderId} partially filled")
 *             "REJECTED" -> println("Order ${update.orderId} rejected: ${update.omsErrorDescription}")
 *             "CANCELLED" -> println("Order ${update.orderId} cancelled")
 *             else -> println("Order ${update.orderId}: ${update.orderStatus}")
 *         }
 *     }
 *
 *     override fun onTradeUpdate(update: TradeUpdate) {
 *         println("Trade executed for order ${update.orderId}")
 *         println("  ${update.tradedQuantity} shares @ ${update.tradedPrice}")
 *     }
 * })
 *
 * client.connect()
 * ```
 *
 * @see OrderStreamClient
 * @see <a href="https://dhanhq.co/docs/v2/order-update/">DhanHQ Live Order Updates</a>
 */
interface OrderStreamListener {

    // ==================== Mandatory Callbacks ====================

    /**
     * Called when the WebSocket connection is established for the first time.
     *
     * For reconnection events, see [onReconnected].
     */
    fun onConnected()

    /**
     * Called when the WebSocket connection is closed.
     *
     * @param code WebSocket close code
     * @param reason Close reason description
     */
    fun onDisconnected(code: Int, reason: String)

    /**
     * Called when an error occurs.
     *
     * This includes connection errors, parsing errors, and listener exceptions.
     *
     * @param error The error that occurred
     */
    fun onError(error: Throwable)

    // ==================== Optional Callbacks ====================

    /**
     * Called when the WebSocket connection is re-established after disconnection.
     */
    fun onReconnected() {}

    /**
     * Called when attempting to reconnect after disconnection.
     *
     * @param attempt Current reconnection attempt number
     * @param delayMs Delay before next attempt in milliseconds
     */
    fun onReconnecting(attempt: Int, delayMs: Long) {}

    // ==================== Data Callbacks ====================

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
}
