package io.github.ust.mico.core.exception;

public class MicoServiceHasDependersException extends Exception {

    private static final long serialVersionUID = -121180952945948245L;

    public MicoServiceHasDependersException(String shortName, String version) {
        super("Service '" + shortName + "' '" + version + "' has dependers, therefore it can't be deleted.");
    }

    public MicoServiceHasDependersException(Long id) {
        super("Service '" + id + "' has dependers, therefore it can't be deleted.");
    }

    public MicoServiceHasDependersException() {
        super("Service has dependers, therefore it can't be deleted.");
    }

}
