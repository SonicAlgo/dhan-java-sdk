package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.config.ApiClient

// ==================== Enums ====================

/**
 * Exchange for EDIS transactions.
 *
 * @see <a href="https://dhanhq.co/docs/v2/edis/">DhanHQ EDIS</a>
 */
enum class Exchange {
    /** National Stock Exchange */
    @JsonProperty("NSE")
    NSE,

    /** Bombay Stock Exchange */
    @JsonProperty("BSE")
    BSE
}

/**
 * Segment for EDIS transactions.
 *
 * @see <a href="https://dhanhq.co/docs/v2/edis/">DhanHQ EDIS</a>
 */
enum class Segment {
    /** Equity segment */
    @JsonProperty("EQ")
    EQ
}

// ==================== Response Models ====================

/**
 * EDIS form data.
 */
data class EdisForm(
    @JsonProperty("dhanClientId")
    val dhanClientId: String? = null,

    @JsonProperty("edisFormHtml")
    val edisFormHtml: String
)

/**
 * Generates EDIS form for T-PIN entry and stock marking via API.
 */
@JvmSynthetic
internal fun executeGenerateEdisForm(apiClient: ApiClient, params: GenerateEdisFormParams): EdisForm {
    return apiClient.post(
        endpoint = "/edis/form",
        body = params
    )
}

/**
 * Request parameters for generating EDIS form.
 *
 * @property isin International Securities Identification Number
 * @property quantity Quantity of shares for EDIS transaction
 * @property exchange NSE or BSE
 * @property segment EQ
 * @property bulk Mark EDIS for entire portfolio
 */
@GenerateBuilder
data class GenerateEdisFormParams(
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
