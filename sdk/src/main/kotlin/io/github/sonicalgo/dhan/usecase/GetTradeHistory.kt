package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.common.ExchangeSegment
import io.github.sonicalgo.dhan.common.ProductType
import io.github.sonicalgo.dhan.common.TransactionType
import io.github.sonicalgo.dhan.config.ApiClient

// ==================== Response Models ====================

/**
 * Trade history entry.
 *
 * @see <a href="https://dhanhq.co/docs/v2/statements/">DhanHQ Statement API</a>
 */
data class TradeHistory(
    @JsonProperty("dhanClientId")
    val dhanClientId: String,

    @JsonProperty("orderId")
    val orderId: String,

    @JsonProperty("exchangeOrderId")
    val exchangeOrderId: String? = null,

    @JsonProperty("exchangeTradeId")
    val exchangeTradeId: String? = null,

    @JsonProperty("transactionType")
    val transactionType: TransactionType,

    @JsonProperty("exchangeSegment")
    val exchangeSegment: ExchangeSegment,

    @JsonProperty("productType")
    val productType: ProductType,

    @JsonProperty("tradingSymbol")
    val tradingSymbol: String,

    @JsonProperty("securityId")
    val securityId: String,

    @JsonProperty("tradedQuantity")
    val tradedQuantity: Int,

    @JsonProperty("tradedPrice")
    val tradedPrice: Double,

    @JsonProperty("brokerageCharges")
    val brokerageCharges: Double? = null,

    @JsonProperty("exchangeTime")
    val exchangeTime: String? = null
)

/**
 * Gets trade history for a date range via API.
 * Results are paginated.
 */
@JvmSynthetic
internal fun executeGetTradeHistory(apiClient: ApiClient, fromDate: String, toDate: String, page: Int = 0): List<TradeHistory> {
    return apiClient.get(
        endpoint = "/trades/$fromDate/$toDate/$page"
    )
}
