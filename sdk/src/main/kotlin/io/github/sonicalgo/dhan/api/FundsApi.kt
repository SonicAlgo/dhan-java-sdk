package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.request.MarginCalculatorParams
import io.github.sonicalgo.dhan.model.response.FundLimit
import io.github.sonicalgo.dhan.model.response.MarginCalculation

/**
 * API module for funds and margin operations.
 *
 * Provides methods for retrieving fund limits and calculating margin requirements.
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Get fund limits
 * val funds = dhan.getFundsApi().getFundLimit()
 * println("Available: ${funds.availableBalance}")
 * println("Withdrawable: ${funds.withdrawableBalance}")
 *
 * // Calculate margin
 * val margin = dhan.getFundsApi().calculateMargin(MarginCalculatorParams(
 *     dhanClientId = "1000000132",
 *     exchangeSegment = ExchangeSegment.NSE_FNO,
 *     transactionType = TransactionType.BUY,
 *     quantity = 50,
 *     productType = ProductType.MARGIN,
 *     securityId = "49081",
 *     price = 250.0
 * ))
 * println("Total Margin Required: ${margin.totalMargin}")
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/funds/">DhanHQ Funds API</a>
 */
class FundsApi internal constructor(
    private val apiClient: ApiClient,
    private val config: DhanConfig
) {

    /**
     * Gets fund limit and margin details.
     *
     * Returns available balance, collateral, and fund utilization.
     *
     * Example:
     * ```kotlin
     * val fundsApi = dhan.getFundsApi()
     *
     * val funds = fundsApi.getFundLimit()
     * println("Available Balance: ${funds.availableBalance}")
     * println("Start of Day: ${funds.sodLimit}")
     * println("Collateral: ${funds.collateralAmount}")
     * println("Utilized: ${funds.utilizedAmount}")
     * println("Withdrawable: ${funds.withdrawableBalance}")
     * ```
     *
     * @return [FundLimit] with balance and margin details
     * @throws DhanApiException if retrieval fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/funds/">Get Fund Limit API</a>
     */
    fun getFundLimit(): FundLimit {
        return apiClient.get(
            endpoint = Endpoints.FUND_LIMIT
        )
    }

    /**
     * Calculates margin requirements for an order.
     *
     * Returns SPAN margin, exposure margin, VAR margin, brokerage,
     * and leverage for the specified order parameters.
     *
     * Example:
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
     * println("SPAN: ${margin.spanMargin}")
     * println("Exposure: ${margin.exposureMargin}")
     * println("Brokerage: ${margin.brokerage}")
     * ```
     *
     * @param params Margin calculation request parameters
     * @return [MarginCalculation] with margin requirements
     * @throws DhanApiException if calculation fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/funds/">Margin Calculator API</a>
     */
    fun calculateMargin(params: MarginCalculatorParams): MarginCalculation {
        val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
        return apiClient.post(
            endpoint = Endpoints.MARGIN_CALCULATOR,
            body = request
        )
    }

    internal object Endpoints {
        const val FUND_LIMIT = "/fundlimit"
        const val MARGIN_CALCULATOR = "/margincalculator"
    }
}
