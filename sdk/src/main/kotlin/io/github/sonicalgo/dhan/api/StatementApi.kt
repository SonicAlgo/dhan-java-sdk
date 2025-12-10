package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.response.LedgerEntry
import io.github.sonicalgo.dhan.model.response.TradeHistory

/**
 * API module for statement and trade history operations.
 *
 * Provides methods for retrieving ledger entries and historical trade data.
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Get ledger entries
 * val ledger = dhan.getStatementApi().getLedger("2024-01-01", "2024-01-31")
 * ledger.forEach { entry ->
 *     println("${entry.voucherDate}: ${entry.narration}")
 *     println("  Debit: ${entry.debit}, Credit: ${entry.credit}")
 * }
 *
 * // Get trade history
 * val trades = dhan.getStatementApi().getTradeHistory("2024-01-01", "2024-01-31", 0)
 * trades.forEach { trade ->
 *     println("${trade.tradingSymbol}: ${trade.tradedQuantity} @ ${trade.tradedPrice}")
 * }
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/statements/">DhanHQ Statement API</a>
 */
class StatementApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Gets ledger entries for a date range.
     *
     * Returns trading account debit and credit details.
     *
     * Example:
     * ```kotlin
     * val statementApi = dhan.getStatementApi()
     *
     * val ledger = statementApi.getLedger("2024-01-01", "2024-01-31")
     * ledger.forEach { entry ->
     *     println("${entry.voucherDate}: ${entry.voucherDescription}")
     *     println("  ${entry.narration}")
     *     println("  Debit: ${entry.debit}, Credit: ${entry.credit}")
     *     println("  Balance: ${entry.runningBalance}")
     * }
     * ```
     *
     * @param fromDate Start date in YYYY-MM-DD format
     * @param toDate End date in YYYY-MM-DD format
     * @return List of [LedgerEntry]
     * @throws DhanApiException if retrieval fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/statements/">Get Ledger API</a>
     */
    fun getLedger(fromDate: String, toDate: String): List<LedgerEntry> {
        return apiClient.get(
            endpoint = Endpoints.LEDGER,
            queryParams = mapOf(
                "from-date" to fromDate,
                "to-date" to toDate
            )
        )
    }

    /**
     * Gets trade history for a date range.
     *
     * Returns detailed trade history with charges breakdown.
     * Results are paginated.
     *
     * Example:
     * ```kotlin
     * val statementApi = dhan.getStatementApi()
     *
     * val trades = statementApi.getTradeHistory("2024-01-01", "2024-01-31", 0)
     * trades.forEach { trade ->
     *     println("Order: ${trade.orderId}")
     *     println("  ${trade.tradingSymbol}: ${trade.transactionType}")
     *     println("  Qty: ${trade.tradedQuantity} @ ${trade.tradedPrice}")
     *     println("  Brokerage: ${trade.brokerageCharges}")
     * }
     * ```
     *
     * @param fromDate Start date in YYYY-MM-DD format
     * @param toDate End date in YYYY-MM-DD format
     * @param page Page number (0-indexed)
     * @return List of [TradeHistory]
     * @throws DhanApiException if retrieval fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/statements/">Get Trade History API</a>
     */
    fun getTradeHistory(fromDate: String, toDate: String, page: Int = 0): List<TradeHistory> {
        return apiClient.get(
            endpoint = "${Endpoints.TRADES}/$fromDate/$toDate/$page"
        )
    }

    internal object Endpoints {
        const val LEDGER = "/ledger"
        const val TRADES = "/trades"
    }
}
