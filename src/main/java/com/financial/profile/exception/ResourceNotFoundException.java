package com.financial.profile.exception;

/**
 * Thrown when a requested resource does not exist.
 *
 * <p>Analogy: this is like a customer asking for a file that isn't in the
 * cabinet. The exception carries a clear message so the global handler can
 * turn it into a polite 404 response.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
