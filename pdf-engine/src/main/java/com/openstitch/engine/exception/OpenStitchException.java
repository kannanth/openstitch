package com.openstitch.engine.exception;

/**
 * Base runtime exception for the OpenStitch PDF engine.
 */
public class OpenStitchException extends RuntimeException {

    public OpenStitchException(String message) {
        super(message);
    }

    public OpenStitchException(String message, Throwable cause) {
        super(message, cause);
    }
}
