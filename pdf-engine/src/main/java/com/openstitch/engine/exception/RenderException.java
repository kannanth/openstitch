package com.openstitch.engine.exception;

/**
 * Exception thrown when PDF rendering fails.
 */
public class RenderException extends OpenStitchException {

    public RenderException(String message) {
        super(message);
    }

    public RenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
