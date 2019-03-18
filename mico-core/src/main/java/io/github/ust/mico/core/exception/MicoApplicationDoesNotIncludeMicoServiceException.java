package io.github.ust.mico.core.exception;

public class MicoApplicationDoesNotIncludeMicoServiceException extends Exception {

    private static final long serialVersionUID = 7967102365629360380L;

    public MicoApplicationDoesNotIncludeMicoServiceException(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion) {
        super("Application '" + applicationShortName + "' '" + applicationVersion + "' does not include service '" + serviceShortName + "' '" + serviceVersion + "'.");
    }

    public MicoApplicationDoesNotIncludeMicoServiceException(String applicationShortName, String applicationVersion, String serviceShortName) {
        super("Application '" + applicationShortName + "' '" + applicationVersion + "' does not include service '" + serviceShortName + "'.");
    }

    public MicoApplicationDoesNotIncludeMicoServiceException(Long applicationId, Long serviceId) {
        super("Application '" + applicationId + "' does not include service '" + serviceId + "'.");
    }

    public MicoApplicationDoesNotIncludeMicoServiceException() {
        super("Application does not include service.");
    }

}
