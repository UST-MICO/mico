package io.github.ust.mico.core.exception;

public class MicoServiceAlreadyAddedToMicoApplicationException extends Exception {

    private static final long serialVersionUID = -8117539196261870923L;

    public MicoServiceAlreadyAddedToMicoApplicationException(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion) {
        super("Application '" + applicationShortName + "' '" + applicationVersion + "' already contains service '" + serviceShortName + "' '" + serviceVersion + "'.");
    }

    public MicoServiceAlreadyAddedToMicoApplicationException(Long applicationId, Long serviceId) {
        super("Application '" + applicationId + "' already contains service '" + serviceId + "'.");
    }

    public MicoServiceAlreadyAddedToMicoApplicationException() {
        super("Application already contains service.");
    }

}
