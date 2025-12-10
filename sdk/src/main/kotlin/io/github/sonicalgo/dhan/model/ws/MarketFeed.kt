package io.github.sonicalgo.dhan.model.ws

import io.github.sonicalgo.dhan.config.DhanConstants

/**
 * Subscription mode for market feed data.
 *
 * Determines the level of data received for each instrument.
 *
 * Example:
 * ```kotlin
 * // Subscribe to basic price updates
 * client.subscribe(instruments, FeedMode.TICKER)
 *
 * // Subscribe to price with market depth
 * client.subscribe(instruments, FeedMode.QUOTE)
 *
 * // Subscribe to full data including OI
 * client.subscribe(instruments, FeedMode.FULL)
 * ```
 *
 * @property code Internal code used in subscription requests
 */
enum class FeedMode(val code: Int) {
    /**
     * Basic price updates only.
     * Returns: LTP, LTQ, Volume, Open, High, Low, Close
     */
    TICKER(DhanConstants.FeedRequestCode.SUBSCRIBE_TICKER),

    /**
     * Price with best bid/ask.
     * Returns: Ticker data + Best 5 bid/ask
     */
    QUOTE(DhanConstants.FeedRequestCode.SUBSCRIBE_QUOTE),

    /**
     * Full market depth data.
     * Returns: Quote data + Open Interest
     */
    FULL(DhanConstants.FeedRequestCode.SUBSCRIBE_FULL);

    /**
     * Gets the unsubscribe code for this mode.
     */
    val unsubscribeCode: Int
        get() = when (this) {
            TICKER -> DhanConstants.FeedRequestCode.UNSUBSCRIBE_TICKER
            QUOTE -> DhanConstants.FeedRequestCode.UNSUBSCRIBE_QUOTE
            FULL -> DhanConstants.FeedRequestCode.UNSUBSCRIBE_FULL
        }
}

/**
 * Instrument identifier for market feed subscription.
 *
 * Use the factory methods to create instruments for different exchange segments:
 * - [Instrument.nseEquity] - NSE Equity (Cash)
 * - [Instrument.nseFno] - NSE Futures & Options
 * - [Instrument.nseCurrency] - NSE Currency
 * - [Instrument.bseEquity] - BSE Equity (Cash)
 * - [Instrument.bseFno] - BSE Futures & Options
 * - [Instrument.bseCurrency] - BSE Currency
 * - [Instrument.mcxCommodity] - MCX Commodity
 * - [Instrument.index] - Index values
 *
 * ## Creating Instruments
 *
 * ```kotlin
 * // NSE Stocks
 * val hdfcBank = Instrument.nseEquity("1333")     // HDFC Bank
 * val tcs = Instrument.nseEquity("11536")         // TCS
 * val reliance = Instrument.nseEquity("2885")     // Reliance
 *
 * // NSE F&O
 * val niftyFut = Instrument.nseFno("43225")       // NIFTY futures
 * val bankNiftyOpt = Instrument.nseFno("52179")   // BANKNIFTY option
 *
 * // Indices
 * val nifty50 = Instrument.index("26000")         // NIFTY 50
 * val bankNifty = Instrument.index("26009")       // BANK NIFTY
 *
 * // MCX Commodities
 * val gold = Instrument.mcxCommodity("224035")    // Gold
 * val crude = Instrument.mcxCommodity("220822")   // Crude Oil
 * ```
 *
 * ## Subscribing to Instruments
 *
 * ```kotlin
 * val client = dhan.createMarketFeedClient()
 * client.addListener(myListener)
 * client.connect()
 *
 * // Subscribe to multiple instruments
 * client.subscribe(
 *     listOf(
 *         Instrument.nseEquity("1333"),
 *         Instrument.nseEquity("11536"),
 *         Instrument.index("26000")
 *     ),
 *     FeedMode.TICKER
 * )
 * ```
 *
 * @property exchangeSegment Exchange segment code
 * @property securityId Security ID from instruments master
 * @see <a href="https://dhanhq.co/docs/v2/instruments/">DhanHQ Instruments API</a>
 */
