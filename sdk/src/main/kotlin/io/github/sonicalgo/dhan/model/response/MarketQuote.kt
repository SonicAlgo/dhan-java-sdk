package io.github.sonicalgo.dhan.model.response

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * OHLC (Open, High, Low, Close) price data.
 *
 * @property open Market opening price of the day
 * @property close Market closing price of the day
 * @property high Day High price
 * @property low Day Low price
 *
 * @see <a href="https://dhanhq.co/docs/v2/market-quote/">DhanHQ Market Quote API</a>
 */
data class OhlcData(
    @JsonProperty("open")
    val open: Double,

    @JsonProperty("close")
    val close: Double,

    @JsonProperty("high")
    val high: Double,

    @JsonProperty("low")
    val low: Double
)

/**
 * Market depth level data.
 *
 * @property quantity Number of quantity at this price depth
 * @property orders Number of open orders at this price depth
 * @property price Price at which the depth stands
 *
 * @see <a href="https://dhanhq.co/docs/v2/market-quote/">DhanHQ Market Quote API</a>
 */
data class DepthLevel(
    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("orders")
    val orders: Int,

    @JsonProperty("price")
    val price: Double
)

/**
 * Market depth data with buy and sell levels.
 *
 * @property buy List of buy depth levels (best bids)
 * @property sell List of sell depth levels (best asks)
 *
 * @see <a href="https://dhanhq.co/docs/v2/market-quote/">DhanHQ Market Quote API</a>
 */
data class MarketDepth(
    @JsonProperty("buy")
    val buy: List<DepthLevel>,

    @JsonProperty("sell")
    val sell: List<DepthLevel>
)

/**
 * LTP (Last Traded Price) quote data.
 *
 * @property lastPrice LTP of the Instrument
 *
 * @see <a href="https://dhanhq.co/docs/v2/market-quote/">DhanHQ Market Quote API</a>
 */
data class Ltp(
    @JsonProperty("last_price")
    val lastPrice: Double
)

/**
 * OHLC quote data with LTP.
 *
 * @property lastPrice LTP of the Instrument
 * @property ohlc OHLC price data
 *
 * @see <a href="https://dhanhq.co/docs/v2/market-quote/">DhanHQ Market Quote API</a>
 */
data class OhlcQuote(
    @JsonProperty("last_price")
    val lastPrice: Double,

    @JsonProperty("ohlc")
    val ohlc: OhlcData
)

/**
 * Full market quote data with depth.
 *
 * @property averagePrice Volume weighted average price of the day
 * @property buyQuantity Total buy order quantity pending at the exchange
 * @property sellQuantity Total sell order quantity pending at the exchange
 * @property depth Market depth with 5 levels
 * @property lastPrice LTP of the Instrument
 * @property lastQuantity Last traded quantity
 * @property lastTradeTime Last trade timestamp
 * @property lowerCircuitLimit Current lower circuit limit
 * @property upperCircuitLimit Current upper circuit limit
 * @property netChange Absolute change in LTP from previous day closing price
 * @property ohlc OHLC price data
 * @property openInterest Open Interest in the contract (for Derivatives)
 * @property oiDayHigh Highest Open Interest for the day (NSE_FNO only)
 * @property oiDayLow Lowest Open Interest for the day (NSE_FNO only)
 * @property volume Total traded volume for the day
 *
 * @see <a href="https://dhanhq.co/docs/v2/market-quote/">DhanHQ Market Quote API</a>
 */
data class FullQuote(
    @JsonProperty("average_price")
    val averagePrice: Double? = null,

    @JsonProperty("buy_quantity")
    val buyQuantity: Int? = null,

    @JsonProperty("sell_quantity")
    val sellQuantity: Int? = null,

    @JsonProperty("depth")
    val depth: MarketDepth? = null,

    @JsonProperty("last_price")
    val lastPrice: Double,

    @JsonProperty("last_quantity")
    val lastQuantity: Int? = null,

    @JsonProperty("last_trade_time")
    val lastTradeTime: String? = null,

    @JsonProperty("lower_circuit_limit")
    val lowerCircuitLimit: Double? = null,

    @JsonProperty("upper_circuit_limit")
    val upperCircuitLimit: Double? = null,

    @JsonProperty("net_change")
    val netChange: Double? = null,

    @JsonProperty("ohlc")
    val ohlc: OhlcData? = null,

    @JsonProperty("oi")
    val openInterest: Int? = null,

    @JsonProperty("oi_day_high")
    val oiDayHigh: Int? = null,

    @JsonProperty("oi_day_low")
    val oiDayLow: Int? = null,

    @JsonProperty("volume")
    val volume: Int? = null
)

/**
 * Wrapper response for market quote endpoints.
 *
 * The Dhan API returns market quote data wrapped in this structure:
 * ```json
 * {
 *   "data": {
 *     "NSE_EQ": {
 *       "11536": {"last_price": 3238.2}
 *     }
 *   },
 *   "status": "success"
 * }
 * ```
 *
 * @property data Nested map of exchange segment -> security ID -> quote data
 * @property status API response status
 */
internal data class MarketQuoteResponse<T>(
    @JsonProperty("data")
    val data: Map<String, Map<String, T>>? = null,

    @JsonProperty("status")
    val status: String? = null
)
