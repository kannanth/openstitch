package com.openstitch.engine.exception;

/**
 * Exception thrown when template JSON parsing fails.
 */
public class TemplateParseException extends OpenStitchException {

    public TemplateParseException(String message) {
        super(message);
    }

    public TemplateParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
