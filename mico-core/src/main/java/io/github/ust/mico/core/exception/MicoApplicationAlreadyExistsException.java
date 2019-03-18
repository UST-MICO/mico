package io.github.ust.mico.core.exception;

public class MicoApplicationAlreadyExistsException extends Exception {

    private static final long serialVersionUID = -6585628133363244006L;

    public MicoApplicationAlreadyExistsException(String shortName, String version) {
        super("Application '" + shortName + "' '" + version + "' already exists.");
    }

    public MicoApplicationAlreadyExistsException(Long id) {
        super("Application '" + id + "' already exists.");
    }

    public MicoApplicationAlreadyExistsException() {
        super("Application already exists.");
    }

}
