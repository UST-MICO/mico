package io.github.ust.mico.core.exception;

import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;

public class DeploymentRequirementsNotMetException extends Exception {

    private static final long serialVersionUID = -5821314684610488893L;

    public DeploymentRequirementsNotMetException(MicoServiceDeploymentInfo serviceDeploymentInfo, String reason) {
        super("Deployment requirements not met for MicoService '" + serviceDeploymentInfo.getService().getShortName() +
            "' '" + serviceDeploymentInfo.getService().getVersion() + "' with instance ID '" + serviceDeploymentInfo.getInstanceId() +
            "'! Reason: " + reason);
    }

    public DeploymentRequirementsNotMetException(MicoServiceDeploymentInfo serviceDeploymentInfo) {
        super("Deployment requirements not met for MicoService '" + serviceDeploymentInfo.getService().getShortName() +
            "' '" + serviceDeploymentInfo.getService().getVersion() + "' with instance ID '" + serviceDeploymentInfo.getInstanceId() + "'!");
    }

    public DeploymentRequirementsNotMetException() {
        super("Deployment requirements not met!");
    }

}
