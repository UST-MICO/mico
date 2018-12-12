package io.github.ust.mico.core.mapping;

/**
 * Enumeration for all port types, e.g., TCP,
 * supported by MICO.
 */
public enum MicoPortType {
	
	/** Transmission Control Protocol. */
	TCP,
	/** User Datagram Protocol. */
	UDP;
	
	/** Default port type is {@link Type#TCP}. */
	public static MicoPortType DEFAULT = MicoPortType.TCP;

}
