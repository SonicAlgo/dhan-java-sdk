package io.github.sonicalgo.dhan.config

/**
 * Constants used by the Dhan SDK.
 *
 * Contains all API URLs, timeouts, WebSocket configurations, and other constants
 * derived from the official DhanHQ API documentation.
 *
 * @see <a href="https://dhanhq.co/docs/v2/">DhanHQ API Documentation</a>
 */
internal object DhanConstants {

    // ==================== API Base URLs ====================

    /** Base URL for Dhan Trading API v2 endpoints. */
    const val BASE_URL = "https://api.dhan.co/v2"

    /** Base URL for Dhan authentication endpoints. */
    const val AUTH_URL = "https://auth.dhan.co"

    /** Base URL for instrument data files. */
    const val INSTRUMENTS_URL = "https://images.dhan.co/api-data"

    // ==================== WebSocket URLs ====================

    /** WebSocket URL for live market feed. */
    const val WS_MARKET_FEED_URL = "wss://api-feed.dhan.co"

    /** WebSocket URL for live order updates. */
    const val WS_ORDER_UPDATE_URL = "wss://api-order-update.dhan.co"

    /** WebSocket URL for 20-level market depth. */
    const val WS_TWENTY_DEPTH_URL = "wss://depth-api-feed.dhan.co/twentydepth"

    /** WebSocket URL for 200-level market depth. */
    const val WS_TWO_HUNDRED_DEPTH_URL = "wss://full-depth-api.dhan.co/twohundreddepth"

    // ==================== HTTP Timeouts ====================

    /** Connection timeout in milliseconds (10 seconds). */
    const val CONNECT_TIMEOUT_MS = 10_000L

    /** Read timeout in milliseconds (30 seconds). */
    const val READ_TIMEOUT_MS = 30_000L

    /** Write timeout in milliseconds (30 seconds). */
    const val WRITE_TIMEOUT_MS = 30_000L

    // ==================== WebSocket Configuration ====================

    /** WebSocket ping interval in milliseconds (10 seconds). */
    const val WEBSOCKET_PING_INTERVAL_MS = 10_000L

    /** Initial delay for WebSocket reconnection in milliseconds (1 second). */
    const val WEBSOCKET_RECONNECT_INITIAL_DELAY_MS = 1_000L

    /** Maximum delay for WebSocket reconnection in milliseconds (30 seconds). */
    const val WEBSOCKET_RECONNECT_MAX_DELAY_MS = 30_000L

    /** Default maximum number of WebSocket reconnection attempts. */
    const val WEBSOCKET_DEFAULT_MAX_RECONNECT_ATTEMPTS = 5

    /** Maximum instruments per WebSocket connection. */
    const val WEBSOCKET_MAX_INSTRUMENTS_PER_CONNECTION = 5000

    /** Maximum instruments per subscription message. */
    const val WEBSOCKET_MAX_INSTRUMENTS_PER_SUBSCRIPTION = 100

    /** Maximum WebSocket connections per user. */
    const val WEBSOCKET_MAX_CONNECTIONS = 5

    /** Timeout in seconds for HTTP client shutdown. */
    const val SHUTDOWN_TIMEOUT_SECONDS = 5L

    // ==================== Exchange Segment Codes ====================

    /** Exchange segment code mapping for WebSocket subscriptions. */
    object ExchangeSegmentCode {
        const val IDX_I = 0          // Index values
        const val NSE_EQ = 1         // NSE Equity Cash
        const val NSE_FNO = 2        // NSE Futures & Options
        const val NSE_CURRENCY = 3   // NSE Currency
        const val BSE_EQ = 4         // BSE Equity Cash
        const val MCX_COMM = 5       // MCX Commodity
        const val BSE_CURRENCY = 7   // BSE Currency
        const val BSE_FNO = 8        // BSE Futures & Options
    }

    // ==================== WebSocket Request Codes ====================

    /** WebSocket request codes for market feed. */
    object FeedRequestCode {
        const val CONNECT = 11
        const val DISCONNECT = 12
        const val SUBSCRIBE_TICKER = 15
        const val UNSUBSCRIBE_TICKER = 16
        const val SUBSCRIBE_QUOTE = 17
        const val UNSUBSCRIBE_QUOTE = 18
        const val SUBSCRIBE_FULL = 21
        const val UNSUBSCRIBE_FULL = 22
        const val SUBSCRIBE_DEPTH = 23
        const val UNSUBSCRIBE_DEPTH = 24
    }

    /** WebSocket response codes for market feed. */
    object FeedResponseCode {
        const val INDEX = 1
        const val TICKER = 2
        const val QUOTE = 4
        const val OI = 5
        const val PREV_CLOSE = 6
        const val MARKET_STATUS = 7
        const val FULL = 8
        const val DISCONNECT = 50
    }

    /** Order update login message code. */
    const val ORDER_UPDATE_MSG_CODE = 42
}
