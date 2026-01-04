package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.config.ApiClient

// ==================== Response Models ====================

/**
 * Fund limit and margin details.
 *
 * Note: Some JSON field names contain typos in Dhan's API response
 * (e.g., "availabelBalance", "receiveableAmount"). The property names
 * use correct spelling while JsonProperty matches the actual API response.
 *
 * @see <a href="https://dhanhq.co/docs/v2/funds/">DhanHQ Funds API</a>
 */
data class FundLimit(
    @JsonProperty("dhanClientId")
    val dhanClientId: String,

    // Note: "availabelBalance" is the actual JSON field name from Dhan API (their typo)
    @JsonProperty("availabelBalance")
    val availableBalance: Double,

    @JsonProperty("sodLimit")
    val sodLimit: Double,

    @JsonProperty("collateralAmount")
    val collateralAmount: Double,

    // Note: "receiveableAmount" is the actual JSON field name from Dhan API (their typo)
    @JsonProperty("receiveableAmount")
    val receivableAmount: Double,

    @JsonProperty("utilizedAmount")
    val utilizedAmount: Double,

    @JsonProperty("blockedPayoutAmount")
    val blockedPayoutAmount: Double,

    @JsonProperty("withdrawableBalance")
    val withdrawableBalance: Double
)

/**
 * Gets fund limit and margin details via API.
 */
@JvmSynthetic
internal fun executeGetFundLimits(apiClient: ApiClient): FundLimit {
    return apiClient.get(
        endpoint = "/fundlimit"
    )
}
