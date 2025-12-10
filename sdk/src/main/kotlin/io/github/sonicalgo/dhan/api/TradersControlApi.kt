package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.enums.KillSwitchStatus
import io.github.sonicalgo.dhan.model.response.KillSwitchResult

/**
 * API module for Trader's Control operations.
 *
 * Provides kill switch functionality to enable/disable trading
 * for the current trading day.
 *
 * Important: Ensure all positions are closed and no pending orders exist
 * before activating the kill switch.
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Activate kill switch (disable trading)
 * val response = dhan.getTradersControlApi().setKillSwitch(KillSwitchStatus.ACTIVATE)
 * println(response.killSwitchStatus)
 *
 * // Deactivate kill switch (enable trading)
 * val deactivate = dhan.getTradersControlApi().setKillSwitch(KillSwitchStatus.DEACTIVATE)
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/traders-control/">DhanHQ Trader's Control API</a>
 */
class TradersControlApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Activates or deactivates the kill switch.
     *
     * When activated, trading is disabled for the current trading day.
     * Requires all positions to be closed and no pending orders.
     *
     * Example:
     * ```kotlin
     * val tradersControlApi = dhan.getTradersControlApi()
     *
     * // Activate kill switch
     * val response = tradersControlApi.setKillSwitch(KillSwitchStatus.ACTIVATE)
     * println("Kill switch activated: ${response.killSwitchStatus}")
     *
     * // Deactivate kill switch
     * val deactivate = tradersControlApi.setKillSwitch(KillSwitchStatus.DEACTIVATE)
     * println("Kill switch deactivated: ${deactivate.killSwitchStatus}")
     * ```
     *
     * @param status ACTIVATE to disable trading, DEACTIVATE to enable
     * @return [KillSwitchResult] with confirmation message
     * @throws DhanApiException if operation fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/traders-control/">Kill Switch API</a>
     */
    fun setKillSwitch(status: KillSwitchStatus): KillSwitchResult {
        return apiClient.post(
            endpoint = "${Endpoints.KILL_SWITCH}?killSwitchStatus=${status.name}"
        )
    }

    internal object Endpoints {
        const val KILL_SWITCH = "/killswitch"
    }
}
