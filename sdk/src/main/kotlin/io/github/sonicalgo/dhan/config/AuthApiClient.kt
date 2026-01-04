package io.github.sonicalgo.dhan.config

import io.github.sonicalgo.core.client.HttpClient.Companion.objectMapper
import io.github.sonicalgo.dhan.exception.DhanApiException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * HTTP client for Dhan authentication endpoints (auth.dhan.co).
 *
 * This client is used for consent-based authentication flow where
 * app_id and app_secret are passed per-request rather than stored in config.
 *
 * @property config The configuration for timeouts and logging
 */
internal class AuthApiClient(private val config: DhanConfig) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
        .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
        .apply {
            if (config.loggingEnabled) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }
        .build()

    /**
     * Makes a POST request to the auth endpoint.
     *
     * @param endpoint The endpoint path (e.g., "/app/generate-consent")
     * @param appId API Key generated from Dhan
     * @param appSecret API Secret generated from Dhan
     * @param responseType The class type for response deserialization
     * @return Deserialized response object
     */
    inline fun <reified T> post(
        endpoint: String,
        appId: String,
        appSecret: String
    ): T {
        val url = "${DhanConstants.AUTH_URL}$endpoint"
        val emptyBody = "{}".toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(emptyBody)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .addHeader("app_id", appId)
            .addHeader("app_secret", appSecret)
            .build()

        return executeRequest(request)
    }

    /**
     * Makes a GET request to the auth endpoint.
     *
     * @param endpoint The endpoint path (e.g., "/app/consumeApp-consent")
     * @param appId API Key generated from Dhan
     * @param appSecret API Secret generated from Dhan
     * @param responseType The class type for response deserialization
     * @return Deserialized response object
     */
    inline fun <reified T> get(
        endpoint: String,
        appId: String,
        appSecret: String
    ): T {
        val url = "${DhanConstants.AUTH_URL}$endpoint"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", "application/json")
            .addHeader("app_id", appId)
            .addHeader("app_secret", appSecret)
            .build()

        return executeRequest(request)
    }

    /**
     * Executes an HTTP request and handles the response.
     */
    inline fun <reified T> executeRequest(request: Request): T {
        try {
            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    throw DhanApiException(responseBody, response.code)
                }

                return objectMapper.readValue(responseBody, T::class.java)
            }
        } catch (e: DhanApiException) {
            throw e
        } catch (e: Exception) {
            throw DhanApiException("Network error: ${e.message}", null, e)
        }
    }
}
