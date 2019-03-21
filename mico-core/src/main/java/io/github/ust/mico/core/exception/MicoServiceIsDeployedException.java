package io.github.ust.mico.core.exception;

public class MicoServiceIsDeployedException extends Exception {

    private static final long serialVersionUID = 4519395483945185615L;

    public MicoServiceIsDeployedException(String shortName, String version) {
        super("Service '" + shortName + "' '" + version + "' is currently deployed!");
    }

    public MicoServiceIsDeployedException(String shortName) {
        super("Service '" + shortName + "' is currently deployed!");
    }

    public MicoServiceIsDeployedException(Long id) {
        super("Service '" + id + "' is currently deployed!");
    }

    public MicoServiceIsDeployedException() {
        super("Service is currently deployed!");
    }

}
