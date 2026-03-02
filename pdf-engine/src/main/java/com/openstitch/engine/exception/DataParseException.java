package com.openstitch.engine.exception;

/**
 * Exception thrown when data parsing fails (JSON, XML, CSV).
 */
public class DataParseException extends OpenStitchException {

    public DataParseException(String message) {
        super(message);
    }

    public DataParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
