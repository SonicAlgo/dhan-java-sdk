package io.github.sonicalgo.dhan.exception

import io.github.sonicalgo.core.exception.SdkException

/**
 * Exception for Dhan HTTP API errors.
 *
 * Extends [SdkException] to provide common error categorization methods
 * like [isRateLimitError], [isAuthenticationError], [isValidationError], etc.
 *
 * @property httpStatusCode HTTP status code returned by the API
 * @param message Raw response body from the API
 */
class DhanApiException(
    message: String?,
    httpStatusCode: Int? = null,
    cause: Throwable? = null
) : SdkException(message, httpStatusCode, cause) {

    /**
     * Secondary constructor for backward compatibility.
     */
    constructor(httpStatusCode: Int, message: String) : this(message, httpStatusCode, null)
}
