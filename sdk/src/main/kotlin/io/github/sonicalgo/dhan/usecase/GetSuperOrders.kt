package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.common.ExchangeSegment
import io.github.sonicalgo.dhan.common.LegName
import io.github.sonicalgo.dhan.common.OrderStatus
import io.github.sonicalgo.dhan.common.OrderType
import io.github.sonicalgo.dhan.common.ProductType
import io.github.sonicalgo.dhan.common.TransactionType
import io.github.sonicalgo.dhan.common.Validity
import io.github.sonicalgo.dhan.config.ApiClient

// ==================== Response Models ====================

/**
 * Super Order book item.
 *
 * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
 */
data class SuperOrderBookItem(
    @JsonProperty("dhanClientId")
    val dhanClientId: String,

    @JsonProperty("orderId")
    val orderId: String,

    @JsonProperty("correlationId")
    val correlationId: String? = null,

    @JsonProperty("orderStatus")
    val orderStatus: OrderStatus,

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

    @JsonProperty("remainingQuantity")
    val remainingQuantity: Int? = null,

    @JsonProperty("ltp")
    val ltp: Double? = null,

    @JsonProperty("price")
    val price: Double,

    @JsonProperty("afterMarketOrder")
    val afterMarketOrder: Boolean? = null,

    @JsonProperty("legName")
    val legName: LegName? = null,

    @JsonProperty("trailingJump")
    val trailingJump: Double? = null,

    @JsonProperty("exchangeOrderId")
    val exchangeOrderId: String? = null,

    @JsonProperty("createTime")
    val createTime: String? = null,

    @JsonProperty("updateTime")
    val updateTime: String? = null,

    @JsonProperty("exchangeTime")
    val exchangeTime: String? = null,

    @JsonProperty("omsErrorDescription")
    val omsErrorDescription: String? = null,

    @JsonProperty("averageTradedPrice")
    val averageTradedPrice: Double? = null,

    @JsonProperty("filledQty")
    val filledQty: Int? = null,

    @JsonProperty("legDetails")
    val legDetails: List<SuperOrderLegDetail>? = null
)

/**
 * Super Order leg detail.
 *
 * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
 */
data class SuperOrderLegDetail(
    @JsonProperty("orderId")
    val orderId: String? = null,

    @JsonProperty("legName")
    val legName: LegName? = null,

    @JsonProperty("transactionType")
    val transactionType: TransactionType? = null,

    @JsonProperty("totalQuantity")
    val totalQuantity: Int? = null,

    @JsonProperty("remainingQuantity")
    val remainingQuantity: Int? = null,

    @JsonProperty("triggeredQuantity")
    val triggeredQuantity: Int? = null,

    @JsonProperty("price")
    val price: Double? = null,

    @JsonProperty("orderStatus")
    val orderStatus: OrderStatus? = null,

    @JsonProperty("trailingJump")
    val trailingJump: Double? = null
)

/**
 * Gets all Super Orders for the day via API.
 */
@JvmSynthetic
internal fun executeGetSuperOrders(apiClient: ApiClient): List<SuperOrderBookItem> {
    return apiClient.get(
        endpoint = "/super/orders"
    )
}
