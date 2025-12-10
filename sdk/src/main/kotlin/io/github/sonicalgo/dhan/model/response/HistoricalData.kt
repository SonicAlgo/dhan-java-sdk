package io.github.sonicalgo.dhan.model.response

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Historical OHLCV candle data response.
 *
 * @property open Array of opening prices per candle
 * @property high Array of highest prices per candle
 * @property low Array of lowest prices per candle
 * @property close Array of closing prices per candle
 * @property volume Array of volume traded per candle
 * @property timestamp Array of Unix epoch timestamps
 * @property openInterest Array of open interest values (when requested)
 *
 * @see <a href="https://dhanhq.co/docs/v2/historical-data/">DhanHQ Historical Data API</a>
 */
data class HistoricalData(
    @JsonProperty("open")
    val open: List<Double>,

    @JsonProperty("high")
    val high: List<Double>,

    @JsonProperty("low")
    val low: List<Double>,

    @JsonProperty("close")
    val close: List<Double>,

    @JsonProperty("volume")
    val volume: List<Int>,

    @JsonProperty("timestamp")
    val timestamp: List<Long>,

    @JsonProperty("open_interest")
    val openInterest: List<Int>? = null
)
