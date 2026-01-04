package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.common.ExchangeSegment
import io.github.sonicalgo.dhan.common.ProductType
import io.github.sonicalgo.dhan.common.TransactionType
import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig

// ==================== Response Models ====================

/**
 * Margin calculation result.
 *
 * @see <a href="https://dhanhq.co/docs/v2/funds/">DhanHQ Funds API</a>
 */
data class MarginCalculation(
    @JsonProperty("totalMargin")
    val totalMargin: Double,

    @JsonProperty("spanMargin")
    val spanMargin: Double,

    @JsonProperty("exposureMargin")
    val exposureMargin: Double,

    @JsonProperty("availableBalance")
    val availableBalance: Double,

    @JsonProperty("variableMargin")
    val variableMargin: Double,

    @JsonProperty("insufficientBalance")
    val insufficientBalance: Double,

    @JsonProperty("brokerage")
    val brokerage: Double,

    @JsonProperty("leverage")
    val leverage: String? = null
)

/**
 * Calculates margin requirements for an order via API.
 */
@JvmSynthetic
internal fun executeCalculateMargin(apiClient: ApiClient, config: DhanConfig, params: CalculateMarginParams): MarginCalculation {
    val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
    return apiClient.post(
        endpoint = "/margincalculator",
        body = request
    )
}

/**
 * Request parameters for calculating margin requirements.
 *
 * @property dhanClientId User identification. Auto-injected from config if not provided.
 * @property exchangeSegment Exchange segment: NSE_EQ, NSE_FNO, BSE_EQ, BSE_FNO, MCX_COMM.
 * @property transactionType Transaction type: BUY or SELL.
 * @property quantity Number of shares/lots for the order.
 * @property productType Product type: CNC, INTRADAY, MARGIN, MTF, CO, BO.
 * @property securityId Exchange standard ID for each scrip.
 * @property price Order placement price.
 * @property triggerPrice Trigger price for SL-M and SL-L orders.
 * @see <a href="https://dhanhq.co/docs/v2/funds/">DhanHQ Funds API</a>
 */
@GenerateBuilder
data class CalculateMarginParams(
    @JsonProperty("dhanClientId")
    val dhanClientId: String? = null,

    @JsonProperty("exchangeSegment")
    val exchangeSegment: ExchangeSegment,

    @JsonProperty("transactionType")
    val transactionType: TransactionType,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("productType")
    val productType: ProductType,

    @JsonProperty("securityId")
    val securityId: String,

    @JsonProperty("price")
    val price: Double,

    @JsonProperty("triggerPrice")
    val triggerPrice: Double? = null
)
