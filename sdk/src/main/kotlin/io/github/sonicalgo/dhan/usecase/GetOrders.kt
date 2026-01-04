package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.common.OrderBookItem
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Gets all orders for the day (order book) via API.
 */
@JvmSynthetic
internal fun executeGetOrders(apiClient: ApiClient): List<OrderBookItem> {
    return apiClient.get(
        endpoint = "/orders"
    )
}
