package io.github.sonicalgo.dhan.usecase

import io.github.sonicalgo.dhan.common.LegName
import io.github.sonicalgo.dhan.common.OrderAction
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Cancels a Super Order or specific leg via API.
 */
@JvmSynthetic
internal fun executeCancelSuperOrder(apiClient: ApiClient, orderId: String, legName: LegName): OrderAction {
    return apiClient.delete(
        endpoint = "/super/orders/$orderId/${legName.name}"
    )
}
