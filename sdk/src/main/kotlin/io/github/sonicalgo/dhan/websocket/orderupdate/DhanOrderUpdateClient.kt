package io.github.sonicalgo.dhan.websocket.orderupdate

import io.github.sonicalgo.core.client.HttpClient
import io.github.sonicalgo.dhan.config.DhanConfig
import io.github.sonicalgo.dhan.config.DhanConstants
import io.github.sonicalgo.dhan.config.DhanWebSocketConfig
import io.github.sonicalgo.dhan.model.ws.*
import io.github.sonicalgo.dhan.websocket.BaseWebSocketClient
import okhttp3.OkHttpClient
import okio.ByteString
import java.util.concurrent.CopyOnWriteArrayList

/**
 * WebSocket client for real-time order updates from Dhan.
 *
 * Provides real-time notifications for order status changes and trade executions.
 * Updates are pushed by the server whenever an order's status changes.
 *
 * Features:
 * - Real-time order status updates
 * - Trade execution notifications
 * - Automatic reconnection with exponential backoff
 * - Thread-safe listener management
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Create listener
 * val listener = object : OrderUpdateListener {
 *     override fun onConnected(isReconnect: Boolean) {
 *         println("Connected to order updates!")
 *     }
 *
 *     override fun onOrderUpdate(update: OrderUpdate) {
 *         println("Order ${update.orderId}: ${update.orderStatus}")
 *         when (update.orderStatus) {
 *             OrderStatus.TRADED -> println("Order fully executed!")
 *             OrderStatus.REJECTED -> println("Order rejected: ${update.omsErrorDescription}")
 *             OrderStatus.PENDING -> println("Order pending")
 *             else -> {}
 *         }
 *     }
 *
 *     override fun onTradeUpdate(update: TradeUpdate) {
 *         println("Trade executed: ${update.tradedQuantity} @ ${update.tradedPrice}")
 *     }
 *
 *     override fun onError(error: Throwable) {
 *         println("Error: ${error.message}")
 *     }
 * }
 *
 * // Create client
 * val client = dhan.createOrderUpdateClient()
 * client.addListener(listener)
 *
 * // Connect (automatically authenticates)
 * client.connect()
 *
 * // Later: disconnect
 * client.close()
 * ```
 *
 * @see OrderUpdateListener
 * @see <a href="https://dhanhq.co/docs/v2/order-update/">DhanHQ Live Order Updates</a>
 */
class DhanOrderUpdateClient internal constructor(
    dhanConfig: DhanConfig,
    wsConfig: DhanWebSocketConfig,
    wsHttpClient: OkHttpClient
) : BaseWebSocketClient(wsHttpClient, dhanConfig, wsConfig, "DhanOrderUpdate") {

    private val listeners = CopyOnWriteArrayList<OrderUpdateListener>()

    private val objectMapper = HttpClient.objectMapper

    /**
     * Adds a listener to receive order update events.
     *
     * @param listener Listener to add
     */
    fun addListener(listener: OrderUpdateListener) {
        listeners.add(listener)
    }

    /**
     * Removes a listener.
     *
     * @param listener Listener to remove
     */
    fun removeListener(listener: OrderUpdateListener) {
        listeners.remove(listener)
    }

    /**
     * Connects to the order update WebSocket.
     *
     * Automatically sends authentication message after connection.
     *
     * @param autoReconnect Whether to automatically reconnect on disconnection (default: from config)
     */
    fun connect(autoReconnect: Boolean = wsConfig.autoReconnectEnabled) {
        initiateConnection(autoReconnect)
    }

    // ==================== BaseWebSocketClient Implementation ====================

    override fun getWebSocketUrl(): String {
        return DhanConstants.WS_ORDER_UPDATE_URL
    }

    override fun onWebSocketMessage(text: String) {
        try {
            // Try to parse as different message types
            val jsonObject: Map<String, Any?> = objectMapper.readValue(text, Map::class.java) as Map<String, Any?>

            when {
                // Connection status message
                jsonObject.containsKey("status") || jsonObject.containsKey("type") -> {
                    val status = objectMapper.readValue(text, ConnectionStatus::class.java)
                    notifyListeners { it.onConnectionStatus(status) }
                }

                // Trade update (has exchangeTradeId)
                jsonObject.containsKey("exchangeTradeId") -> {
                    val tradeUpdate = objectMapper.readValue(text, TradeUpdate::class.java)
                    notifyListeners { it.onTradeUpdate(tradeUpdate) }
                }

                // Order update (has orderId and orderStatus)
                jsonObject.containsKey("orderId") && jsonObject.containsKey("orderStatus") -> {
                    val orderUpdate = objectMapper.readValue(text, OrderUpdate::class.java)
                    notifyListeners { it.onOrderUpdate(orderUpdate) }
                }

                // Unknown message type - still try to parse as order update
                jsonObject.containsKey("orderId") -> {
                    val orderUpdate = objectMapper.readValue(text, OrderUpdate::class.java)
                    notifyListeners { it.onOrderUpdate(orderUpdate) }
                }

                // Unknown message type - log for debugging
                else -> {
                    System.err.println("DhanOrderUpdateClient: Unknown message type: $text")
                }
            }
        } catch (e: Exception) {
            notifyListeners { it.onError(e) }
        }
    }

    /**
     * Notifies all listeners with exception guarding.
     * If a listener throws, other listeners still receive the notification.
     */
    private inline fun notifyListeners(action: (OrderUpdateListener) -> Unit) {
        listeners.forEach { listener ->
            try {
                action(listener)
            } catch (e: Exception) {
                System.err.println("DhanOrderUpdateClient: Listener threw exception: ${e.message}")
            }
        }
    }

    override fun onWebSocketBinaryMessage(bytes: ByteString) {
        // Order updates use JSON, not binary
        // Convert to string and process
        onWebSocketMessage(bytes.utf8())
    }

    override fun onConnectionEstablished(isReconnect: Boolean) {
        // Validate credentials before sending auth
        if (hasCredentialsError()) {
            notifyListeners { it.onError(IllegalStateException("Credentials not set")) }
            return
        }

        // Send authentication message
        val authMessage = buildAuthMessage()
        sendTextMessage(authMessage)

        // Notify listeners
        notifyListeners { it.onConnected(isReconnect) }
    }

    override fun onWebSocketDisconnected(code: Int, reason: String) {
        notifyListeners { it.onDisconnected(code, reason) }
    }

    override fun onWebSocketReconnecting(attempt: Int, delayMs: Long) {
        notifyListeners { it.onReconnecting(attempt, delayMs) }
    }

    override fun onWebSocketError(error: Throwable) {
        notifyListeners { it.onError(error) }
    }

    /**
     * Builds the authentication message for the WebSocket.
     */
    private fun buildAuthMessage(): String {
        val authData = mapOf(
            "LoginReq" to mapOf(
                "MsgCode" to DhanConstants.ORDER_UPDATE_MSG_CODE,
                "ClientId" to dhanConfig.clientId,
                "Token" to dhanConfig.accessToken
            )
        )
        return objectMapper.writeValueAsString(authData)
    }

    /**
     * Closes the client and releases resources.
     *
     * Clears all listeners.
     */
    override fun close() {
        listeners.clear()
        super.close()
    }
}
