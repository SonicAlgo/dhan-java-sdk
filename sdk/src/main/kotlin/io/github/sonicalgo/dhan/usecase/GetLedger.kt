package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.config.ApiClient

// ==================== Response Models ====================

/**
 * Ledger entry.
 *
 * @see <a href="https://dhanhq.co/docs/v2/statements/">DhanHQ Statement API</a>
 */
data class LedgerEntry(
    @JsonProperty("dhanClientId")
    val dhanClientId: String? = null,

    @JsonProperty("narration")
    val narration: String,

    @JsonProperty("voucherdate")
    val voucherDate: String,

    @JsonProperty("voucherdesc")
    val voucherDescription: String? = null,

    @JsonProperty("debit")
    val debit: Double,

    @JsonProperty("credit")
    val credit: Double,

    @JsonProperty("runningbal")
    val runningBalance: Double
)

/**
 * Gets ledger entries for a date range via API.
 */
@JvmSynthetic
internal fun executeGetLedger(apiClient: ApiClient, fromDate: String, toDate: String): List<LedgerEntry> {
    return apiClient.get(
        endpoint = "/ledger",
        queryParams = mapOf(
            "from-date" to fromDate,
            "to-date" to toDate
        )
    )
}
