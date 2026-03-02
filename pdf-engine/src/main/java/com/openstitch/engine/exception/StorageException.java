package com.openstitch.engine.exception;

/**
 * Exception thrown when a storage operation fails.
 */
public class StorageException extends OpenStitchException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
