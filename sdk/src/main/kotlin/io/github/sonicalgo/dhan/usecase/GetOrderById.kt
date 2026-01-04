package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.common.OrderBookItem
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Gets details of a specific order by order ID via API.
 */
@JvmSynthetic
internal fun executeGetOrderById(apiClient: ApiClient, orderId: String): OrderBookItem {
    return apiClient.get(
        endpoint = "/orders/$orderId"
    )
}
