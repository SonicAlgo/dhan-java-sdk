package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.common.*
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Gets intraday historical OHLCV data via API.
 */
@JvmSynthetic
internal fun executeGetIntradayHistory(apiClient: ApiClient, params: GetIntradayHistoryParams): HistoricalData {
    return apiClient.post(
        endpoint = "/charts/intraday",
        body = params
    )
}

/**
 * Request parameters for intraday historical data.
 *
 * Intraday data is limited to 90-day queries per request.
 *
 * @property securityId Exchange standard ID for each scrip.
 * @property exchangeSegment Exchange segment for data retrieval.
 * @property instrument Instrument type classification.
 * @property interval Candle interval: MINUTE_1, MINUTE_5, MINUTE_15, MINUTE_25, or MINUTE_60.
 * @property includeOpenInterest Include open interest data for F&O.
 * @property fromDate Start datetime in YYYY-MM-DD HH:MM:SS format.
 * @property toDate End datetime in YYYY-MM-DD HH:MM:SS format.
 * @see <a href="https://dhanhq.co/docs/v2/historical-data/">DhanHQ Historical Data API</a>
 */
@GenerateBuilder
data class GetIntradayHistoryParams(
    @JsonProperty("securityId")
    val securityId: String,

    @JsonProperty("exchangeSegment")
    val exchangeSegment: ExchangeSegment,

    @JsonProperty("instrument")
    val instrument: InstrumentType,

    @JsonProperty("interval")
    val interval: ChartInterval,

    @JsonProperty("oi")
    val includeOpenInterest: Boolean? = null,

    @JsonProperty("fromDate")
    val fromDate: String,

    @JsonProperty("toDate")
    val toDate: String
)
