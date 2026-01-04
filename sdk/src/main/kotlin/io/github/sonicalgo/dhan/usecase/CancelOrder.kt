package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.common.OrderAction
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Cancels a pending order via API.
 */
@JvmSynthetic
internal fun executeCancelOrder(apiClient: ApiClient, orderId: String): OrderAction {
    return apiClient.delete(
        endpoint = "/orders/$orderId"
    )
}
