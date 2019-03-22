package io.github.ust.mico.core.exception;

public class MicoServiceInterfaceNotFoundException extends Exception {

    public MicoServiceInterfaceNotFoundException(String shortName, String version, String interfaceName) {
        super("MicoServiceInterface" + interfaceName + "of MicoService '" + shortName + "' '" + version + "' was not found!");
    }

}
