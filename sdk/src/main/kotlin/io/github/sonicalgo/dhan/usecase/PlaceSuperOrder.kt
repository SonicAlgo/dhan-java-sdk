package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.common.*
import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig

/**
 * Places a new Super Order via API.
 */
@JvmSynthetic
internal fun executePlaceSuperOrder(apiClient: ApiClient, config: DhanConfig, params: PlaceSuperOrderParams): OrderAction {
    val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
    return apiClient.post(
        endpoint = "/super/orders",
        body = request
    )
}

/**
 * Request parameters for placing a Super Order.
 *
 * Super Orders combine entry, target, and stop loss legs with optional trailing stop loss.
 *
 * @property dhanClientId User identification. Auto-injected from config if not provided.
 * @property correlationId User/partner generated ID for tracking back.
 * @property transactionType Transaction type: BUY or SELL.
 * @property exchangeSegment Exchange segment of the instrument.
 * @property productType Product type: INTRADAY, CNC, MTF.
 * @property orderType Order type: LIMIT or MARKET.
 * @property securityId Exchange standard ID for each scrip.
 * @property quantity Number of shares for the order.
 * @property price Entry price at which order is placed.
 * @property targetPrice Target price for profit booking leg.
 * @property stopLossPrice Stop loss price for stop loss leg.
 * @property trailingJump Trailing stop loss jump value in absolute terms.
 * @see <a href="https://dhanhq.co/docs/v2/super-order/">DhanHQ Super Order API</a>
 */
@GenerateBuilder
data class PlaceSuperOrderParams(
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

    @JsonProperty("securityId")
    val securityId: String,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("price")
    val price: Double,

    @JsonProperty("targetPrice")
    val targetPrice: Double? = null,

    @JsonProperty("stopLossPrice")
    val stopLossPrice: Double? = null,

    @JsonProperty("trailingJump")
    val trailingJump: Double? = null
)
