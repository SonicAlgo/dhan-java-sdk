package io.github.sonicalgo.dhan.model.response

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Option Greeks data.
 *
 * @property delta Premium change per Re 1 underlying movement
 * @property theta Time decay measurement
 * @property gamma Delta change rate
 * @property vega Volatility sensitivity (1% IV change)
 *
 * @see <a href="https://dhanhq.co/docs/v2/option-chain/">DhanHQ Option Chain API</a>
 */
data class OptionGreeks(
    @JsonProperty("delta")
    val delta: Double? = null,

    @JsonProperty("theta")
    val theta: Double? = null,

    @JsonProperty("gamma")
    val gamma: Double? = null,

    @JsonProperty("vega")
    val vega: Double? = null
)

/**
 * Option contract data (Call or Put).
 *
 * @property securityId Security ID of the option contract
 * @property greeks Option Greeks
 * @property impliedVolatility Expected stock volatility
 * @property lastPrice Last traded price
 * @property openInterest Open interest in the contract
 * @property previousClosePrice Prior day closing price
 * @property previousOpenInterest Prior day open interest
 * @property previousVolume Prior day volume
 * @property topAskPrice Best ask price
 * @property topAskQuantity Quantity at best ask
 * @property topBidPrice Best bid price
 * @property topBidQuantity Quantity at best bid
 * @property volume Current day volume
 *
 * @see <a href="https://dhanhq.co/docs/v2/option-chain/">DhanHQ Option Chain API</a>
 */
data class OptionContractData(
    @JsonProperty("security_id")
    val securityId: String? = null,

    @JsonProperty("greeks")
    val greeks: OptionGreeks? = null,

    @JsonProperty("implied_volatility")
    val impliedVolatility: Double? = null,

    @JsonProperty("last_price")
    val lastPrice: Double? = null,

    @JsonProperty("oi")
    val openInterest: Int? = null,

    @JsonProperty("previous_close_price")
    val previousClosePrice: Double? = null,

    @JsonProperty("previous_oi")
    val previousOpenInterest: Int? = null,

    @JsonProperty("previous_volume")
    val previousVolume: Int? = null,

    @JsonProperty("top_ask_price")
    val topAskPrice: Double? = null,

    @JsonProperty("top_ask_quantity")
    val topAskQuantity: Int? = null,

    @JsonProperty("top_bid_price")
    val topBidPrice: Double? = null,

    @JsonProperty("top_bid_quantity")
    val topBidQuantity: Int? = null,

    @JsonProperty("volume")
    val volume: Int? = null
)

/**
 * Strike-level option chain data.
 *
 * @property strikePrice Strike price
 * @property callOption Call option data
 * @property putOption Put option data
 *
 * @see <a href="https://dhanhq.co/docs/v2/option-chain/">DhanHQ Option Chain API</a>
 */
data class StrikeData(
    @JsonProperty("strike_price")
    val strikePrice: Double,

    @JsonProperty("ce")
    val callOption: OptionContractData? = null,

    @JsonProperty("pe")
    val putOption: OptionContractData? = null
)

/**
 * Complete option chain response.
 *
 * @property lastPrice Underlying's last traded price
 * @property optionChain Option chain data organized by strike
 *
 * @see <a href="https://dhanhq.co/docs/v2/option-chain/">DhanHQ Option Chain API</a>
 */
data class OptionChain(
    @JsonProperty("last_price")
    val lastPrice: Double,

    @JsonProperty("oc")
    val optionChain: Map<String, StrikeData>
)

/**
 * Wrapper response for expiry list endpoint.
 *
 * The Dhan API returns expiry dates wrapped in this structure:
 * ```json
 * {
 *   "data": ["2024-10-17", "2024-10-24", ...],
 *   "status": "success"
 * }
 * ```
 */
internal data class ExpiryListResponse(
    @JsonProperty("data")
    val data: List<String>? = null,

    @JsonProperty("status")
    val status: String? = null
)

/**
 * Wrapper response for option chain endpoint.
 *
 * The Dhan API returns option chain data wrapped in this structure:
 * ```json
 * {
 *   "data": {
 *     "last_price": 24964.25,
 *     "oc": { ... }
 *   }
 * }
 * ```
 */
internal data class OptionChainResponse(
    @JsonProperty("data")
    val data: OptionChain? = null
)
