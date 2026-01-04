package io.github.sonicalgo.dhan.websocket.marketFeed

import io.github.sonicalgo.dhan.config.DhanConfig
import io.github.sonicalgo.dhan.config.DhanConstants
import io.github.sonicalgo.dhan.config.DhanWebSocketConfig
import io.github.sonicalgo.dhan.websocket.BaseWebSocketClient
import okhttp3.OkHttpClient
import okio.ByteString
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * WebSocket client for live market feed data from Dhan.
 *
 * Provides real-time market data including price updates, market depth,
 * and open interest for subscribed instruments.
 *
 * Features:
 * - Multiple subscription modes: TICKER, QUOTE, FULL
 * - Automatic reconnection with exponential backoff
 * - Thread-safe subscription management
 * - Support for up to 5000 instruments per connection
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Create listener
 * val listener = object : MarketFeedListener {
 *     override fun onConnected() {
 *         println("Connected!")
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
 *     override fun onTickerData(data: TickerData) {
 *         println("${data.securityId}: LTP=${data.ltp}")
 *     }
 * }
 *
 * // Create client
 * val client = dhan.createMarketFeedClient()
 * client.addListener(listener)
 *
 * // Connect
 * client.connect()
 *
 * // Subscribe to instruments
 * client.subscribe(
 *     listOf(
 *         Instrument.nseEquity("1333"),  // HDFC Bank
 *         Instrument.nseEquity("11536")  // TCS
 *     ),
 *     FeedMode.TICKER
 * )
 *
 * // Later: change mode
 * client.subscribe(
 *     listOf(Instrument.nseEquity("1333")),
 *     FeedMode.QUOTE
 * )
 *
 * // Unsubscribe
 * client.unsubscribe(listOf(Instrument.nseEquity("11536")))
 *
 * // Disconnect
 * client.close()
 * ```
 *
 * @see MarketFeedListener
 * @see <a href="https://dhanhq.co/docs/v2/live-market-feed/">DhanHQ Live Market Feed</a>
 */
class MarketFeedClient internal constructor(
    dhanConfig: DhanConfig,
    wsConfig: DhanWebSocketConfig,
    wsHttpClient: OkHttpClient
) : BaseWebSocketClient(wsHttpClient, dhanConfig, wsConfig, "MarketFeed") {

    private val listeners = CopyOnWriteArrayList<MarketFeedListener>()

    // Track subscriptions: instrument -> current mode
    private val subscriptions = ConcurrentHashMap<Instrument, FeedMode>()

    // Track pending subscriptions for resubscription after reconnect
    private val pendingSubscriptions = ConcurrentHashMap<Instrument, FeedMode>()

    /**
     * Adds a listener to receive market feed events.
     *
     * @param listener Listener to add
     */
    fun addListener(listener: MarketFeedListener) {
        listeners.add(listener)
    }

    /**
     * Removes a listener.
     *
     * @param listener Listener to remove
     */
    fun removeListener(listener: MarketFeedListener) {
        listeners.remove(listener)
    }

    /**
     * Connects to the market feed WebSocket.
     *
     * @param autoReconnect Whether to automatically reconnect on disconnection (default: from config)
     */
    fun connect(autoReconnect: Boolean = wsConfig.autoReconnectEnabled) {
        initiateConnection(autoReconnect)
    }

    /**
     * Subscribes to market data for the specified instruments.
     *
     * Example:
     * ```kotlin
     * // Subscribe to NSE Equity stocks
     * client.subscribe(
     *     listOf(
     *         Instrument.nseEquity("1333"),   // HDFC Bank
     *         Instrument.nseEquity("11536"),  // TCS
     *         Instrument.nseEquity("2885")    // Reliance
     *     ),
     *     FeedMode.TICKER
     * )
     *
     * // Subscribe to index
     * client.subscribe(
     *     listOf(Instrument.index("26000")),  // NIFTY 50
     *     FeedMode.QUOTE
     * )
     *
     * // Subscribe to F&O
     * client.subscribe(
     *     listOf(Instrument.nseFno("43225")), // NIFTY futures
     *     FeedMode.FULL
     * )
     * ```
     *
     * Instruments already subscribed will have their mode updated.
     * Maximum 100 instruments per subscription request.
     * Maximum 5000 total instruments per connection.
     *
     * @param instruments List of instruments to subscribe (use [Instrument] factory methods)
     * @param mode Subscription mode: [FeedMode.TICKER], [FeedMode.QUOTE], or [FeedMode.FULL]
     * @return true if subscription request was sent, false if not connected
     * @see Instrument.nseEquity
     * @see Instrument.nseFno
     * @see Instrument.index
     */
    fun subscribe(instruments: List<Instrument>, mode: FeedMode): Boolean {
        if (!isConnected) {
            // Store for subscription after connect
            instruments.forEach { pendingSubscriptions[it] = mode }
            return false
        }

        if (instruments.isEmpty()) return true

        // Check total subscription limit
        val newInstruments = instruments.filter { !subscriptions.containsKey(it) }
        if (subscriptions.size + newInstruments.size > DhanConstants.WEBSOCKET_MAX_INSTRUMENTS_PER_CONNECTION) {
            val reason = "Maximum instruments per connection (${DhanConstants.WEBSOCKET_MAX_INSTRUMENTS_PER_CONNECTION}) exceeded"
            notifyListeners { it.onSubscriptionFailed(instruments, reason) }
            notifyListeners { it.onError(IllegalStateException(reason)) }
            return false
        }

        // Update subscription tracking
        instruments.forEach { subscriptions[it] = mode }

        // Send in batches of 100 using JSON format (v2 API)
        instruments.chunked(DhanConstants.WEBSOCKET_MAX_INSTRUMENTS_PER_SUBSCRIPTION).forEach { batch ->
            val json = buildSubscriptionJson(mode.code, batch)
            sendTextMessage(json)
        }

        // Notify listeners of subscription success
        notifyListeners { it.onSubscriptionSuccess(instruments, mode) }

        return true
    }

    /**
     * Unsubscribes from market data for the specified instruments.
     *
     * @param instruments List of instruments to unsubscribe
     * @return true if unsubscription request was sent, false if not connected
     */
    fun unsubscribe(instruments: List<Instrument>): Boolean {
        if (!isConnected) return false
        if (instruments.isEmpty()) return true

        // Get the current mode for each instrument to send proper unsubscribe
        val modeGroups = instruments.groupBy { subscriptions[it] }

        modeGroups.forEach { (mode, instrumentList) ->
            if (mode != null) {
                instrumentList.chunked(DhanConstants.WEBSOCKET_MAX_INSTRUMENTS_PER_SUBSCRIPTION).forEach { batch ->
                    val json = buildSubscriptionJson(mode.unsubscribeCode, batch)
                    sendTextMessage(json)
                }
            }
        }

        // Remove from tracking
        instruments.forEach {
            subscriptions.remove(it)
            pendingSubscriptions.remove(it)
        }

        return true
    }

    /**
     * Unsubscribes from all instruments.
     *
     * @return true if request was sent, false if not connected
     */
    fun unsubscribeAll(): Boolean {
        return unsubscribe(subscriptions.keys.toList())
    }

    /**
     * Gets the list of currently subscribed instruments.
     *
     * @return Map of instruments to their subscription modes
     */
    fun getSubscriptions(): Map<Instrument, FeedMode> = subscriptions.toMap()

    /**
     * Gets the count of currently subscribed instruments.
     */
    val subscriptionCount: Int
        get() = subscriptions.size

    // ==================== BaseWebSocketClient Implementation ====================

    override fun getWebSocketUrl(): String {
        return "${DhanConstants.WS_MARKET_FEED_URL}?version=2&token=${dhanConfig.accessToken}&clientId=${dhanConfig.clientId}&authType=2"
    }

    override fun onWebSocketMessage(text: String) {
        // Market feed primarily uses binary messages
        // Text messages may contain error information or connection responses
        if (text.isNotBlank()) {
            // Check if it's an error message
            val lowerText = text.lowercase()
            if (lowerText.contains("error") || lowerText.contains("invalid") ||
                lowerText.contains("unauthorized") || lowerText.contains("failed")) {
                notifyListeners { it.onError(RuntimeException("Server message: $text")) }
            }
        }
    }

    override fun onWebSocketBinaryMessage(bytes: ByteString) {
        BinaryPacketParser.parse(bytes, object : MarketFeedListener {
            // Mandatory callbacks (not used by parser, but required by interface)
            override fun onConnected() {}
            override fun onDisconnected(code: Int, reason: String) {}
            override fun onError(error: Throwable) {
                notifyListeners { it.onError(error) }
            }

            // Data callbacks used by parser
            override fun onTickerData(data: TickerData) {
                notifyListeners { it.onTickerData(data) }
            }

            override fun onQuoteData(data: QuoteData) {
                notifyListeners { it.onQuoteData(data) }
            }

            override fun onFullData(data: FullData) {
                notifyListeners { it.onFullData(data) }
            }

            override fun onIndexData(data: IndexData) {
                notifyListeners { it.onIndexData(data) }
            }

            override fun onOIData(data: OIData) {
                notifyListeners { it.onOIData(data) }
            }

            override fun onPrevCloseData(data: PrevCloseData) {
                notifyListeners { it.onPrevCloseData(data) }
            }

            override fun onMarketStatus(data: MarketStatusData) {
                notifyListeners { it.onMarketStatus(data) }
            }
        })
    }

    /**
     * Notifies all listeners with exception guarding.
     * If a listener throws, the error is reported via onError and other listeners still receive the notification.
     */
    private inline fun notifyListeners(action: (MarketFeedListener) -> Unit) {
        listeners.forEach { listener ->
            try {
                action(listener)
            } catch (e: Exception) {
                try {
                    listener.onError(e)
                } catch (_: Exception) {
                    // Ignore errors from error handler itself
                }
            }
        }
    }

    override fun onConnectionEstablished(isReconnect: Boolean) {
        // Validate credentials before proceeding
        if (hasCredentialsError()) {
            notifyListeners { it.onError(IllegalStateException("Credentials not set")) }
            return
        }

        // Notify listeners (separate callbacks for connect vs reconnect)
        if (isReconnect) {
            notifyListeners { it.onReconnected() }
        } else {
            notifyListeners { it.onConnected() }
        }

        // Resubscribe to instruments if this is a reconnection and auto-resubscribe is enabled
        if (isReconnect && wsConfig.autoResubscribeEnabled && subscriptions.isNotEmpty()) {
            // Group by mode and resubscribe
            val modeGroups = subscriptions.entries.groupBy({ it.value }, { it.key })
            modeGroups.forEach { (mode, instrumentList) ->
                subscribe(instrumentList, mode)
            }
        }

        // Subscribe to pending subscriptions (thread-safe)
        processPendingSubscriptions()
    }

    /**
     * Processes pending subscriptions atomically to avoid race conditions.
     */
    @Synchronized
    private fun processPendingSubscriptions() {
        if (pendingSubscriptions.isEmpty()) return

        val pending = pendingSubscriptions.toMap()
        pendingSubscriptions.clear()

        val modeGroups = pending.entries.groupBy({ it.value }, { it.key })
        modeGroups.forEach { (mode, instrumentList) ->
            subscribe(instrumentList, mode)
        }
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
     * Closes the client and releases resources.
     *
     * Sends disconnect message per Dhan API spec before closing,
     * then clears all subscriptions and listeners.
     */
    override fun close() {
        // Send disconnect request per Dhan API spec (v2)
        val wasConnected = isConnected
        if (wasConnected) {
            sendTextMessage("""{"RequestCode":12}""")
        }

        // Clear subscriptions
        subscriptions.clear()
        pendingSubscriptions.clear()

        // Close the WebSocket
        super.close()

        // Notify listeners of disconnect (after close, before clearing listeners)
        if (wasConnected) {
            notifyListeners { it.onDisconnected(1000, "Client closed") }
        }

        // Clear listeners last
        listeners.clear()
    }

    // ==================== JSON Subscription Helpers ====================

    /**
     * Builds a JSON subscription message for v2 WebSocket API.
     *
     * Format:
     * ```json
     * {
     *   "RequestCode": 15,
     *   "InstrumentCount": 2,
     *   "InstrumentList": [
     *     {"ExchangeSegment": "NSE_EQ", "SecurityId": "1333"},
     *     {"ExchangeSegment": "NSE_EQ", "SecurityId": "11536"}
     *   ]
     * }
     * ```
     */
    private fun buildSubscriptionJson(requestCode: Int, instruments: List<Instrument>): String {
        val instrumentsJson = instruments.joinToString(",") { inst ->
            """{"ExchangeSegment":"${getExchangeSegmentString(inst.exchangeSegment)}","SecurityId":"${inst.securityId}"}"""
        }
        return """{"RequestCode":$requestCode,"InstrumentCount":${instruments.size},"InstrumentList":[$instrumentsJson]}"""
    }

    /**
     * Converts exchange segment code to string representation for v2 API.
     */
    private fun getExchangeSegmentString(code: Int): String = when (code) {
        DhanConstants.ExchangeSegmentCode.IDX_I -> "IDX_I"
        DhanConstants.ExchangeSegmentCode.NSE_EQ -> "NSE_EQ"
        DhanConstants.ExchangeSegmentCode.NSE_FNO -> "NSE_FNO"
        DhanConstants.ExchangeSegmentCode.NSE_CURRENCY -> "NSE_CURRENCY"
        DhanConstants.ExchangeSegmentCode.BSE_EQ -> "BSE_EQ"
        DhanConstants.ExchangeSegmentCode.MCX_COMM -> "MCX_COMM"
        DhanConstants.ExchangeSegmentCode.BSE_CURRENCY -> "BSE_CURRENCY"
        DhanConstants.ExchangeSegmentCode.BSE_FNO -> "BSE_FNO"
        else -> code.toString()
    }
}
