package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.dhan.common.*
import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig

/**
 * Converts position between product types via API.
 */
@JvmSynthetic
internal fun executeConvertPosition(apiClient: ApiClient, config: DhanConfig, params: ConvertPositionParams) {
    val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
    apiClient.post<Unit>(
        endpoint = "/positions/convert",
        body = request
    )
}

/**
 * Request parameters for converting position between product types.
 *
 * @property dhanClientId User identification. Auto-injected from config if not provided.
 * @property fromProductType Source product type: CNC, INTRADAY, MARGIN, CO, BO.
 * @property exchangeSegment Exchange segment of the position.
 * @property positionType Position type: LONG, SHORT, CLOSED.
 * @property securityId Exchange standard ID for each scrip.
 * @property tradingSymbol Trading symbol of the security (optional).
 * @property convertQty Quantity to convert.
 * @property toProductType Target product type: CNC, INTRADAY, MARGIN, CO, BO.
 * @see <a href="https://dhanhq.co/docs/v2/portfolio/">DhanHQ Portfolio API</a>
 */
@GenerateBuilder
data class ConvertPositionParams(
    @JsonProperty("dhanClientId")
    val dhanClientId: String? = null,

    @JsonProperty("fromProductType")
    val fromProductType: ProductType,

    @JsonProperty("exchangeSegment")
    val exchangeSegment: ExchangeSegment,

    @JsonProperty("positionType")
    val positionType: PositionType,

    @JsonProperty("securityId")
    val securityId: String,

    @JsonProperty("tradingSymbol")
    val tradingSymbol: String? = null,

    @JsonProperty("convertQty")
    val convertQty: Int,

    @JsonProperty("toProductType")
    val toProductType: ProductType
)
