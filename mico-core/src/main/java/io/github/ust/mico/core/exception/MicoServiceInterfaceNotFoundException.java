package io.github.ust.mico.core.exception;

public class MicoServiceInterfaceNotFoundException extends Exception {

    private static final long serialVersionUID = 8900030683652471349L;

    public MicoServiceInterfaceNotFoundException(String shortName, String version, String interfaceName) {
        super("MicoServiceInterface" + interfaceName + "of MicoService '" + shortName + "' '" + version + "' was not found!");
    }

    public MicoServiceInterfaceNotFoundException(String shortName, String version) {
        super("MicoService '" + shortName + "' '" + version + "' does not include any interfaces.");
    }

}
