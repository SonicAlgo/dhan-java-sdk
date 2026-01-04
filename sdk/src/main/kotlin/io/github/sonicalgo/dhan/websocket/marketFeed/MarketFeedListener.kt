package io.github.sonicalgo.dhan.websocket.marketFeed

/**
 * Listener interface for market feed WebSocket events.
 *
 * Implement this interface to receive real-time market data updates.
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
 * val listener = object : MarketFeedListener {
 *     override fun onConnected() {
 *         println("Connected to market feed")
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
 * ```
 *
 * ## Full Example with Subscription
 *
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("your-client-id")
 *     .accessToken("your-token")
 *     .build()
 *
 * val client = dhan.createMarketFeedClient()
 *
 * client.addListener(object : MarketFeedListener {
 *     override fun onConnected() {
 *         // Subscribe on connection
 *         client.subscribe(
 *             listOf(Instrument.nseEquity("1333")),
 *             FeedMode.QUOTE
 *         )
 *     }
 *
 *     override fun onReconnected() {
 *         println("Reconnected - subscriptions auto-restored")
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
 *     override fun onTickerData(data: TickerData) {
 *         println("Ticker: ${data.securityId} LTP=${data.ltp}")
 *     }
 *
 *     override fun onQuoteData(data: QuoteData) {
 *         println("Quote: ${data.securityId} LTP=${data.ltp}")
 *     }
 *
 *     override fun onSubscriptionSuccess(instruments: List<Instrument>, mode: FeedMode) {
 *         println("Subscribed to ${instruments.size} instruments in $mode mode")
 *     }
 * })
 *
 * client.connect()
 * ```
 *
 * @see MarketFeedClient
 * @see <a href="https://dhanhq.co/docs/v2/live-market-feed/">DhanHQ Live Market Feed</a>
 */
interface MarketFeedListener {

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
     *
     * Subscriptions are automatically restored if autoResubscribeEnabled is true.
     */
    fun onReconnected() {}

    /**
     * Called when attempting to reconnect after disconnection.
     *
     * @param attempt Current reconnection attempt number
     * @param delayMs Delay before next attempt in milliseconds
     */
    fun onReconnecting(attempt: Int, delayMs: Long) {}

    // ==================== Subscription Callbacks ====================

    /**
     * Called when subscription is successful.
     *
     * @param instruments List of instruments that were subscribed
     * @param mode The feed mode used for subscription
     */
    fun onSubscriptionSuccess(instruments: List<Instrument>, mode: FeedMode) {}

    /**
     * Called when subscription fails.
     *
     * @param instruments List of instruments that failed to subscribe
     * @param reason Reason for failure
     */
    fun onSubscriptionFailed(instruments: List<Instrument>, reason: String) {}

    // ==================== Data Callbacks ====================

    /**
     * Called when ticker data is received.
     *
     * Ticker mode provides basic price data: LTP, LTQ, Volume, OHLC.
     *
     * @param data Ticker packet data
     */
    fun onTickerData(data: TickerData) {}

    /**
     * Called when quote data is received.
     *
     * Quote mode provides ticker data plus market depth (best 5 bid/ask).
     *
     * @param data Quote packet data
     */
    fun onQuoteData(data: QuoteData) {}

    /**
     * Called when full data is received.
     *
     * Full mode provides quote data plus open interest information.
     *
     * @param data Full packet data
     */
    fun onFullData(data: FullData) {}

    /**
     * Called when index data is received.
     *
     * @param data Index packet data
     */
    fun onIndexData(data: IndexData) {}

    /**
     * Called when open interest data is received.
     *
     * @param data Open interest packet data
     */
    fun onOIData(data: OIData) {}

    /**
     * Called when previous close data is received.
     *
     * @param data Previous close packet data
     */
    fun onPrevCloseData(data: PrevCloseData) {}

    /**
     * Called when market status changes.
     *
     * @param data Market status data
     */
    fun onMarketStatus(data: MarketStatusData) {}
}
