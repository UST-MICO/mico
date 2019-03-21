package io.github.ust.mico.core.exception;

public class MicoServiceAlreadyExistsException extends Exception {

    private static final long serialVersionUID = -9095314816751991862L;

    public MicoServiceAlreadyExistsException(String shortName, String version) {
        super("Service '" + shortName + "' '" + version + "' already exists.");
    }

    public MicoServiceAlreadyExistsException(Long id) {
        super("Service '" + id + "' already exists.");
    }

    public MicoServiceAlreadyExistsException() {
        super("Service already exists.");
    }
}
