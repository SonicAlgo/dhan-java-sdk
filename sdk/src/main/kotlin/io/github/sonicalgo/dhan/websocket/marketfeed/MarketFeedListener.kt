package io.github.sonicalgo.dhan.websocket.marketfeed

import io.github.sonicalgo.dhan.model.ws.*

/**
 * Listener interface for market feed WebSocket events.
 *
 * Implement this interface to receive real-time market data updates.
 * All callbacks are invoked on a background thread, so perform any
 * UI updates on the appropriate thread.
 *
 * ## Basic Usage
 *
 * ```kotlin
 * val listener = object : MarketFeedListener {
 *     override fun onConnected(isReconnect: Boolean) {
 *         println("Connected to market feed")
 *     }
 *
 *     override fun onTickerData(data: TickerData) {
 *         println("${data.securityId}: LTP=${data.ltp}")
 *     }
 *
 *     override fun onQuoteData(data: QuoteData) {
 *         println("${data.securityId}: LTP=${data.ltp}, Bids=${data.bids.size}")
 *     }
 *
 *     override fun onError(error: Throwable) {
 *         println("Error: ${error.message}")
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
 *     override fun onConnected(isReconnect: Boolean) {
 *         if (!isReconnect) {
 *             // Subscribe on first connection
 *             client.subscribe(
 *                 listOf(Instrument.nseEquity("1333")),
 *                 FeedMode.QUOTE
 *             )
 *         }
 *     }
 *
 *     override fun onTickerData(data: TickerData) {
 *         println("Ticker: ${data.securityId} LTP=${data.ltp} Vol=${data.volume}")
 *     }
 *
 *     override fun onQuoteData(data: QuoteData) {
 *         println("Quote: ${data.securityId} LTP=${data.ltp}")
 *         data.bids.forEachIndexed { i, bid ->
 *             println("  Bid $i: ${bid.quantity} @ ${bid.price}")
 *         }
 *     }
 *
 *     override fun onFullData(data: FullData) {
 *         println("Full: ${data.securityId} LTP=${data.ltp} OI=${data.oi}")
 *     }
 *
 *     override fun onReconnecting(attempt: Int, delayMs: Long) {
 *         println("Reconnecting attempt $attempt in ${delayMs}ms")
 *     }
 *
 *     override fun onError(error: Throwable) {
 *         System.err.println("Market feed error: ${error.message}")
 *     }
 * })
 *
 * client.connect()
 * ```
 *
 * @see DhanMarketFeedClient
 * @see <a href="https://dhanhq.co/docs/v2/live-market-feed/">DhanHQ Live Market Feed</a>
 */
interface MarketFeedListener {

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
