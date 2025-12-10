package io.github.sonicalgo.dhan.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.model.enums.Exchange
import io.github.sonicalgo.dhan.model.enums.Segment

/**
 * Request parameters for generating EDIS form.
 *
 * @property isin International Securities Identification Number
 * @property quantity Quantity of shares for EDIS transaction
 * @property exchange NSE or BSE
 * @property segment EQ
 * @property bulk Mark EDIS for entire portfolio
 *
 * @see <a href="https://dhanhq.co/docs/v2/edis/">DhanHQ EDIS API</a>
 */
data class EdisFormParams(
    @JsonProperty("isin")
    val isin: String,

    @JsonProperty("qty")
    val quantity: Int,

    @JsonProperty("exchange")
    val exchange: Exchange,

    @JsonProperty("segment")
    val segment: Segment = Segment.EQ,

    @JsonProperty("bulk")
    val bulk: Boolean = false
)
