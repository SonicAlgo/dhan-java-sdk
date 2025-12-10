package io.github.sonicalgo.dhan.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.model.enums.*
import io.github.sonicalgo.dhan.Dhan

/**
 * Request parameters for placing a forever order (GTT).
 *
 * @property dhanClientId Optional. Auto-injected from [Dhan] config if not provided.
 * @property correlationId Partner-generated tracking identifier
 * @property orderFlag SINGLE or OCO
 * @property transactionType BUY or SELL
 * @property exchangeSegment NSE_EQ, NSE_FNO, BSE_EQ
 * @property productType CNC or MTF
 * @property orderType LIMIT or MARKET
 * @property validity DAY or IOC
 * @property securityId Exchange standard instrument ID
 * @property quantity Number of shares
 * @property disclosedQuantity Visible quantity (min 30% of total)
 * @property price Order placement price
 * @property triggerPrice Price triggering execution
 * @property price1 Target price for OCO (conditional)
 * @property triggerPrice1 Trigger for OCO leg (conditional)
 * @property quantity1 Quantity for OCO leg (conditional)
 *
 * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
 */
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

/**
 * Request parameters for modifying a forever order.
 *
 * @property dhanClientId Optional. Auto-injected from [Dhan] config if not provided.
 * @property orderId Target order ID
 * @property orderFlag SINGLE or OCO
 * @property orderType LIMIT, MARKET, STOP_LOSS, STOP_LOSS_MARKET
 * @property legName TARGET_LEG or STOP_LOSS_LEG
 * @property quantity Updated share count
 * @property price Revised order price
 * @property disclosedQuantity Visible quantity
 * @property triggerPrice Updated trigger threshold
 * @property validity DAY or IOC
 *
 * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
 */
data class ModifyForeverOrderParams(
    @JsonProperty("dhanClientId")
    val dhanClientId: String? = null,

    @JsonProperty("orderId")
    val orderId: String,

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
