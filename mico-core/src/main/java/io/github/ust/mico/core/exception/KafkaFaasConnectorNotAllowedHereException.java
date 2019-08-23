package io.github.ust.mico.core.exception;

public class KafkaFaasConnectorNotAllowedHereException extends Exception {

    private static final long serialVersionUID = -6352481291495405718L;

    public KafkaFaasConnectorNotAllowedHereException() {
        super("KafkaFaasConnector is not allowed here. Use special API endpoints instead.");
    }
}
