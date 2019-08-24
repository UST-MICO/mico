package io.github.ust.mico.core.exception;

public class KafkaFaasConnectorVersionNotFoundException extends Exception {

    private static final long serialVersionUID = -3213448176922564812L;

    public KafkaFaasConnectorVersionNotFoundException(String version) {
        super("KafkaFaasConnector in version '" + version + "' not found!");
    }

    public KafkaFaasConnectorVersionNotFoundException() {
        super("KafkaFaasConnector was not found!");
    }

}
