package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.common.MarketQuoteResponse
import io.github.sonicalgo.dhan.common.OhlcQuote
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Gets OHLC data for instruments via API.
 */
@JvmSynthetic
internal fun executeGetOhlc(apiClient: ApiClient, instruments: Map<String, List<Int>>): Map<String, Map<String, OhlcQuote>> {
    val response: MarketQuoteResponse<OhlcQuote> = apiClient.post(
        endpoint = "/marketfeed/ohlc",
        body = instruments
    )
    return response.data ?: emptyMap()
}
