package com.corezoid.sdk.utils;

/**
 * Custom exception for HTTP-related errors in the Corezoid SDK.
 * <p>
 * This exception is thrown when there are issues with HTTP communication,
 * such as connection failures, timeouts, or unexpected response codes.
 * </p>
 */
public class HttpException extends Exception {
    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
