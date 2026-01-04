package io.github.sonicalgo.dhan.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.dhan.config.ApiClient

// ==================== Enums ====================

/**
 * DDPI status for user profile.
 *
 * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
 */
enum class DdpiStatus {
    /** DDPI is active */
    @JsonProperty("Active")
    ACTIVE,

    /** DDPI is deactive */
    @JsonProperty("Deactive")
    DEACTIVE
}

/**
 * MTF status for user profile.
 *
 * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
 */
enum class MtfStatus {
    /** MTF is active */
    @JsonProperty("Active")
    ACTIVE,

    /** MTF is deactive */
    @JsonProperty("Deactive")
    DEACTIVE
}

/**
 * Data plan status for user profile.
 *
 * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
 */
enum class DataPlanStatus {
    /** Data plan is active */
    @JsonProperty("Active")
    ACTIVE,

    /** Data plan is deactive */
    @JsonProperty("Deactive")
    DEACTIVE
}

// ==================== Response Models ====================

/**
 * User profile information.
 */
data class UserProfile(
    @JsonProperty("dhanClientId")
    val dhanClientId: String,

    @JsonProperty("tokenValidity")
    val tokenValidity: String? = null,

    @JsonProperty("activeSegment")
    val activeSegment: String? = null,

    @JsonProperty("ddpi")
    val ddpi: DdpiStatus? = null,

    @JsonProperty("mtf")
    val mtf: MtfStatus? = null,

    @JsonProperty("dataPlan")
    val dataPlan: DataPlanStatus? = null,

    @JsonProperty("dataValidity")
    val dataValidity: String? = null
)

/**
 * Gets user profile and token validity via API.
 */
@JvmSynthetic
internal fun executeGetProfile(apiClient: ApiClient): UserProfile {
    return apiClient.get(
        endpoint = "/profile"
    )
}
