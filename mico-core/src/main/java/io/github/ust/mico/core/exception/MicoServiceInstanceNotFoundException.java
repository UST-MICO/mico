package io.github.ust.mico.core.exception;

public class MicoServiceInstanceNotFoundException extends Exception {

    private static final long serialVersionUID = -4136839099118490824L;

    public MicoServiceInstanceNotFoundException(String shortName, String version, String instanceId) {
        super("Instance of service with shortName '" + shortName + "', version '" + version +
            "' and instanceId '" + instanceId + "' was not found!");
    }

    public MicoServiceInstanceNotFoundException() {
        super("Instance of service was not found!");
    }

}
