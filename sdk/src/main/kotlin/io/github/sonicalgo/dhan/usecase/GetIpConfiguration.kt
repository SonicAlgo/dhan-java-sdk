package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Gets current static IP configuration via API.
 */
@JvmSynthetic
internal fun executeGetIpConfiguration(apiClient: ApiClient): IpConfiguration {
    return apiClient.get(
        endpoint = "/ip/getIP"
    )
}

// ==================== Response Models ====================

/**
 * Static IP configuration details.
 *
 * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication API</a>
 */
data class IpConfiguration(
    @JsonProperty("primaryIP")
    val primaryIp: String? = null,

    @JsonProperty("modifyDatePrimary")
    val modifyDatePrimary: String? = null,

    @JsonProperty("secondaryIP")
    val secondaryIp: String? = null,

    @JsonProperty("modifyDateSecondary")
    val modifyDateSecondary: String? = null
)
