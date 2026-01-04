package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Inquires EDIS approval status for a security via API.
 * Use "ALL" as isin to check status for all holdings.
 */
@JvmSynthetic
internal fun executeInquireEdisStatus(apiClient: ApiClient, isin: String): EdisStatusResponse {
    return apiClient.get(
        endpoint = "/edis/inquire/$isin"
    )
}

// ==================== Response Models ====================

/**
 * EDIS status response.
 */
data class EdisStatusResponse(
    @JsonProperty("clientId")
    val clientId: String? = null,

    @JsonProperty("isin")
    val isin: String,

    @JsonProperty("totalQty")
    val totalQuantity: Int,

    @JsonProperty("aprvdQty")
    val approvedQuantity: Int,

    @JsonProperty("status")
    val status: EdisStatus? = null,

    @JsonProperty("remarks")
    val remarks: String? = null
)

// ==================== Enums ====================

/**
 * EDIS transaction status.
 *
 * @see <a href="https://dhanhq.co/docs/v2/edis/">DhanHQ EDIS</a>
 */
enum class EdisStatus {
    /** Transaction successful */
    @JsonProperty("SUCCESS")
    SUCCESS,

    /** Transaction failed */
    @JsonProperty("FAILURE")
    FAILURE,

    /** Transaction pending */
    @JsonProperty("PENDING")
    PENDING
}
