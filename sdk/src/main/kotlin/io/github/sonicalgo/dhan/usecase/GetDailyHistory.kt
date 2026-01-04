package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.common.*
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Gets daily historical OHLCV data via API.
 */
@JvmSynthetic
internal fun executeGetDailyHistory(apiClient: ApiClient, params: GetDailyHistoryParams): HistoricalData {
    return apiClient.post(
        endpoint = "/charts/historical",
        body = params
    )
}

/**
 * Request parameters for daily historical data.
 *
 * @property securityId Exchange standard ID for each scrip.
 * @property exchangeSegment Exchange segment for data retrieval.
 * @property instrument Instrument type classification.
 * @property expiryCode Derivative expiration code (optional).
 * @property includeOpenInterest Include open interest data for F&O.
 * @property fromDate Start date in YYYY-MM-DD format.
 * @property toDate End date (non-inclusive) in YYYY-MM-DD format.
 * @see <a href="https://dhanhq.co/docs/v2/historical-data/">DhanHQ Historical Data API</a>
 */
@GenerateBuilder
data class GetDailyHistoryParams(
    @JsonProperty("securityId")
    val securityId: String,

    @JsonProperty("exchangeSegment")
    val exchangeSegment: ExchangeSegment,

    @JsonProperty("instrument")
    val instrument: InstrumentType,

    @JsonProperty("expiryCode")
    val expiryCode: Int? = null,

    @JsonProperty("oi")
    val includeOpenInterest: Boolean? = null,

    @JsonProperty("fromDate")
    val fromDate: String,

    @JsonProperty("toDate")
    val toDate: String
)
