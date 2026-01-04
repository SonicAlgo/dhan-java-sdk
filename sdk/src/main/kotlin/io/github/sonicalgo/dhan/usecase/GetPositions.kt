package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.common.DrvOptionType
import io.github.sonicalgo.dhan.common.ExchangeSegment
import io.github.sonicalgo.dhan.common.PositionType
import io.github.sonicalgo.dhan.common.ProductType
import io.github.sonicalgo.dhan.config.ApiClient

// ==================== Response Models ====================

/**
 * Position data from trading account.
 *
 * @see <a href="https://dhanhq.co/docs/v2/portfolio/">DhanHQ Portfolio API</a>
 */
data class Position(
    @JsonProperty("dhanClientId")
    val dhanClientId: String,

    @JsonProperty("tradingSymbol")
    val tradingSymbol: String,

    @JsonProperty("securityId")
    val securityId: String,

    @JsonProperty("positionType")
    val positionType: PositionType,

    @JsonProperty("exchangeSegment")
    val exchangeSegment: ExchangeSegment,

    @JsonProperty("productType")
    val productType: ProductType,

    @JsonProperty("buyAvg")
    val buyAverage: Double,

    @JsonProperty("buyQty")
    val buyQuantity: Int,

    @JsonProperty("costPrice")
    val costPrice: Double,

    @JsonProperty("sellAvg")
    val sellAverage: Double,

    @JsonProperty("sellQty")
    val sellQuantity: Int,

    @JsonProperty("netQty")
    val netQuantity: Int,

    @JsonProperty("realizedProfit")
    val realizedProfit: Double,

    @JsonProperty("unrealizedProfit")
    val unrealizedProfit: Double,

    @JsonProperty("rbiReferenceRate")
    val rbiReferenceRate: Double? = null,

    @JsonProperty("multiplier")
    val multiplier: Int? = null,

    @JsonProperty("carryForwardBuyQty")
    val carryForwardBuyQuantity: Int? = null,

    @JsonProperty("carryForwardSellQty")
    val carryForwardSellQuantity: Int? = null,

    @JsonProperty("carryForwardBuyValue")
    val carryForwardBuyValue: Double? = null,

    @JsonProperty("carryForwardSellValue")
    val carryForwardSellValue: Double? = null,

    @JsonProperty("dayBuyQty")
    val dayBuyQuantity: Int? = null,

    @JsonProperty("daySellQty")
    val daySellQuantity: Int? = null,

    @JsonProperty("dayBuyValue")
    val dayBuyValue: Double? = null,

    @JsonProperty("daySellValue")
    val daySellValue: Double? = null,

    @JsonProperty("drvExpiryDate")
    val drvExpiryDate: String? = null,

    @JsonProperty("drvOptionType")
    val drvOptionType: DrvOptionType? = null,

    @JsonProperty("drvStrikePrice")
    val drvStrikePrice: Double? = null,

    @JsonProperty("crossCurrency")
    val crossCurrency: Boolean? = null
)

/**
 * Gets all open positions via API.
 */
@JvmSynthetic
internal fun executeGetPositions(apiClient: ApiClient): List<Position> {
    return apiClient.get(
        endpoint = "/positions"
    )
}
