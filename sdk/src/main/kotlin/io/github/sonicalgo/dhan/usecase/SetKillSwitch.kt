package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.config.ApiClient

// ==================== Enums ====================

/**
 * Kill switch status for Trader's Control.
 *
 * @see <a href="https://dhanhq.co/docs/v2/traders-control/">DhanHQ Trader's Control</a>
 */
enum class KillSwitchStatus {
    /** Activate kill switch - disable trading */
    @JsonProperty("ACTIVATE")
    ACTIVATE,

    /** Deactivate kill switch - enable trading */
    @JsonProperty("DEACTIVATE")
    DEACTIVATE
}

// ==================== Response Models ====================

/**
 * Kill switch result.
 */
data class KillSwitchResult(
    @JsonProperty("killSwitchStatus")
    val killSwitchStatus: String,

    @JsonProperty("message")
    val message: String? = null
)

/**
 * Activates or deactivates the kill switch via API.
 * When activated, trading is disabled for the current trading day.
 */
@JvmSynthetic
internal fun executeSetKillSwitch(apiClient: ApiClient, status: KillSwitchStatus): KillSwitchResult {
    return apiClient.post(
        endpoint = "/killswitch?killSwitchStatus=${status.name}"
    )
}
