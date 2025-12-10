package io.github.sonicalgo.dhan.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.Dhan
import io.github.sonicalgo.dhan.model.enums.ExchangeSegment
import io.github.sonicalgo.dhan.model.enums.PositionType
import io.github.sonicalgo.dhan.model.enums.ProductType

/**
 * Request parameters for converting position between intraday and delivery.
 *
 * @property dhanClientId Optional. Auto-injected from [Dhan] config if not provided.
 * @property fromProductType Source product type: CNC, INTRADAY, MARGIN, CO, BO
 * @property exchangeSegment Exchange and segment in which position is created
 * @property positionType Position Type: LONG, SHORT, CLOSED
 * @property securityId Exchange standard ID for each scrip
 * @property tradingSymbol Trading symbol reference
 * @property convertQty Number of shares modification is desired
 * @property toProductType Desired product type: CNC, INTRADAY, MARGIN, CO, BO
 *
 * @see <a href="https://dhanhq.co/docs/v2/portfolio/">DhanHQ Portfolio API</a>
 */
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
