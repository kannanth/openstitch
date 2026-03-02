package com.openstitch.engine.exception;

/**
 * Exception thrown when expression evaluation fails.
 */
public class ExpressionException extends OpenStitchException {

    public ExpressionException(String message) {
        super(message);
    }

    public ExpressionException(String message, Throwable cause) {
        super(message, cause);
    }
}
