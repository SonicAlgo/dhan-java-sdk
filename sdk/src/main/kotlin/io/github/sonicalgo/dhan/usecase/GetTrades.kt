package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.common.TradeBookItem
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Gets all trades for the day (trade book) via API.
 */
@JvmSynthetic
internal fun executeGetTrades(apiClient: ApiClient): List<TradeBookItem> {
    return apiClient.get(
        endpoint = "/trades"
    )
}
