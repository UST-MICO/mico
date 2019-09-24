package io.github.ust.mico.core.exception;

public class KafkaFaasConnectorLatestVersionNotFound extends Exception {
    public KafkaFaasConnectorLatestVersionNotFound() {
        super("Could not find the latest version of the KafkaFaaSConnector");
    }
}
