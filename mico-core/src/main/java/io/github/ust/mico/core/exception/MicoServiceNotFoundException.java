package io.github.ust.mico.core.exception;

public class MicoServiceNotFoundException extends Exception {

    private static final long serialVersionUID = -935780471998542258L;

    public MicoServiceNotFoundException(String shortName, String version, String instanceId) {
        super("Service '" + shortName + "' '" + version + "' in instance '" + instanceId + "' was not found!");
    }

    public MicoServiceNotFoundException(String shortName, String version) {
        super("Service '" + shortName + "' '" + version + "' was not found!");
    }

    public MicoServiceNotFoundException(String shortName) {
        super("Service '" + shortName + "' was not found!");
    }

    public MicoServiceNotFoundException(Long id) {
        super("Service '" + id + "' was not found!");
    }

    public MicoServiceNotFoundException() {
        super("Service was not found!");
    }

}
