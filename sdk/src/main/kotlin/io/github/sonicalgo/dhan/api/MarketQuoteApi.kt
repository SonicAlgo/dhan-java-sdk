package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.response.FullQuote
import io.github.sonicalgo.dhan.model.response.Ltp
import io.github.sonicalgo.dhan.model.response.MarketQuoteResponse
import io.github.sonicalgo.dhan.model.response.OhlcQuote

/**
 * API module for market quote operations.
 *
 * Provides real-time market data including LTP, OHLC, and full quotes
 * with market depth. Maximum 1000 instruments per request.
 *
 * Rate limit: 1 request per second.
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Get LTP
 * val ltp = dhan.getMarketQuoteApi().getLtp(mapOf(
 *     "NSE_EQ" to listOf(11536, 1333)
 * ))
 *
 * // Get full quote with depth
 * val quote = dhan.getMarketQuoteApi().getQuote(mapOf(
 *     "NSE_EQ" to listOf(11536)
 * ))
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/market-quote/">DhanHQ Market Quote API</a>
 */
class MarketQuoteApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Gets Last Traded Price for instruments.
     *
     * Example:
     * ```kotlin
     * val marketQuoteApi = dhan.getMarketQuoteApi()
     *
     * val ltp = marketQuoteApi.getLtp(mapOf(
     *     "NSE_EQ" to listOf(11536, 1333),
     *     "NSE_FNO" to listOf(49081)
     * ))
     * // Returns nested map: {"NSE_EQ": {"11536": Ltp(lastPrice=3456.75)}}
     * ```
     *
     * @param instruments Map of exchange segment to list of security IDs
     * @return Nested map of exchange segment to security ID to [Ltp]
     * @throws DhanApiException if request fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/market-quote/">LTP API</a>
     */
    fun getLtp(instruments: Map<String, List<Int>>): Map<String, Map<String, Ltp>> {
        val response: MarketQuoteResponse<Ltp> = apiClient.post(
            endpoint = Endpoints.LTP,
            body = instruments
        )
        return response.data ?: emptyMap()
    }

    /**
     * Gets OHLC data for instruments.
     *
     * Returns Open, High, Low, Close prices along with LTP.
     *
     * Example:
     * ```kotlin
     * val marketQuoteApi = dhan.getMarketQuoteApi()
     *
     * val ohlc = marketQuoteApi.getOhlc(mapOf(
     *     "NSE_EQ" to listOf(11536)
     * ))
     * // Returns nested map: {"NSE_EQ": {"11536": OhlcQuote(...)}}
     * ```
     *
     * @param instruments Map of exchange segment to list of security IDs
     * @return Nested map of exchange segment to security ID to [OhlcQuote]
     * @throws DhanApiException if request fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/market-quote/">OHLC API</a>
     */
    fun getOhlc(instruments: Map<String, List<Int>>): Map<String, Map<String, OhlcQuote>> {
        val response: MarketQuoteResponse<OhlcQuote> = apiClient.post(
            endpoint = Endpoints.OHLC,
            body = instruments
        )
        return response.data ?: emptyMap()
    }

    /**
     * Gets full quote data with market depth.
     *
     * Returns complete market data including OHLC, volume, OI,
     * and 5-level market depth.
     *
     * Example:
     * ```kotlin
     * val marketQuoteApi = dhan.getMarketQuoteApi()
     *
     * val quote = marketQuoteApi.getQuote(mapOf(
     *     "NSE_EQ" to listOf(11536)
     * ))
     * // Returns nested map: {"NSE_EQ": {"11536": FullQuote(...)}}
     * quote.forEach { (segment, securities) ->
     *     securities.forEach { (securityId, data) ->
     *         println("$segment/$securityId: LTP = ${data.lastPrice}")
     *         println("  Volume: ${data.volume}")
     *         data.depth?.buy?.take(3)?.forEachIndexed { i, level ->
     *             println("  Bid $i: ${level.quantity} @ ${level.price}")
     *         }
     *     }
     * }
     * ```
     *
     * @param instruments Map of exchange segment to list of security IDs
     * @return Nested map of exchange segment to security ID to [FullQuote]
     * @throws DhanApiException if request fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/market-quote/">Quote API</a>
     */
    fun getQuote(instruments: Map<String, List<Int>>): Map<String, Map<String, FullQuote>> {
        val response: MarketQuoteResponse<FullQuote> = apiClient.post(
            endpoint = Endpoints.QUOTE,
            body = instruments
        )
        return response.data ?: emptyMap()
    }

    internal object Endpoints {
        const val LTP = "/marketfeed/ltp"
        const val OHLC = "/marketfeed/ohlc"
        const val QUOTE = "/marketfeed/quote"
    }
}
