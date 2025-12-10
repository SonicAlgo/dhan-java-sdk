package io.github.sonicalgo.dhan.model.request

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Request parameters for option chain data.
 *
 * @property underlyingScrip Security ID of underlying instrument from instruments list
 * @property underlyingSegment Exchange and segment identifier (optional)
 * @property expiry Option expiry date in YYYY-MM-DD format (optional)
 *
 * @see <a href="https://dhanhq.co/docs/v2/option-chain/">DhanHQ Option Chain API</a>
 */
data class OptionChainParams(
    @JsonProperty("UnderlyingScrip")
    val underlyingScrip: Int,

    @JsonProperty("UnderlyingSeg")
    val underlyingSegment: String? = null,

    @JsonProperty("Expiry")
    val expiry: String? = null
)

/**
 * Request parameters for expiry list.
 *
 * @property underlyingScrip Security ID of underlying instrument from instruments list
 * @property underlyingSegment Exchange and segment identifier (optional)
 *
 * @see <a href="https://dhanhq.co/docs/v2/option-chain/">DhanHQ Option Chain API</a>
 */
data class ExpiryListParams(
    @JsonProperty("UnderlyingScrip")
    val underlyingScrip: Int,

    @JsonProperty("UnderlyingSeg")
    val underlyingSegment: String? = null
)
