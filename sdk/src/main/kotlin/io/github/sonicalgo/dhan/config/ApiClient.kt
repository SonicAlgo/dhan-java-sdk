package io.github.sonicalgo.dhan.config

import io.github.sonicalgo.core.client.HttpClient
import io.github.sonicalgo.core.client.HttpClientProvider
import io.github.sonicalgo.core.exception.SdkException
import io.github.sonicalgo.dhan.exception.DhanApiException

/**
 * HTTP client for making API requests to Dhan.
 *
 * Extends [HttpClient] with Dhan-specific error handling.
 *
 * @property config The configuration for this SDK instance
 * @property clientProvider The HTTP client provider for this SDK instance
 */
internal class ApiClient(
    config: DhanConfig,
    clientProvider: HttpClientProvider
) : HttpClient(
    baseUrl = DhanConstants.BASE_URL,
    config = config,
    clientProvider = clientProvider
) {

    /**
     * Handles API error responses by throwing [DhanApiException].
     */
    override fun handleError(responseBody: String, statusCode: Int): Nothing {
        throw DhanApiException(responseBody, statusCode)
    }

    /**
     * Creates a network exception as [DhanApiException].
     */
    override fun createNetworkException(e: Exception): SdkException {
        return DhanApiException("Network error: ${e.message}", null, e)
    }
}
