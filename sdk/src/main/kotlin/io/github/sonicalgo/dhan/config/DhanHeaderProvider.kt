package io.github.sonicalgo.dhan.config

import io.github.sonicalgo.core.client.HeaderProvider
import io.github.sonicalgo.dhan.exception.DhanApiException

/**
 * Provides HTTP headers for Dhan API requests.
 *
 * Injects the following headers on every request:
 * - Accept: application/json
 * - Content-Type: application/json
 * - access-token: OAuth token from config
 * - client-id: Dhan client ID from config
 *
 * @property config The Dhan configuration containing credentials
 */
internal class DhanHeaderProvider(private val config: DhanConfig) : HeaderProvider {

    /**
     * Returns headers for Dhan API authentication.
     *
     * @return Map of header name to value
     * @throws DhanApiException if access token is not set
     */
    override fun getHeaders(): Map<String, String> {
        val token = config.accessToken
        if (token.isBlank()) {
            throw DhanApiException(
                message = "Access token not set. Call dhan.setAccessToken(token) before making API calls.",
                httpStatusCode = 0
            )
        }

        return mapOf(
            "Accept" to "application/json",
            "Content-Type" to "application/json",
            "access-token" to token,
            "client-id" to config.clientId
        )
    }
}
