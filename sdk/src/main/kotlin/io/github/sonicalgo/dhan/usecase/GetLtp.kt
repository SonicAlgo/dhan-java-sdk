package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.common.Ltp
import io.github.sonicalgo.dhan.common.MarketQuoteResponse
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Gets Last Traded Price for instruments via API.
 */
@JvmSynthetic
internal fun executeGetLtp(apiClient: ApiClient, instruments: Map<String, List<Int>>): Map<String, Map<String, Ltp>> {
    val response: MarketQuoteResponse<Ltp> = apiClient.post(
        endpoint = "/marketfeed/ltp",
        body = instruments
    )
    return response.data ?: emptyMap()
}
