package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.common.FullQuote
import io.github.sonicalgo.dhan.common.MarketQuoteResponse
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Gets full quote data with market depth via API.
 */
@JvmSynthetic
internal fun executeGetQuote(apiClient: ApiClient, instruments: Map<String, List<Int>>): Map<String, Map<String, FullQuote>> {
    val response: MarketQuoteResponse<FullQuote> = apiClient.post(
        endpoint = "/marketfeed/quote",
        body = instruments
    )
    return response.data ?: emptyMap()
}