@ConsistentCopyVisibility
data class Instrument internal constructor(
    val exchangeSegment: Int,
    val securityId: String
) {
    companion object {
        /** Creates an instrument for NSE Equity (Cash) segment. */
        fun nseEquity(securityId: String) = Instrument(
            DhanConstants.ExchangeSegmentCode.NSE_EQ,
            securityId
        )

        /** Creates an instrument for NSE Futures & Options segment. */
        fun nseFno(securityId: String) = Instrument(
            DhanConstants.ExchangeSegmentCode.NSE_FNO,
            securityId
        )

        /** Creates an instrument for NSE Currency segment. */
        fun nseCurrency(securityId: String) = Instrument(
            DhanConstants.ExchangeSegmentCode.NSE_CURRENCY,
            securityId
        )

        /** Creates an instrument for BSE Equity (Cash) segment. */
        fun bseEquity(securityId: String) = Instrument(
            DhanConstants.ExchangeSegmentCode.BSE_EQ,
            securityId
        )

        /** Creates an instrument for BSE Futures & Options segment. */
        fun bseFno(securityId: String) = Instrument(
            DhanConstants.ExchangeSegmentCode.BSE_FNO,
            securityId
        )

        /** Creates an instrument for BSE Currency segment. */
        fun bseCurrency(securityId: String) = Instrument(
            DhanConstants.ExchangeSegmentCode.BSE_CURRENCY,
            securityId
        )

        /** Creates an instrument for MCX Commodity segment. */
        fun mcxCommodity(securityId: String) = Instrument(
            DhanConstants.ExchangeSegmentCode.MCX_COMM,
            securityId
        )

        /** Creates an instrument for Index values. */
        fun index(securityId: String) = Instrument(
            DhanConstants.ExchangeSegmentCode.IDX_I,
            securityId
        )
    }
}

/**
 * Ticker packet data (basic price updates).
 *
 * Received when subscribed to [FeedMode.TICKER].
 *
 * Example:
 * ```kotlin
 * override fun onTickerData(data: TickerData) {
 *     println("Security ${data.securityId}: LTP = ${data.ltp}")
 *     println("Last traded at: ${java.time.Instant.ofEpochSecond(data.ltt)}")
 * }
 * ```
 *
 * @property exchangeSegment Exchange segment code
 * @property securityId Security ID
 * @property ltp Last traded price
 * @property ltt Last traded time (Unix epoch seconds)
 */
data class TickerData(
    val exchangeSegment: Int,
    val securityId: String,
    val ltp: String,
    val ltt: Long
)

/**
 * Single bid/ask level in market depth.
 *
 * @property price Price at this level
 * @property quantity Quantity available at this price
 * @property orders Number of orders at this price (optional)
 */
data class DepthLevel(
    val price: String,
    val quantity: Int,
    val orders: Int = 0
)

/**
 * Quote packet data (price with OHLC and volume).
 *
 * Received when subscribed to [FeedMode.QUOTE] or [FeedMode.FULL].
 *
 * Example:
 * ```kotlin
 * override fun onQuoteData(data: QuoteData) {
 *     println("${data.securityId}: LTP=${data.ltp} Vol=${data.volume}")
 *     println("OHLC: O=${data.openPrice} H=${data.highPrice} L=${data.lowPrice} C=${data.closePrice}")
 * }
 * ```
 *
 * @property exchangeSegment Exchange segment code
 * @property securityId Security ID
 * @property ltp Last traded price
 * @property ltq Last traded quantity
 * @property ltt Last traded time (Unix epoch seconds)
 * @property avgPrice Average traded price
 * @property volume Total traded volume
 * @property totalBuyQuantity Total buy quantity in market
 * @property totalSellQuantity Total sell quantity in market
 * @property openPrice Day open price
 * @property closePrice Previous close price
 * @property highPrice Day high price
 * @property lowPrice Day low price
 */
