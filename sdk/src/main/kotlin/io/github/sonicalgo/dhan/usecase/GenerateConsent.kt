package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.config.AuthApiClient

/**
 * Executes the generate consent API call.
 *
 * @param authApiClient The auth API client for making requests
 * @param clientId The Dhan client ID
 * @param appId API Key generated from Dhan
 * @param appSecret API Secret generated from Dhan
 * @return GenerateConsentResult with consent app ID
 */
@JvmSynthetic
internal fun executeGenerateConsent(
    authApiClient: AuthApiClient,
    clientId: String,
    appId: String,
    appSecret: String
): GenerateConsentResult {
    return authApiClient.post(
        endpoint = "/app/generate-consent?client_id=$clientId",
        appId = appId,
        appSecret = appSecret
    )
}

// ==================== Response Models ====================

/**
 * Result of the generate consent API call.
 *
 * The consentAppId serves as a temporary session identifier required for
 * the subsequent browser-based login step. Users can generate up to 25
 * consent app IDs daily.
 *
 * @property consentAppId Temporary session identifier for browser login
 * @property consentAppStatus Status of the consent app (e.g., "GENERATED")
 * @property status Overall API status (e.g., "success")
 */
data class GenerateConsentResult(
    @JsonProperty("consentAppId")
    val consentAppId: String,

    @JsonProperty("consentAppStatus")
    val consentAppStatus: String,

    @JsonProperty("status")
    val status: String
)
