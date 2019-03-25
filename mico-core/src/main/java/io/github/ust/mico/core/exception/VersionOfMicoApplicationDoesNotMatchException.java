package io.github.ust.mico.core.exception;

public class VersionOfMicoApplicationDoesNotMatchException extends Exception {

    private static final long serialVersionUID = -6688963927659893933L;

    public VersionOfMicoApplicationDoesNotMatchException() {
        super("Version of the provided application does not match the request parameter.");
    }

}
