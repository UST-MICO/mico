package io.github.ust.mico.core.exception;

public class KafkaFaasConnectorInstanceNotFoundException extends Exception {

    private static final long serialVersionUID = -935780471998542258L;

    public KafkaFaasConnectorInstanceNotFoundException(String instanceId, String version) {
        super("KafkaFaasConnector with instance ID '" + instanceId + "' in '" + version + "' not found!");
    }

    public KafkaFaasConnectorInstanceNotFoundException(String instanceId) {
        super("KafkaFaasConnector with instance ID '" + instanceId + "' not found!");
    }

    public KafkaFaasConnectorInstanceNotFoundException() {
        super("KafkaFaasConnector was not found!");
    }

}
