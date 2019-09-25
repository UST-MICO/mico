package io.github.ust.mico.core.exception;

import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;

public class DeploymentRequirementsOfKafkaFaasConnectorNotMetException extends Exception {

    private static final long serialVersionUID = -5821314684610488893L;

    public DeploymentRequirementsOfKafkaFaasConnectorNotMetException(MicoServiceDeploymentInfo serviceDeploymentInfo, String reason) {
        super("Deployment requirements not met for KafkaFaasConnector in version '" + serviceDeploymentInfo.getService().getVersion() +
            "' with instance ID '" + serviceDeploymentInfo.getInstanceId() + "'! Reason: " + reason);
    }

    public DeploymentRequirementsOfKafkaFaasConnectorNotMetException(MicoServiceDeploymentInfo serviceDeploymentInfo) {
        super("Deployment requirements not met for KafkaFaasConnector in version '" + serviceDeploymentInfo.getService().getVersion() +
            "' with instance ID '" + serviceDeploymentInfo.getInstanceId() + "'!");
    }

    public DeploymentRequirementsOfKafkaFaasConnectorNotMetException() {
        super("Deployment requirements not met for KafkaFaasConnector!");
    }

}
