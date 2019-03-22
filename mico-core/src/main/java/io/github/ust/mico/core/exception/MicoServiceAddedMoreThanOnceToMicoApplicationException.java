package io.github.ust.mico.core.exception;

public class MicoServiceAddedMoreThanOnceToMicoApplicationException extends Exception {

    private static final long serialVersionUID = 7772240438461629056L;

    //TODO: update message
    public MicoServiceAddedMoreThanOnceToMicoApplicationException(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion) {
        super("There are multiple MicoServices with identical short names for MicoApplication '" + applicationShortName + "' '" + applicationVersion + "'.");
    }

    public MicoServiceAddedMoreThanOnceToMicoApplicationException(String applicationShortName, String applicationVersion) {
        super("There are multiple MicoServices with identical short names for MicoApplication '" + applicationShortName + "' '" + applicationVersion + "'.");
    }

    public MicoServiceAddedMoreThanOnceToMicoApplicationException() {
        super("There are multiple MicoServices with identical short names for MicoApplication.");
    }

}
