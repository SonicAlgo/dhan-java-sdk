package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.config.ApiClient

/**
 * Gets all holdings from demat account via API.
 */
@JvmSynthetic
internal fun executeGetHoldings(apiClient: ApiClient): List<Holding> {
    return apiClient.get(
        endpoint = "/holdings"
    )
}

// ==================== Response Models ====================

/**
 * Holdings data from demat account.
 *
 * @see <a href="https://dhanhq.co/docs/v2/portfolio/">DhanHQ Portfolio API</a>
 */
data class Holding(
    @JsonProperty("exchange")
    val exchange: String? = null,

    @JsonProperty("tradingSymbol")
    val tradingSymbol: String,

    @JsonProperty("securityId")
    val securityId: String,

    @JsonProperty("isin")
    val isin: String,

    @JsonProperty("totalQty")
    val totalQuantity: Int,

    @JsonProperty("dpQty")
    val dematQuantity: Int,

    @JsonProperty("t1Qty")
    val t1Quantity: Int,

    @JsonProperty("availableQty")
    val availableQuantity: Int,

    @JsonProperty("collateralQty")
    val collateralQuantity: Int,

    @JsonProperty("avgCostPrice")
    val averageCostPrice: Double
)
