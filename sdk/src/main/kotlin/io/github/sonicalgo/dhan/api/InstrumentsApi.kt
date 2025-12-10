package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConstants
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.enums.ExchangeSegment

/**
 * API module for instrument master data.
 *
 * Provides access to instrument lists for all exchange segments.
 * Instrument data includes security IDs, symbols, lot sizes, and trading parameters.
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Get compact CSV URL
 * val csvUrl = dhan.getInstrumentsApi().getCompactCsvUrl()
 *
 * // Get segment-specific instruments
 * val instruments = dhan.getInstrumentsApi().getInstruments(ExchangeSegment.NSE_EQ)
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/instruments/">DhanHQ Instruments API</a>
 */
class InstrumentsApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Gets the URL for the compact instruments CSV.
     *
     * Contains essential instrument data for all segments.
     *
     * @return URL to download compact CSV
     *
     * @see <a href="https://dhanhq.co/docs/v2/instruments/">Instruments API</a>
     */
    fun getCompactCsvUrl(): String = COMPACT_CSV_URL

    /**
     * Gets the URL for the detailed instruments CSV.
     *
     * Contains comprehensive instrument data including margin
     * requirements and trading parameters.
     *
     * @return URL to download detailed CSV
     *
     * @see <a href="https://dhanhq.co/docs/v2/instruments/">Instruments API</a>
     */
    fun getDetailedCsvUrl(): String = DETAILED_CSV_URL

    /**
     * Gets instruments for a specific exchange segment.
     *
     * Returns instrument data as raw string (CSV/JSON based on API response).
     *
     * Example:
     * ```kotlin
     * val instrumentsApi = dhan.getInstrumentsApi()
     *
     * val nseEquity = instrumentsApi.getInstruments(ExchangeSegment.NSE_EQ)
     * val nseFno = instrumentsApi.getInstruments(ExchangeSegment.NSE_FNO)
     * ```
     *
     * @param segment Exchange segment to fetch
     * @return Raw instrument data string
     * @throws DhanApiException if request fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/instruments/">Instruments API</a>
     */
    fun getInstruments(segment: ExchangeSegment): String {
        return apiClient.getRaw(
            endpoint = "/instrument/${segment.name}"
        )
    }

    companion object {
        /** URL for compact instruments CSV. */
        const val COMPACT_CSV_URL = "${DhanConstants.INSTRUMENTS_URL}/api-scrip-master.csv"

        /** URL for detailed instruments CSV. */
        const val DETAILED_CSV_URL = "${DhanConstants.INSTRUMENTS_URL}/api-scrip-master-detailed.csv"
    }
}
