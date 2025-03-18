package com.corezoid.sdk.utils;

/**
 * Custom exception for HTTP-related errors in the Corezoid SDK
 */
public class HttpException extends Exception {
    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
