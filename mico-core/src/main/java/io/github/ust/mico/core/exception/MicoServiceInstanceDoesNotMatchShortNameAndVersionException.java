package io.github.ust.mico.core.exception;

public class MicoServiceInstanceDoesNotMatchShortNameAndVersionException extends Exception {

    private static final long serialVersionUID = -7092745863409627653L;

    public MicoServiceInstanceDoesNotMatchShortNameAndVersionException(String instanceId, String providedShortName, String providedVersion, String actualShortName, String actualVersion) {
        super("The MICO service deployment with the instance ID '" + instanceId +
            "' does not match the provided short name '" + providedShortName + "' and version '" + providedVersion + "'. " +
            "Actual short name is '" + actualShortName + "' and version '" + actualVersion + "'.");
    }

}
