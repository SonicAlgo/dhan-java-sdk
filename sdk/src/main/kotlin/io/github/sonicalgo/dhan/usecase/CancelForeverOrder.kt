package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.common.OrderAction
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Cancels an existing Forever Order via API.
 */
@JvmSynthetic
internal fun executeCancelForeverOrder(apiClient: ApiClient, orderId: String): OrderAction {
    return apiClient.delete(
        endpoint = "/forever/orders/$orderId"
    )
}
