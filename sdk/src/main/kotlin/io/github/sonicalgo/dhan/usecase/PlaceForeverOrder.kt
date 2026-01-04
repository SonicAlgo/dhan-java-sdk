package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.common.*
import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig

/**
 * Places a new Forever Order (GTT) via API.
 */
@JvmSynthetic
internal fun executePlaceForeverOrder(apiClient: ApiClient, config: DhanConfig, params: PlaceForeverOrderParams): OrderAction {
    val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
    return apiClient.post(
        endpoint = "/forever/orders",
        body = request
    )
}

/**
 * Request parameters for placing a Forever Order (GTT).
 *
 * Forever Orders are Good Till Triggered orders that remain active
 * until the trigger condition is met or the order is cancelled.
 *
 * @property dhanClientId User identification. Auto-injected from config if not provided.
 * @property correlationId User/partner generated ID for tracking back.
 * @property orderFlag Order flag: SINGLE or OCO (One Cancels Other).
 * @property transactionType Transaction type: BUY or SELL.
 * @property exchangeSegment Exchange segment: NSE_EQ, NSE_FNO, BSE_EQ.
 * @property productType Product type: CNC or MTF.
 * @property orderType Order type: LIMIT or MARKET.
 * @property validity Order validity: DAY or IOC.
 * @property securityId Exchange standard ID for each scrip.
 * @property quantity Number of shares for the order.
 * @property disclosedQuantity Number of shares visible (must be greater than 30% of quantity).
 * @property price Price at which order is placed.
 * @property triggerPrice Price at which order is triggered.
 * @property price1 Target price for OCO leg (required for OCO orders).
 * @property triggerPrice1 Target trigger price for OCO leg (required for OCO orders).
 * @property quantity1 Target quantity for OCO leg (required for OCO orders).
 * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
 */
@GenerateBuilder
data class PlaceForeverOrderParams(
    @JsonProperty("dhanClientId")
    val dhanClientId: String? = null,

    @JsonProperty("correlationId")
    val correlationId: String? = null,

    @JsonProperty("orderFlag")
    val orderFlag: ForeverOrderFlag,

    @JsonProperty("transactionType")
    val transactionType: TransactionType,

    @JsonProperty("exchangeSegment")
    val exchangeSegment: ExchangeSegment,

    @JsonProperty("productType")
    val productType: ProductType,

    @JsonProperty("orderType")
    val orderType: OrderType,

    @JsonProperty("validity")
    val validity: Validity,

    @JsonProperty("securityId")
    val securityId: String,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("disclosedQuantity")
    val disclosedQuantity: Int? = null,

    @JsonProperty("price")
    val price: Double,

    @JsonProperty("triggerPrice")
    val triggerPrice: Double,

    @JsonProperty("price1")
    val price1: Double? = null,

    @JsonProperty("triggerPrice1")
    val triggerPrice1: Double? = null,

    @JsonProperty("quantity1")
    val quantity1: Int? = null
)
