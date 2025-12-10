package io.github.sonicalgo.dhan.api

import io.github.sonicalgo.dhan.config.ApiClient
import io.github.sonicalgo.dhan.exception.DhanApiException
import io.github.sonicalgo.dhan.model.request.EdisFormParams
import io.github.sonicalgo.dhan.model.response.EdisForm
import io.github.sonicalgo.dhan.model.response.EdisStatus

/**
 * API module for EDIS (Electronic Delivery of Securities) operations.
 *
 * EDIS enables selling of holding stocks through the CDSL eDIS flow
 * by generating T-PIN, marking stocks, and tracking approval status.
 *
 * Example usage:
 * ```kotlin
 * val dhan = Dhan.builder()
 *     .clientId("1000000132")
 *     .accessToken("your-token")
 *     .build()
 *
 * // Step 1: Generate T-PIN (sent to mobile)
 * dhan.getEdisApi().generateTpin()
 *
 * // Step 2: Generate EDIS form
 * val form = dhan.getEdisApi().generateEdisForm(EdisFormParams(
 *     isin = "INE733E01010",
 *     quantity = 10,
 *     exchange = Exchange.NSE
 * ))
 * // Display form.edisFormHtml to user for T-PIN entry
 *
 * // Step 3: Check status
 * val status = dhan.getEdisApi().inquireEdisStatus("INE733E01010")
 * println("Approved: ${status.approvedQuantity}/${status.totalQuantity}")
 * ```
 *
 * @see <a href="https://dhanhq.co/docs/v2/edis/">DhanHQ EDIS API</a>
 */
class EdisApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Generates T-PIN for EDIS authentication.
     *
     * T-PIN is sent to the registered mobile number.
     *
     * Example:
     * ```kotlin
     * val edisApi = dhan.getEdisApi()
     * edisApi.generateTpin()
     * println("T-PIN sent to registered mobile")
     * ```
     *
     * @throws DhanApiException if T-PIN generation fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/edis/">Generate T-PIN API</a>
     */
    fun generateTpin() {
        apiClient.get<Unit>(
            endpoint = Endpoints.TPIN
        )
    }

    /**
     * Generates EDIS form for T-PIN entry and stock marking.
     *
     * Returns escaped HTML form from CDSL that should be displayed
     * to the user for T-PIN entry.
     *
     * Example:
     * ```kotlin
     * val edisApi = dhan.getEdisApi()
     *
     * val form = edisApi.generateEdisForm(EdisFormParams(
     *     isin = "INE733E01010",
     *     quantity = 10,
     *     exchange = Exchange.NSE,
     *     bulk = false
     * ))
     * // Display form.edisFormHtml to user
     * ```
     *
     * @param params EDIS form request parameters
     * @return [EdisForm] with HTML form
     * @throws DhanApiException if form generation fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/edis/">Generate EDIS Form API</a>
     */
    fun generateEdisForm(params: EdisFormParams): EdisForm {
        return apiClient.post(
            endpoint = Endpoints.FORM,
            body = params
        )
    }

    /**
     * Inquires EDIS approval status for a security.
     *
     * Use "ALL" as isin to check status for all holdings.
     *
     * Example:
     * ```kotlin
     * val edisApi = dhan.getEdisApi()
     *
     * // Check specific ISIN
     * val status = edisApi.inquireEdisStatus("INE733E01010")
     * println("Status: ${status.status}")
     * println("Approved: ${status.approvedQuantity}/${status.totalQuantity}")
     *
     * // Check all holdings
     * val allStatus = edisApi.inquireEdisStatus("ALL")
     * ```
     *
     * @param isin Security identifier or "ALL" for all holdings
     * @return [EdisStatus] with approval status
     * @throws DhanApiException if inquiry fails
     *
     * @see <a href="https://dhanhq.co/docs/v2/edis/">EDIS Inquiry API</a>
     */
    fun inquireEdisStatus(isin: String): EdisStatus {
        return apiClient.get(
            endpoint = "${Endpoints.INQUIRE}/$isin"
        )
    }

    internal object Endpoints {
        const val TPIN = "/edis/tpin"
        const val FORM = "/edis/form"
        const val INQUIRE = "/edis/inquire"
    }
}
