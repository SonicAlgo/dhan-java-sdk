package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.config.DhanConfig
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.request.ConvertPositionParams
import io.github.sonicalgo.dhan.model.response.Holding
import io.github.sonicalgo.dhan.model.response.Position

/**
 * API module for portfolio operations.
 *
 * Provides methods for retrieving holdings, positions, and converting
 * positions between product types.
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Get holdings
 * val holdings = dhan.getPortfolioApi().getHoldings()
 * holdings.forEach { holding ->
 *     println("${holding.tradingSymbol}: ${holding.totalQty} @ ${holding.avgCostPrice}")
 * }
 *
 * // Get positions
 * val positions = dhan.getPortfolioApi().getPositions()
 * positions.forEach { position ->
 *     println("${position.tradingSymbol}: ${position.netQty}")
 *     println("  P&L: ${position.unrealizedProfit}")
 * }
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/portfolio/">DhanHQ Portfolio API</a>
 */
class PortfolioApi internal constructor(
    private val apiClient: ApiClient,
    private val config: DhanConfig
) {

    /**
     * Gets all holdings from demat account.
     *
     * Returns long-term holdings including quantity breakup (DP, T1, collateral).
     *
     * Example:
     * ```kotlin
     * val portfolioApi = dhan.getPortfolioApi()
     *
     * val holdings = portfolioApi.getHoldings()
     * holdings.forEach { holding ->
     *     println("${holding.tradingSymbol} (${holding.isin})")
     *     println("  Total: ${holding.totalQty}")
     *     println("  Available: ${holding.availableQty}")
     *     println("  Avg Cost: ${holding.avgCostPrice}")
     * }
     * ```
     *
     * @return List of [Holding] in demat account
     * @throws DhanApiException if retrieval fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/portfolio/">Get Holdings API</a>
     */
    fun getHoldings(): List<Holding> {
        return apiClient.get(
            endpoint = Endpoints.HOLDINGS
        )
    }

    /**
     * Gets all open positions.
     *
     * Returns intraday and F&O positions with profit/loss calculations.
     *
     * Example:
     * ```kotlin
     * val portfolioApi = dhan.getPortfolioApi()
     *
     * val positions = portfolioApi.getPositions()
     * positions.forEach { position ->
     *     println("${position.tradingSymbol}: ${position.positionType}")
     *     println("  Net Qty: ${position.netQty}")
     *     println("  Realized P&L: ${position.realizedProfit}")
     *     println("  Unrealized P&L: ${position.unrealizedProfit}")
     * }
     * ```
     *
     * @return List of [Position] open positions
     * @throws DhanApiException if retrieval fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/portfolio/">Get Positions API</a>
     */
    fun getPositions(): List<Position> {
        return apiClient.get(
            endpoint = Endpoints.POSITIONS
        )
    }

    /**
     * Converts position between product types.
     *
     * Allows converting intraday to delivery or delivery to intraday.
     *
     * Example:
     * ```kotlin
     * val portfolioApi = dhan.getPortfolioApi()
     *
     * portfolioApi.convertPosition(ConvertPositionParams(
     *     dhanClientId = "1000000009",
     *     fromProductType = ProductType.INTRADAY,
     *     exchangeSegment = ExchangeSegment.NSE_EQ,
     *     positionType = PositionType.LONG,
     *     securityId = "11536",
     *     convertQty = 40,
     *     toProductType = ProductType.CNC
     * ))
     * println("Position converted successfully")
     * ```
     *
     * @param params Position conversion request parameters
     * @throws DhanApiException if conversion fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/portfolio/">Convert Position API</a>
     */
    fun convertPosition(params: ConvertPositionParams) {
        val request = params.copy(dhanClientId = params.dhanClientId ?: config.clientId)
        apiClient.post<Unit>(
            endpoint = Endpoints.CONVERT_POSITION,
            body = request
        )
    }

    internal object Endpoints {
        const val HOLDINGS = "/holdings"
        const val POSITIONS = "/positions"
        const val CONVERT_POSITION = "/positions/convert"
    }
}
