package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.common.*
import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig

/**
 * Modifies a Super Order leg via API.
 */
@JvmSynthetic
internal fun executeModifySuperOrder(apiClient: ApiClient, config: DhanConfig, orderId: String, params: ModifySuperOrderParams): OrderAction {
    val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
    return apiClient.put(
        endpoint = "/super/orders/$orderId",
        body = request
    )
}

/**
 * Request parameters for modifying a Super Order.
 *
 * Use legName to specify which leg to modify: ENTRY_LEG, TARGET_LEG, or STOP_LOSS_LEG.
 *
 * @property dhanClientId User identification. Auto-injected from config if not provided.
 * @property legName Leg to modify: ENTRY_LEG, TARGET_LEG, or STOP_LOSS_LEG.
 * @property orderType Order type: LIMIT or MARKET.
 * @property quantity Quantity to be modified.
 * @property price Price to be modified for entry leg.
 * @property targetPrice Target price for profit booking leg.
 * @property stopLossPrice Stop loss price for stop loss leg.
 * @property trailingJump Trailing stop loss jump value in absolute terms.
 * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
 */
@GenerateBuilder
data class ModifySuperOrderParams(
    @JsonProperty("dhanClientId")
    val dhanClientId: String? = null,

    @JsonProperty("legName")
    val legName: LegName,

    @JsonProperty("orderType")
    val orderType: OrderType? = null,

    @JsonProperty("quantity")
    val quantity: Int? = null,

    @JsonProperty("price")
    val price: Double? = null,

    @JsonProperty("targetPrice")
    val targetPrice: Double? = null,

    @JsonProperty("stopLossPrice")
    val stopLossPrice: Double? = null,

    @JsonProperty("trailingJump")
    val trailingJump: Double? = null
)
