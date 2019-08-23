package io.github.ust.mico.core.exception;

public class KFConnectorInstanceNotFoundException extends Exception {

    private static final long serialVersionUID = -935780471998542258L;

    public KFConnectorInstanceNotFoundException(String instanceId, String version) {
        super("KafkaFaasConnector with instance ID '" + instanceId + "' in '" + version + "' not found!");
    }

    public KFConnectorInstanceNotFoundException(String instanceId) {
        super("KafkaFaasConnector with instance ID '" + instanceId + "' not found!");
    }

    public KFConnectorInstanceNotFoundException() {
        super("KafkaFaasConnector was not found!");
    }

}
