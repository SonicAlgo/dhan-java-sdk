package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.config.ApiClient

// ==================== Response Models ====================

/**
 * Internal wrapper for expiry list response.
 */
internal data class ExpiryListResponse(
    @JsonProperty("data")
    val data: List<String>? = null,

    @JsonProperty("status")
    val status: String? = null
)

/**
 * Gets available expiry dates for an underlying via API.
 */
@JvmSynthetic
internal fun executeGetExpiryList(apiClient: ApiClient, params: GetExpiryListParams): List<String> {
    val response: ExpiryListResponse = apiClient.post(
        endpoint = "/optionchain/expirylist",
        body = params
    )
    return response.data ?: emptyList()
}

/**
 * Request parameters for expiry list.
 *
 * @property underlyingScrip Security ID of underlying instrument
 * @property underlyingSegment Exchange and segment identifier
 */
@GenerateBuilder
data class GetExpiryListParams(
    @JsonProperty("UnderlyingScrip")
    val underlyingScrip: Int,

    @JsonProperty("UnderlyingSeg")
    val underlyingSegment: String? = null
)
