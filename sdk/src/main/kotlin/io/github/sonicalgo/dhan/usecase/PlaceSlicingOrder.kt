package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.common.*
import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig

/**
 * Places a slicing order for large quantities via API.
 */
@JvmSynthetic
internal fun executePlaceSlicingOrder(apiClient: ApiClient, config: DhanConfig, params: SlicingOrderParams): List<OrderAction> {
    val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
    return apiClient.post(
        endpoint = "/orders/slicing",
        body = request
    )
}

/**
 * Request parameters for placing a slicing order.
 *
 * Slicing orders automatically split large orders into smaller chunks
 * to stay within exchange freeze limits for F&O instruments.
 *
 * @property dhanClientId User identification. Auto-injected from config if not provided.
 * @property correlationId User/partner generated ID for tracking back.
 * @property transactionType Transaction type: BUY or SELL.
 * @property exchangeSegment Exchange segment of the instrument.
 * @property productType Product type: CNC, INTRADAY, MARGIN, MTF, CO, BO.
 * @property orderType Order type: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_MARKET.
 * @property validity Order validity: DAY or IOC.
 * @property securityId Exchange standard ID for each scrip.
 * @property quantity Total quantity to order (will be sliced into smaller orders).
 * @property disclosedQuantity Number of shares visible (must be greater than 30% of quantity).
 * @property price Price at which order is placed.
 * @property triggerPrice Price at which order is triggered. Required for stop-loss orders.
 * @property afterMarketOrder Flag for orders placed after market hours (AMO).
 * @property amoTime When AMO should be submitted: PRE_OPEN, OPEN, OPEN_30, OPEN_60.
 * @property boProfitValue Bracket Order target price change.
 * @property boStopLossValue Bracket Order stop-loss price change.
 * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
 */
@GenerateBuilder
data class SlicingOrderParams(
    @JsonProperty("dhanClientId")
    val dhanClientId: String? = null,

    @JsonProperty("correlationId")
    val correlationId: String? = null,

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
    val price: Double? = null,

    @JsonProperty("triggerPrice")
    val triggerPrice: Double? = null,

    @JsonProperty("afterMarketOrder")
    val afterMarketOrder: Boolean? = null,

    @JsonProperty("amoTime")
    val amoTime: AmoTime? = null,

    @JsonProperty("boProfitValue")
    val boProfitValue: Double? = null,

    @JsonProperty("boStopLossValue")
    val boStopLossValue: Double? = null
)
