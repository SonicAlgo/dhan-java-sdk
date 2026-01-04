package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.common.OrderBookItem
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Gets order details by correlation ID via API.
 */
@JvmSynthetic
internal fun executeGetOrderByCorrelationId(apiClient: ApiClient, correlationId: String): OrderBookItem {
    return apiClient.get(
        endpoint = "/orders/external/$correlationId"
    )
}
