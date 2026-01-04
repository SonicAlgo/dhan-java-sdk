package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Generates T-PIN for EDIS authentication via API.
 * T-PIN is sent to the registered mobile number.
 */
@JvmSynthetic
internal fun executeGenerateTpin(apiClient: ApiClient) {
    apiClient.get<Unit>(
        endpoint = "/edis/tpin"
    )
}
