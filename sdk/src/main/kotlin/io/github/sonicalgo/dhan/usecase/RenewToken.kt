package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Renews the access token via API.
 * Extends token validity for another 24 hours.
 */
@JvmSynthetic
internal fun executeRenewToken(apiClient: ApiClient) {
    apiClient.post<Unit>(
        endpoint = "/RenewToken"
    )
}
