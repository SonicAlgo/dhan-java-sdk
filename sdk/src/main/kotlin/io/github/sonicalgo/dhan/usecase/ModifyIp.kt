package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig

/**
 * Modifies static IP configuration via API.
 * Allowed once every 7 days.
 */
@JvmSynthetic
internal fun executeModifyIp(apiClient: ApiClient, config: DhanConfig, params: SetIpParams): SetIpResult {
    val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
    return apiClient.put(
        endpoint = "/ip/modifyIP",
        body = request
    )
}
