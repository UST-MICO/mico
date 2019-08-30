package io.github.ust.mico.core.exception;

public class KafkaFaasConnectorInstanceAlreadyIncludedWithSameVersionInMicoApplicationException extends Exception {

    private static final long serialVersionUID = 6085453870531373041L;

    public KafkaFaasConnectorInstanceAlreadyIncludedWithSameVersionInMicoApplicationException(
        String applicationShortName, String applicationVersion, String instanceId, String version) {
        super("Application '" + applicationShortName + "' '" + applicationVersion +
            "' already contains a KafkaFaasConnector instance the id '" + instanceId + "' and the version '" + version + "'.");
    }

    public KafkaFaasConnectorInstanceAlreadyIncludedWithSameVersionInMicoApplicationException() {
        super("Application already contains a KafkaFaasConnector instance with the same id and version.");
    }

}
