package io.github.sonicalgo.dhan.config

import io.github.sonicalgo.core.config.HttpSdkConfig
import io.github.sonicalgo.dhan.Dhan

/**
 * Configuration for a Dhan SDK instance.
 *
 * Created via [Dhan.Builder] and holds all configuration
 * for a single SDK instance. All properties are immutable after creation except
 * [accessToken] which can be updated for token refresh scenarios.
 *
 * ## Configuration via Builder
 *
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("your-client-id")        // Required
 *     .accessToken("your-token")         // Optional at build time
 *     .loggingEnabled(true)              // Enable HTTP request/response logging
 *     .rateLimitRetries(3)               // Auto-retry on rate limit (0-5)
 *     .build()
 * ```
 *
 * ## Updating Access Token
 *
 * The access token can be updated after creation for token refresh:
 *
 * ```kotlin
 * // Token expires - get new token and update
 * val newToken = refreshToken()
 * dhan.setAccessToken(newToken)
 * ```
 *
 * @property clientId Dhan Client ID for API authentication (required)
 * @property accessToken OAuth access token (JWT), can be updated after creation
 * @property loggingEnabled Whether HTTP logging is enabled
 * @property rateLimitRetries Number of automatic retries for rate-limited requests (0-5)
 * @property connectTimeoutMs Connection timeout in milliseconds
 * @property readTimeoutMs Read timeout in milliseconds
 * @property writeTimeoutMs Write timeout in milliseconds
 * @see Dhan.Builder
 */
class DhanConfig internal constructor(
    val clientId: String,
    @Volatile var accessToken: String = "",
    override val loggingEnabled: Boolean = false,
    override val rateLimitRetries: Int = 0,
    override val connectTimeoutMs: Long = DhanConstants.CONNECT_TIMEOUT_MS,
    override val readTimeoutMs: Long = DhanConstants.READ_TIMEOUT_MS,
    override val writeTimeoutMs: Long = DhanConstants.WRITE_TIMEOUT_MS
) : HttpSdkConfig {
    init {
        require(clientId.isNotBlank()) { "clientId cannot be blank" }
        require(rateLimitRetries in 0..5) { "rateLimitRetries must be between 0 and 5" }
    }
}
