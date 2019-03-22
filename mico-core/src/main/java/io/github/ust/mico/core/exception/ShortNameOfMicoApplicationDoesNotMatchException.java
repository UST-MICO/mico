package io.github.ust.mico.core.exception;

public class ShortNameOfMicoApplicationDoesNotMatchException extends Exception {

    private static final long serialVersionUID = 5620123645380506867L;

    public ShortNameOfMicoApplicationDoesNotMatchException() {
        super("Short name of the provided application does not match the request parameter.");
    }

}
