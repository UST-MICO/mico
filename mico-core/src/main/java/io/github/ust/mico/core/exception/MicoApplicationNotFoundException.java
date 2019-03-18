package io.github.ust.mico.core.exception;

public class MicoApplicationNotFoundException extends Exception {

    private static final long serialVersionUID = -5838689647461478220L;

    public MicoApplicationNotFoundException(String shortName, String version) {
        super("Application '" + shortName + "' '" + version + "' was not found!");
    }

    public MicoApplicationNotFoundException(String shortName) {
        super("Application '" + shortName + "' was not found!");
    }

    public MicoApplicationNotFoundException(Long id) {
        super("Application '" + id + "' was not found!");
    }

    public MicoApplicationNotFoundException() {
        super("Application was not found!");
    }
}
