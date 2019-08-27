package io.github.ust.mico.core.exception;

public class MicoApplicationDoesNotIncludeKFConnectorInstanceException extends Exception {

    private static final long serialVersionUID = -5147491324525639914L;

    public MicoApplicationDoesNotIncludeKFConnectorInstanceException(String applicationShortName, String applicationVersion, String instanceId, String kfConnectorVersion) {
        super("Application '" + applicationShortName + "' '" + applicationVersion + "' does not include KafkaFaasConnector with instance ID '" + instanceId + "' in version '" + kfConnectorVersion + "'.");
    }

    public MicoApplicationDoesNotIncludeKFConnectorInstanceException(String applicationShortName, String applicationVersion, String instanceId) {
        super("Application '" + applicationShortName + "' '" + applicationVersion + "' does not include KafkaFaasConnector with instance ID '" + instanceId + "'.");
    }

    public MicoApplicationDoesNotIncludeKFConnectorInstanceException(String applicationShortName, String applicationVersion) {
        super("Application '" + applicationShortName + "' '" + applicationVersion + "' does not include any KafkaFaasConnectors.");
    }

    public MicoApplicationDoesNotIncludeKFConnectorInstanceException(Long applicationId, Long serviceId) {
        super("Application '" + applicationId + "' does not include KafkaFaasConnector '" + serviceId + "'.");
    }

    public MicoApplicationDoesNotIncludeKFConnectorInstanceException() {
        super("Application does not include KafkaFaasConnector.");
    }

}
