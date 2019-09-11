package io.github.ust.mico.core.exception;

public class MicoServiceDeploymentInformationNotFoundException extends Exception {

    private static final long serialVersionUID = -3445327578348019034L;

    public MicoServiceDeploymentInformationNotFoundException(String instanceId) {
        super("Service deployment information for service '" + instanceId + "' could not be found.");
    }

    public MicoServiceDeploymentInformationNotFoundException(String applicationShortName, String applicationVersion, String serviceShortName) {
        super("Service deployment information for service '" + serviceShortName + "' in application '" + applicationShortName + "' '" + applicationVersion + "' could not be found.");
    }

    public MicoServiceDeploymentInformationNotFoundException(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion) {
        super("Service deployment information for service '" + serviceShortName + "' '" + serviceVersion + "' in application '" + applicationShortName + "' '" + applicationVersion + "' could not be found.");
    }

    public MicoServiceDeploymentInformationNotFoundException(Long applicationId, Long serviceId) {
        super("Service deployment information for service '" + serviceId + "' in application '" + applicationId + "' could not be found.");
    }

}