data class QuoteData(
    val exchangeSegment: Int,
    val securityId: String,
    val ltp: String,
    val ltq: Int,
    val ltt: Long,
    val avgPrice: String,
    val volume: Long,
    val totalBuyQuantity: Long,
    val totalSellQuantity: Long,
    val openPrice: String,
    val closePrice: String,
    val highPrice: String,
    val lowPrice: String
)

/**
 * Full packet data (quote with open interest and market depth).
 *
 * Received when subscribed to [FeedMode.FULL]. Includes market depth (best 5 bid/ask).
 *
 * Example:
 * ```kotlin
 * override fun onFullData(data: FullData) {
 *     println("${data.securityId}: LTP=${data.ltp} OI=${data.openInterest}")
 *
 *     // Print market depth
 *     println("Bids:")
 *     data.bids.forEachIndexed { i, bid ->
 *         println("  ${i+1}. ${bid.quantity} @ ${bid.price}")
 *     }
 *     println("Asks:")
 *     data.asks.forEachIndexed { i, ask ->
 *         println("  ${i+1}. ${ask.quantity} @ ${ask.price}")
 *     }
 * }
 * ```
 *
 * @property exchangeSegment Exchange segment code
 * @property securityId Security ID
 * @property ltp Last traded price
 * @property ltq Last traded quantity
 * @property ltt Last traded time (Unix epoch seconds)
 * @property avgPrice Average traded price
 * @property volume Total traded volume
 * @property totalBuyQuantity Total buy quantity in market
 * @property totalSellQuantity Total sell quantity in market
 * @property openInterest Current open interest
 * @property oiDayHigh Day high open interest
 * @property oiDayLow Day low open interest
 * @property openPrice Day open price
 * @property closePrice Previous close price
 * @property highPrice Day high price
 * @property lowPrice Day low price
 * @property bids List of bid levels (best 5)
 * @property asks List of ask levels (best 5)
 */
data class FullData(
    val exchangeSegment: Int,
    val securityId: String,
    val ltp: String,
    val ltq: Int,
    val ltt: Long,
    val avgPrice: String,
    val volume: Long,
    val totalBuyQuantity: Long,
    val totalSellQuantity: Long,
    val openInterest: Long,
    val oiDayHigh: Long,
    val oiDayLow: Long,
    val openPrice: String,
    val closePrice: String,
    val highPrice: String,
    val lowPrice: String,
    val bids: List<DepthLevel>,
    val asks: List<DepthLevel>
)

/**
 * Index data packet.
 *
 * Contains index value and OHLC data.
 *
 * @property exchangeSegment Exchange segment code
 * @property securityId Security ID
 * @property indexValue Current index value
 * @property openValue Day open value
 * @property highValue Day high value
 * @property lowValue Day low value
 * @property closeValue Previous close value
 * @property changePercent Percentage change from close
 */
data class IndexData(
    val exchangeSegment: Int,
    val securityId: String,
    val indexValue: String,
    val openValue: String,
    val highValue: String,
    val lowValue: String,
    val closeValue: String,
    val changePercent: String
)

/**
 * Open Interest update data.
 *
 * @property exchangeSegment Exchange segment code
 * @property securityId Security ID
 * @property openInterest Current open interest
 */
data class OIData(
    val exchangeSegment: Int,
    val securityId: String,
    val openInterest: Long
)

/**
 * Previous close update data.
 *
 * @property exchangeSegment Exchange segment code
 * @property securityId Security ID
 * @property previousClose Previous close price
 */
data class PrevCloseData(
    val exchangeSegment: Int,
    val securityId: String,
    val previousClose: String
)

/**
 * Market status update.
 *
 * @property exchangeSegment Exchange segment code
 * @property status Market status (OPEN, CLOSED, etc.)
 */
data class MarketStatusData(
    val exchangeSegment: Int,
    val status: String
)
