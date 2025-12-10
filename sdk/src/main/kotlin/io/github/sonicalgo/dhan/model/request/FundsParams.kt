package io.github.sonicalgo.dhan.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.Dhan
import io.github.sonicalgo.dhan.model.enums.ExchangeSegment
import io.github.sonicalgo.dhan.model.enums.ProductType
import io.github.sonicalgo.dhan.model.enums.TransactionType

/**
 * Request parameters for calculating margin requirements.
 *
 * Used to calculate SPAN, exposure, VAR margins and brokerage for an order.
 *
 * Example usage:
 * ```kotlin
 * val fundsApi = dhan.getFundsApi()
 *
 * val margin = fundsApi.calculateMargin(MarginCalculatorParams(
 *     dhanClientId = "1000000132",
 *     exchangeSegment = ExchangeSegment.NSE_FNO,
 *     transactionType = TransactionType.BUY,
 *     quantity = 50,
 *     productType = ProductType.MARGIN,
 *     securityId = "49081",
 *     price = 250.0
 * ))
 * println("Total Margin: ${margin.totalMargin}")
 * ```
 *
 * @property dhanClientId Optional. Auto-injected from [Dhan] config if not provided.
 * @property exchangeSegment Exchange and segment
 * @property transactionType Transaction type: BUY or SELL
 * @property quantity Number of shares/lots
 * @property productType Product type
 * @property securityId Exchange security identifier
 * @property price Order price
 * @property triggerPrice Trigger price for SL-M and SL-L orders
 * @see <a href="https://dhanhq.co/docs/v2/funds/">DhanHQ Funds API</a>
 */
data class MarginCalculatorParams(
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
