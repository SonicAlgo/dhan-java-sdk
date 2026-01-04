package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.config.AuthApiClient

/**
 * Executes the consume consent API call.
 *
 * @param authApiClient The auth API client for making requests
 * @param tokenId User-specific token obtained from browser login step
 * @param appId API Key generated from Dhan
 * @param appSecret API Secret generated from Dhan
 * @return ConsumeConsentResult with access token and user details
 */
@JvmSynthetic
internal fun executeConsumeConsent(
    authApiClient: AuthApiClient,
    tokenId: String,
    appId: String,
    appSecret: String
): ConsumeConsentResult {
    return authApiClient.get(
        endpoint = "/app/consumeApp-consent?tokenId=$tokenId",
        appId = appId,
        appSecret = appSecret
    )
}

// ==================== Response Models ====================

/**
 * Result of the consume consent API call.
 *
 * Contains the access token and user details obtained after
 * completing the browser-based login step.
 *
 * @property dhanClientId The Dhan client ID
 * @property dhanClientName The user's full name
 * @property dhanClientUcc The user's UCC (Unique Client Code)
 * @property givenPowerOfAttorney Whether the user has granted power of attorney
 * @property accessToken The JWT access token for API authentication
 * @property expiryTime Token expiry timestamp (e.g., "2025-09-23T12:37:23")
 */
data class ConsumeConsentResult(
    @JsonProperty("dhanClientId")
    val dhanClientId: String,

    @JsonProperty("dhanClientName")
    val dhanClientName: String,

    @JsonProperty("dhanClientUcc")
    val dhanClientUcc: String,

    @JsonProperty("givenPowerOfAttorney")
    val givenPowerOfAttorney: Boolean,

    @JsonProperty("accessToken")
    val accessToken: String,

    @JsonProperty("expiryTime")
    val expiryTime: String
)
