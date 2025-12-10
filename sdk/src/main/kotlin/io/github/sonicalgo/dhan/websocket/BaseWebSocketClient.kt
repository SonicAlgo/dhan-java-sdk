package io.github.sonicalgo.dhan.websocket

import io.github.sonicalgo.core.websocket.BaseWebSocketClient as CoreBaseWebSocketClient
import io.github.sonicalgo.dhan.config.DhanConfig
import io.github.sonicalgo.dhan.config.DhanConstants
import io.github.sonicalgo.dhan.config.DhanWebSocketConfig
import okhttp3.OkHttpClient

/**
 * Base class for Dhan WebSocket clients.
 *
 * Provides Dhan-specific WebSocket functionality:
 * - Access to [DhanConfig] for credentials (clientId, accessToken)
 * - Credential validation helper
 * - Dhan-specific reconnection delays
 *
 * @param httpClient OkHttpClient configured for WebSocket connections
 * @param dhanConfig Dhan configuration for credentials
 * @param wsConfig WebSocket-specific configuration
 * @param clientName Name used for the reconnection thread
 */
abstract class BaseWebSocketClient(
    httpClient: OkHttpClient,
    protected val dhanConfig: DhanConfig,
    protected val wsConfig: DhanWebSocketConfig,
    clientName: String
) : CoreBaseWebSocketClient(
    httpClient = httpClient,
    config = wsConfig,
    clientName = clientName,
    initialReconnectDelayMs = DhanConstants.WEBSOCKET_RECONNECT_INITIAL_DELAY_MS,
    maxReconnectDelayMs = DhanConstants.WEBSOCKET_RECONNECT_MAX_DELAY_MS
) {

    /**
     * Checks if credentials (clientId and accessToken) are missing or blank.
     *
     * @return true if credentials are missing, false if valid
     */
    protected fun hasCredentialsError(): Boolean {
        return dhanConfig.clientId.isBlank() || dhanConfig.accessToken.isBlank()
    }
}
