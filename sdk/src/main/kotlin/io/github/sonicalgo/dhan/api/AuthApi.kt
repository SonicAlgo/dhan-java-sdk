package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.request.SetIpParams
import io.github.sonicalgo.dhan.model.response.IpConfiguration
import io.github.sonicalgo.dhan.model.response.SetIpResult
import io.github.sonicalgo.dhan.model.response.UserProfile

/**
 * API module for authentication and user profile operations.
 *
 * Provides methods for token management, static IP configuration,
 * and user profile retrieval.
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Get user profile
 * val profile = dhan.getAuthApi().getProfile()
 * println("Client: ${profile.dhanClientId}")
 * println("Token Valid: ${profile.tokenValidity}")
 *
 * // Configure static IP
 * dhan.getAuthApi().setIp(SetIpParams(
 *     dhanClientId = profile.dhanClientId,
 *     ip = "203.0.113.50",
 *     ipFlag = IpFlag.PRIMARY
 * ))
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication API</a>
 */
class AuthApi internal constructor(
    private val apiClient: ApiClient,
    private val config: DhanConfig
) {

    /**
     * Gets user profile and token validity.
     *
     * Returns active segments, subscription status, and token expiry.
     *
     * Example:
     * ```kotlin
     * val profile = dhan.getAuthApi().getProfile()
     * println("Client ID: ${profile.dhanClientId}")
     * println("Token Validity: ${profile.tokenValidity}")
     * println("Active Segments: ${profile.activeSegment}")
     * println("Data Plan: ${profile.dataPlan}")
     * ```
     *
     * @return [UserProfile] with user details
     * @throws DhanApiException if request fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
     */
    fun getProfile(): UserProfile {
        return apiClient.get(
            endpoint = Endpoints.PROFILE
        )
    }

    /**
     * Sets static IP for order API whitelisting.
     *
     * Required for order placement, modification, and cancellation APIs.
     *
     * Example:
     * ```kotlin
     * val response = dhan.getAuthApi().setIp(SetIpParams(
     *     dhanClientId = "1000000132",
     *     ip = "203.0.113.50",
     *     ipFlag = IpFlag.PRIMARY
     * ))
     * println(response.message)
     * ```
     *
     * @param params IP configuration parameters
     * @return [SetIpResult] with confirmation
     * @throws DhanApiException if request fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
     */
    fun setIp(params: SetIpParams): SetIpResult {
        val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
        return apiClient.post(
            endpoint = Endpoints.SET_IP,
            body = request
        )
    }

    /**
     * Modifies static IP configuration.
     *
     * Allowed once every 7 days.
     *
     * @param params IP configuration parameters
     * @return [SetIpResult] with confirmation
     * @throws DhanApiException if request fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
     */
    fun modifyIp(params: SetIpParams): SetIpResult {
        val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
        return apiClient.put(
            endpoint = Endpoints.MODIFY_IP,
            body = request
        )
    }

    /**
     * Gets current static IP configuration.
     *
     * Example:
     * ```kotlin
     * val config = dhan.getAuthApi().getIpConfiguration()
     * println("Primary IP: ${config.primaryIp}")
     * println("Secondary IP: ${config.secondaryIp}")
     * ```
     *
     * @return [IpConfiguration] with whitelisted IPs
     * @throws DhanApiException if request fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
     */
    fun getIpConfiguration(): IpConfiguration {
        return apiClient.get(
            endpoint = Endpoints.GET_IP
        )
    }

    /**
     * Renews the access token.
     *
     * Extends token validity for another 24 hours.
     *
     * @throws DhanApiException if renewal fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
     */
    fun renewToken() {
        apiClient.post<Unit>(
            endpoint = Endpoints.RENEW_TOKEN
        )
    }

    internal object Endpoints {
        const val PROFILE = "/profile"
        const val SET_IP = "/ip/setIP"
        const val MODIFY_IP = "/ip/modifyIP"
        const val GET_IP = "/ip/getIP"
        const val RENEW_TOKEN = "/RenewToken"
    }
}
