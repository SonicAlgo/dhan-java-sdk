package io.github.sonicalgo.dhan.config

import io.github.sonicalgo.core.config.WebSocketSdkConfig

/**
 * WebSocket configuration for Dhan WebSocket clients.
 *
 * @property maxReconnectAttempts Maximum reconnection attempts
 * @property autoReconnectEnabled Enable automatic reconnection
 * @property autoResubscribeEnabled Enable automatic resubscription after reconnect (Dhan-specific)
 */
class DhanWebSocketConfig internal constructor(
    override val maxReconnectAttempts: Int,
    override val autoReconnectEnabled: Boolean,
    override val pingIntervalMs: Long = DhanConstants.WEBSOCKET_PING_INTERVAL_MS,
    val autoResubscribeEnabled: Boolean = true
) : WebSocketSdkConfig