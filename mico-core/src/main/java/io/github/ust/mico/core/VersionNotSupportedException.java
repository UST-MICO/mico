package io.github.ust.mico.core;

public class VersionNotSupportedException extends Exception {
    public VersionNotSupportedException() {
    }

    public VersionNotSupportedException(String message) {
        super(message);
    }
}
