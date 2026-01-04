package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig

// ==================== Enums ====================

/**
 * IP flag for static IP configuration.
 *
 * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
 */
enum class IpFlag {
    /** Primary IP address */
    @JsonProperty("PRIMARY")
    PRIMARY,

    /** Secondary IP address */
    @JsonProperty("SECONDARY")
    SECONDARY
}

// ==================== Response Models ====================

/**
 * Response from set IP operation.
 */
data class SetIpResult(
    @JsonProperty("message")
    val message: String,

    @JsonProperty("status")
    val status: String
)

/**
 * Sets static IP for order API whitelisting via API.
 */
@JvmSynthetic
internal fun executeSetIp(apiClient: ApiClient, config: DhanConfig, params: SetIpParams): SetIpResult {
    val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
    return apiClient.post(
        endpoint = "/ip/setIP",
        body = request
    )
}

/**
 * Request parameters for setting static IP.
 *
 * @property dhanClientId Optional. Auto-injected from config if not provided.
 * @property ip IPv4 or IPv6 address
 * @property ipFlag PRIMARY or SECONDARY
 */
@GenerateBuilder
data class SetIpParams(
    @JsonProperty("dhanClientId")
    val dhanClientId: String? = null,

    @JsonProperty("ip")
    val ip: String,

    @JsonProperty("ipFlag")
    val ipFlag: IpFlag
)
