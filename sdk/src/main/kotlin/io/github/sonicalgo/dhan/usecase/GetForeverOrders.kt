package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.common.DrvOptionType
import io.github.sonicalgo.dhan.common.ExchangeSegment
import io.github.sonicalgo.dhan.common.ForeverOrderFlag
import io.github.sonicalgo.dhan.common.LegName
import io.github.sonicalgo.dhan.common.OrderStatus
import io.github.sonicalgo.dhan.common.OrderType
import io.github.sonicalgo.dhan.common.ProductType
import io.github.sonicalgo.dhan.common.TransactionType
import io.github.sonicalgo.dhan.common.Validity
import io.github.sonicalgo.dhan.config.ApiClient

// ==================== Response Models ====================

/**
 * Forever Order book item.
 *
 * @see <a href="https://dhanhq.co/docs/v2/forever/">DhanHQ Forever Order API</a>
 */
data class ForeverOrderBookItem(
    @JsonProperty("dhanClientId")
    val dhanClientId: String,

    @JsonProperty("orderId")
    val orderId: String,

    @JsonProperty("correlationId")
    val correlationId: String? = null,

    @JsonProperty("orderStatus")
    val orderStatus: OrderStatus,

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
    val validity: Validity? = null,

    @JsonProperty("tradingSymbol")
    val tradingSymbol: String,

    @JsonProperty("securityId")
    val securityId: String,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("price")
    val price: Double? = null,

    @JsonProperty("triggerPrice")
    val triggerPrice: Double? = null,

    @JsonProperty("legName")
    val legName: LegName? = null,

    @JsonProperty("createTime")
    val createTime: String? = null,

    @JsonProperty("updateTime")
    val updateTime: String? = null,

    @JsonProperty("exchangeTime")
    val exchangeTime: String? = null,

    @JsonProperty("drvExpiryDate")
    val drvExpiryDate: String? = null,

    @JsonProperty("drvOptionType")
    val drvOptionType: DrvOptionType? = null,

    @JsonProperty("drvStrikePrice")
    val drvStrikePrice: Double? = null
)

/**
 * Gets all Forever Orders via API.
 */
@JvmSynthetic
internal fun executeGetForeverOrders(apiClient: ApiClient): List<ForeverOrderBookItem> {
    return apiClient.get(
        endpoint = "/forever/orders"
    )
}
