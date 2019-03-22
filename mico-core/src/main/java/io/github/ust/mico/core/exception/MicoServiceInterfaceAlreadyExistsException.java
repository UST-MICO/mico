package io.github.ust.mico.core.exception;

public class MicoServiceInterfaceAlreadyExistsException extends Exception {

    private static final long serialVersionUID = -2684242246453189213L;

    public MicoServiceInterfaceAlreadyExistsException(String serviceShortName, String serviceVersion, String serviceInterfaceName) {
        super("An interface with the name '" + serviceInterfaceName + "' is already associated with the service '" + serviceShortName + "' '" + serviceVersion + "'.");
    }

    public MicoServiceInterfaceAlreadyExistsException(Long serviceId, String serviceInterfaceName) {
        super("An interface with the name '" + serviceInterfaceName + "' is already associated with the service '" + serviceId + "'.");
    }

    public MicoServiceInterfaceAlreadyExistsException() {
        super("An interface is already associated with the service.");
    }

}
