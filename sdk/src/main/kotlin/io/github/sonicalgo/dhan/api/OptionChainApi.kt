package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.request.ExpiryListParams
import io.github.sonicalgo.dhan.model.request.OptionChainParams
import io.github.sonicalgo.dhan.model.response.ExpiryListResponse
import io.github.sonicalgo.dhan.model.response.OptionChain
import io.github.sonicalgo.dhan.model.response.OptionChainResponse

/**
 * API module for option chain operations.
 *
 * Provides real-time option chain data with Greeks across exchanges.
 *
 * Rate limit: 1 request per 3 seconds.
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Get expiry dates
 * val expiries = dhan.getOptionChainApi().getExpiryList(ExpiryListParams(
 *     underlyingScrip = 13 // NIFTY
 * ))
 *
 * // Get option chain
 * val chain = dhan.getOptionChainApi().getOptionChain(OptionChainParams(
 *     underlyingScrip = 13,
 *     expiry = "2024-01-25"
 * ))
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/option-chain/">DhanHQ Option Chain API</a>
 */
class OptionChainApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Gets available expiry dates for an underlying.
     *
     * Example:
     * ```kotlin
     * val optionChainApi = dhan.getOptionChainApi()
     *
     * val expiries = optionChainApi.getExpiryList(ExpiryListParams(
     *     underlyingScrip = 13 // NIFTY
     * ))
     * expiries.forEach { println(it) }
     * // Output: 2024-01-25, 2024-02-01, ...
     * ```
     *
     * @param params Expiry list request parameters
     * @return List of expiry dates in YYYY-MM-DD format
     * @throws DhanApiException if request fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/option-chain/">Expiry List API</a>
     */
    fun getExpiryList(params: ExpiryListParams): List<String> {
        val response: ExpiryListResponse = apiClient.post(
            endpoint = Endpoints.EXPIRY_LIST,
            body = params
        )
        return response.data ?: emptyList()
    }

    /**
     * Gets option chain data for an underlying.
     *
     * Returns strike-wise call and put data with Greeks,
     * implied volatility, and market data.
     *
     * Example:
     * ```kotlin
     * val optionChainApi = dhan.getOptionChainApi()
     *
     * val chain = optionChainApi.getOptionChain(OptionChainParams(
     *     underlyingScrip = 13, // NIFTY
     *     expiry = "2024-01-25"
     * ))
     *
     * println("Underlying LTP: ${chain.lastPrice}")
     * chain.optionChain.forEach { (strike, data) ->
     *     println("Strike $strike:")
     *     data.callOption?.let { println("  CE: LTP=${it.lastPrice}, IV=${it.impliedVolatility}") }
     *     data.putOption?.let { println("  PE: LTP=${it.lastPrice}, IV=${it.impliedVolatility}") }
     * }
     * ```
     *
     * @param params Option chain request parameters
     * @return [OptionChain] with strikes and options
     * @throws DhanApiException if request fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/option-chain/">Option Chain API</a>
     */
    fun getOptionChain(params: OptionChainParams): OptionChain {
        val response: OptionChainResponse = apiClient.post(
            endpoint = Endpoints.OPTION_CHAIN,
            body = params
        )
        return response.data ?: throw DhanApiException("Empty option chain response", null)
    }

    internal object Endpoints {
        const val OPTION_CHAIN = "/optionchain"
        const val EXPIRY_LIST = "/optionchain/expirylist"
    }
}
