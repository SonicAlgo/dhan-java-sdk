package io.github.sonicalgo.dhan.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.Dhan
import io.github.sonicalgo.dhan.model.enums.*

/**
 * Request parameters for placing a single order.
 *
 * @property dhanClientId Optional. Auto-injected from [Dhan] config if not provided.
 * @property correlationId The user/partner generated id for tracking back
 * @property transactionType BUY or SELL
 * @property exchangeSegment Exchange Segment of instrument
 * @property productType CNC, INTRADAY, MARGIN, MTF, CO, BO
 * @property orderType LIMIT, MARKET, STOP_LOSS, STOP_LOSS_MARKET
 * @property validity DAY or IOC
 * @property securityId Exchange standard ID for each scrip
 * @property quantity Number of shares for the order
 * @property disclosedQuantity Number of shares visible (greater than 30% of quantity)
 * @property price Price at which order is placed
 * @property triggerPrice Price at which order is triggered (for SL orders)
 * @property afterMarketOrder Flag for orders placed after market hours
 * @property amoTime When AMO should be submitted: PRE_OPEN, OPEN, OPEN_30, OPEN_60
 * @property boProfitValue Bracket Order Target Price change
 * @property boStopLossValue Bracket Order Stop Loss Price change
 *
 * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
 */
data class PlaceOrderParams(
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

/**
 * Request parameters for placing a slicing order.
 *
 * Slicing orders automatically split large orders into smaller chunks
 * to stay within exchange freeze limits for F&O instruments.
 *
 * @property dhanClientId Optional. Auto-injected from [Dhan] config if not provided.
 * @property correlationId The user/partner generated id for tracking back
 * @property transactionType BUY or SELL
 * @property exchangeSegment Exchange Segment of instrument
 * @property productType CNC, INTRADAY, MARGIN, MTF, CO, BO
 * @property orderType LIMIT, MARKET, STOP_LOSS, STOP_LOSS_MARKET
 * @property validity DAY or IOC
 * @property securityId Exchange standard ID for each scrip
 * @property quantity Total quantity to order (will be sliced)
 * @property disclosedQuantity Number of shares visible
 * @property price Price at which order is placed
 * @property triggerPrice Price at which order is triggered (for SL orders)
 * @property afterMarketOrder Flag for orders placed after market hours
 * @property amoTime When AMO should be submitted: PRE_OPEN, OPEN, OPEN_30, OPEN_60
 * @property boProfitValue Bracket Order Target Price change
 * @property boStopLossValue Bracket Order Stop Loss Price change
 *
 * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
 */
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

/**
 * Request parameters for modifying an existing order.
 *
 * Only orders in PENDING or PART_TRADED status can be modified.
 *
 * @property dhanClientId Optional. Auto-injected from [Dhan] config if not provided.
 * @property orderId Order specific identification generated by Dhan
 * @property orderType LIMIT, MARKET, STOP_LOSS, STOP_LOSS_MARKET
 * @property legName Leg name for multi-leg orders: ENTRY_LEG, TARGET_LEG, STOP_LOSS_LEG
 * @property quantity Quantity to be modified
 * @property price Price to be modified
 * @property disclosedQuantity Number of shares visible (greater than 30% of quantity)
 * @property triggerPrice Price at which order is triggered
 * @property validity DAY or IOC
 *
 * @see <a href="https://dhanhq.co/docs/v2/orders/">DhanHQ Orders API</a>
 */
data class ModifyOrderParams(
    @JsonProperty("dhanClientId")
    val dhanClientId: String? = null,

    @JsonProperty("orderId")
    val orderId: String,

    @JsonProperty("orderType")
    val orderType: OrderType,

    @JsonProperty("legName")
    val legName: LegName? = null,

    @JsonProperty("quantity")
    val quantity: Int? = null,

    @JsonProperty("price")
    val price: Double? = null,

    @JsonProperty("disclosedQuantity")
    val disclosedQuantity: Int? = null,

    @JsonProperty("triggerPrice")
    val triggerPrice: Double? = null,

    @JsonProperty("validity")
    val validity: Validity
)
