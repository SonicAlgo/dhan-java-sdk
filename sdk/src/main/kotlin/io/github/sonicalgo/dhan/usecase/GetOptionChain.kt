package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.exception.DhanApiException

/**
 * Gets option chain data for an underlying via API.
 */
@JvmSynthetic
internal fun executeGetOptionChain(apiClient: ApiClient, params: GetOptionChainParams): OptionChain {
    val response: OptionChainResponse = apiClient.post(
        endpoint = "/optionchain",
        body = params
    )
    return response.data ?: throw DhanApiException("Empty option chain response", null)
}

// ==================== Params ====================

/**
 * Request parameters for option chain data.
 *
 * @property underlyingScrip Security ID of underlying instrument
 * @property underlyingSegment Exchange and segment identifier
 * @property expiry Option expiry date in YYYY-MM-DD format
 */
@GenerateBuilder
data class GetOptionChainParams(
    @JsonProperty("UnderlyingScrip")
    val underlyingScrip: Int,

    @JsonProperty("UnderlyingSeg")
    val underlyingSegment: String? = null,

    @JsonProperty("Expiry")
    val expiry: String? = null
)

// ==================== Response Models ====================

/**
 * Option chain data.
 */
data class OptionChain(
    @JsonProperty("last_price")
    val lastPrice: Double,

    @JsonProperty("oc")
    val optionChain: Map<String, OptionStrike>
)

/**
 * Option strike data.
 */
data class OptionStrike(
    @JsonProperty("ce")
    val callOption: OptionData? = null,

    @JsonProperty("pe")
    val putOption: OptionData? = null
)

/**
 * Individual option data.
 */
data class OptionData(
    @JsonProperty("last_price")
    val lastPrice: Double,

    @JsonProperty("oi")
    val openInterest: Int,

    @JsonProperty("volume")
    val volume: Int,

    @JsonProperty("iv")
    val impliedVolatility: Double? = null,

    @JsonProperty("greeks")
    val greeks: Greeks? = null
)

/**
 * Option Greeks.
 */
data class Greeks(
    @JsonProperty("delta")
    val delta: Double? = null,

    @JsonProperty("gamma")
    val gamma: Double? = null,

    @JsonProperty("theta")
    val theta: Double? = null,

    @JsonProperty("vega")
    val vega: Double? = null
)

/**
 * Internal wrapper for option chain response.
 */
internal data class OptionChainResponse(
    @JsonProperty("data")
    val data: OptionChain? = null,

    @JsonProperty("status")
    val status: String? = null
)
