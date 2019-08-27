package io.github.ust.mico.core.exception;

public class KafkaFaasConnectorNotAllowedHereException extends Exception {

    private static final long serialVersionUID = -6352481291495405718L;

    public KafkaFaasConnectorNotAllowedHereException() {
        super("KafkaFaasConnector is a special MICO service that is handled differently. " +
            "Therefore there are other API endpoints for handling KafkaFaasConnectors.");
    }
}
