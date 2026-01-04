package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.common.ExchangeSegment
import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConstants

/**
 * Gets instruments for a specific exchange segment via API.
 */
@JvmSynthetic
internal fun executeGetInstruments(apiClient: ApiClient, segment: ExchangeSegment): String {
    return apiClient.getRaw(
        endpoint = "/instrument/${segment.name}"
    )
}

/**
 * Gets the URL for the compact instruments CSV.
 */
fun getCompactCsvUrl(): String = "${DhanConstants.INSTRUMENTS_URL}/api-scrip-master.csv"

/**
 * Gets the URL for the detailed instruments CSV.
 */
fun getDetailedCsvUrl(): String = "${DhanConstants.INSTRUMENTS_URL}/api-scrip-master-detailed.csv"
