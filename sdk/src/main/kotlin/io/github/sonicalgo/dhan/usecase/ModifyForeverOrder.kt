package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.common.*
import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig

/**
 * Modifies an existing Forever Order via API.
 */
@JvmSynthetic
internal fun executeModifyForeverOrder(apiClient: ApiClient, config: DhanConfig, orderId: String, params: ModifyForeverOrderParams): OrderAction {
    val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
    return apiClient.put(
        endpoint = "/forever/orders/$orderId",
        body = request
    )
}

/**
 * Request parameters for modifying a Forever Order.
 *
 * Use legName to specify which leg to modify: TARGET_LEG or STOP_LOSS_LEG.
 *
 * @property dhanClientId User identification. Auto-injected from config if not provided.
 * @property orderFlag Order flag: SINGLE or OCO.
 * @property orderType Order type: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_MARKET.
 * @property legName Leg to modify: TARGET_LEG or STOP_LOSS_LEG.
 * @property quantity Quantity to be modified.
 * @property price Price to be modified.
 * @property disclosedQuantity Number of shares visible (must be greater than 30% of quantity).
 * @property triggerPrice Updated trigger price for execution.
 * @property validity Order validity: DAY or IOC.
 * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
 */
@GenerateBuilder
data class ModifyForeverOrderParams(
    @JsonProperty("dhanClientId")
    val dhanClientId: String? = null,

    @JsonProperty("orderFlag")
    val orderFlag: ForeverOrderFlag,

    @JsonProperty("orderType")
    val orderType: OrderType,

    @JsonProperty("legName")
    val legName: LegName,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("price")
    val price: Double,

    @JsonProperty("disclosedQuantity")
    val disclosedQuantity: Int? = null,

    @JsonProperty("triggerPrice")
    val triggerPrice: Double,

    @JsonProperty("validity")
    val validity: Validity
)
