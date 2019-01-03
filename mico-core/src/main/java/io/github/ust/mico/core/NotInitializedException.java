package io.github.ust.mico.core;

public class NotInitializedException extends Exception {
    public NotInitializedException() {
    }

    public NotInitializedException(String message) {
        super(message);
    }
}
