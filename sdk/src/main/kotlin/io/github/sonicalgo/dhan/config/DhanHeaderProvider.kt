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
     * @throws DhanApiException if credentials are not set
     */
    override fun getHeaders(): Map<String, String> {
        val token = config.accessToken
        val clientId = config.clientId

        if (token.isBlank() || clientId.isBlank()) {
            throw DhanApiException(
                message = "Credentials not set. Ensure clientId is set via builder and call dhan.setAccessToken(token) before making API calls.",
                httpStatusCode = 0
            )
        }

        return mapOf(
            "Accept" to "application/json",
            "Content-Type" to "application/json",
            "access-token" to token,
            "client-id" to clientId
        )
    }
}
