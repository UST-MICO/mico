package io.github.ust.mico.core.model;

/**
 * Enumeration for all port types, e.g., TCP,
 * supported by MICO.
 */
public enum MicoPortType {

    /**
     * Transmission Control Protocol.
     */
    TCP,
    /**
     * User Datagram Protocol.
     */
    UDP;

    /**
     * Default port type is {@link MicoPortType#TCP}.
     */
    public static MicoPortType DEFAULT = MicoPortType.TCP;

}
