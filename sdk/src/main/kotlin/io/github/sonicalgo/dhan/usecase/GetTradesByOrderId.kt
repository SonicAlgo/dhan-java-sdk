package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.common.TradeBookItem
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Gets trades for a specific order via API.
 */
@JvmSynthetic
internal fun executeGetTradesByOrderId(apiClient: ApiClient, orderId: String): List<TradeBookItem> {
    return apiClient.get(
        endpoint = "/trades/$orderId"
    )
}
