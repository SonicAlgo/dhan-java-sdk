package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.request.DailyHistoricalParams
import io.github.sonicalgo.dhan.model.request.IntradayHistoricalParams
import io.github.sonicalgo.dhan.model.response.HistoricalData

/**
 * API module for historical data operations.
 *
 * Provides daily and intraday OHLCV data. Intraday data is available
 * for up to 5 years with a maximum of 90 days per request.
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Get daily data
 * val daily = dhan.getHistoricalDataApi().getDailyData(DailyHistoricalParams(
 *     securityId = "1333",
 *     exchangeSegment = ExchangeSegment.NSE_EQ,
 *     instrument = InstrumentType.EQUITY,
 *     fromDate = "2024-01-01",
 *     toDate = "2024-02-01"
 * ))
 *
 * // Get intraday data
 * val intraday = dhan.getHistoricalDataApi().getIntradayData(IntradayHistoricalParams(
 *     securityId = "1333",
 *     exchangeSegment = ExchangeSegment.NSE_EQ,
 *     instrument = InstrumentType.EQUITY,
 *     interval = ChartInterval.MINUTE_5,
 *     fromDate = "2024-01-15 09:15:00",
 *     toDate = "2024-01-15 15:30:00"
 * ))
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/historical-data/">DhanHQ Historical Data API</a>
 */
class HistoricalDataApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Gets daily historical OHLCV data.
     *
     * Returns daily candles from instrument inception date.
     *
     * Example:
     * ```kotlin
     * val historicalApi = dhan.getHistoricalDataApi()
     *
     * val data = historicalApi.getDailyData(DailyHistoricalParams(
     *     securityId = "1333",
     *     exchangeSegment = ExchangeSegment.NSE_EQ,
     *     instrument = InstrumentType.EQUITY,
     *     fromDate = "2024-01-01",
     *     toDate = "2024-02-01"
     * ))
     *
     * data.timestamp.forEachIndexed { i, ts ->
     *     println("$ts: O=${data.open[i]}, H=${data.high[i]}, L=${data.low[i]}, C=${data.close[i]}")
     * }
     * ```
     *
     * @param params Daily historical data request parameters
     * @return [HistoricalData] with OHLCV arrays
     * @throws DhanApiException if request fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/historical-data/">Daily Historical API</a>
     */
    fun getDailyData(params: DailyHistoricalParams): HistoricalData {
        return apiClient.post(
            endpoint = Endpoints.HISTORICAL,
            body = params
        )
    }

    /**
     * Gets intraday historical OHLCV data.
     *
     * Returns minute-level candles. Maximum 90 days per request.
     * Supported intervals: 1, 5, 15, 25, 60 minutes.
     *
     * Example:
     * ```kotlin
     * val historicalApi = dhan.getHistoricalDataApi()
     *
     * val data = historicalApi.getIntradayData(IntradayHistoricalParams(
     *     securityId = "1333",
     *     exchangeSegment = ExchangeSegment.NSE_EQ,
     *     instrument = InstrumentType.EQUITY,
     *     interval = ChartInterval.MINUTE_15,
     *     fromDate = "2024-01-15 09:15:00",
     *     toDate = "2024-01-15 15:30:00"
     * ))
     * ```
     *
     * @param params Intraday historical data request parameters
     * @return [HistoricalData] with OHLCV arrays
     * @throws DhanApiException if request fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/historical-data/">Intraday Historical API</a>
     */
    fun getIntradayData(params: IntradayHistoricalParams): HistoricalData {
        return apiClient.post(
            endpoint = Endpoints.INTRADAY,
            body = params
        )
    }

    internal object Endpoints {
        const val HISTORICAL = "/charts/historical"
        const val INTRADAY = "/charts/intraday"
    }
}
