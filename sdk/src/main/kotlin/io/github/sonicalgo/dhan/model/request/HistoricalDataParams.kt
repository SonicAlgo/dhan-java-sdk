package io.github.sonicalgo.dhan.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.model.enums.ChartInterval
import io.github.sonicalgo.dhan.model.enums.ExchangeSegment
import io.github.sonicalgo.dhan.model.enums.InstrumentType

/**
 * Request parameters for daily historical data.
 *
 * @property securityId Exchange standard ID for each scrip
 * @property exchangeSegment Exchange and segment for data retrieval
 * @property instrument Instrument type of the scrip
 * @property expiryCode Expiry for derivatives; reference instruments docs
 * @property includeOpenInterest Open Interest data toggle for Futures & Options
 * @property fromDate Start date in YYYY-MM-DD format
 * @property toDate End date (non-inclusive) in YYYY-MM-DD format
 *
 * @see <a href="https://dhanhq.co/docs/v2/historical-data/">DhanHQ Historical Data API</a>
 */
data class DailyHistoricalParams(
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

/**
 * Request parameters for intraday historical data.
 *
 * @property securityId Exchange standard ID for each scrip
 * @property exchangeSegment Exchange and segment for data retrieval
 * @property instrument Instrument type of the scrip
 * @property interval Candle interval: MINUTE_1, MINUTE_5, MINUTE_15, MINUTE_25, or MINUTE_60
 * @property includeOpenInterest Open Interest data toggle for Futures & Options
 * @property fromDate Start datetime in YYYY-MM-DD HH:MM:SS format
 * @property toDate End datetime in YYYY-MM-DD HH:MM:SS format
 *
 * @see <a href="https://dhanhq.co/docs/v2/historical-data/">DhanHQ Historical Data API</a>
 */
data class IntradayHistoricalParams(
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
