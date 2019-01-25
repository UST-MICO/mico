package io.github.ust.mico.core;

/**
 * Indicates that a certain version is not supported.
 */
public class VersionNotSupportedException extends Exception {

    private static final long serialVersionUID = 5361232117156933210L;

    public VersionNotSupportedException() {
    
    }

    public VersionNotSupportedException(String message) {
        super(message);
    }
}
