package io.github.ust.mico.core.exception;

public class MicoApplicationIsDeployedException extends Exception {

    private static final long serialVersionUID = -6712310195594027221L;

    public MicoApplicationIsDeployedException(String shortName, String version) {
        super("Application '" + shortName + "' '" + version + "' is currently deployed!");
    }

    public MicoApplicationIsDeployedException(String shortName) {
        super("Application '" + shortName + "' is currently deployed!");
    }

    public MicoApplicationIsDeployedException(Long id) {
        super("Application '" + id + "' is currently deployed!");
    }

    public MicoApplicationIsDeployedException() {
        super("Application is currently deployed!");
    }

}
