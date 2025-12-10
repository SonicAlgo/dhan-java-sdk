package io.github.sonicalgo.dhan.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.model.enums.IpFlag
import io.github.sonicalgo.dhan.Dhan

/**
 * Request parameters for setting static IP.
 *
 * @property dhanClientId Optional. Auto-injected from [Dhan] config if not provided.
 * @property ip IPv4 or IPv6 address
 * @property ipFlag PRIMARY or SECONDARY
 *
 * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
 */
data class SetIpParams(
    @JsonProperty("dhanClientId")
    val dhanClientId: String? = null,

    @JsonProperty("ip")
    val ip: String,

    @JsonProperty("ipFlag")
    val ipFlag: IpFlag
)
