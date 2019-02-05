package io.github.ust.mico.core.model;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a basic port with a port number and port type (protocol).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
public class MicoServicePort {

    /**
     * The id of this service port.
     */
    @JsonIgnore
    @Id
    @GeneratedValue
    private Long id;

    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The port number of the externally exposed port.
     */
    @ApiModelProperty(required = true)
    private int number;

    /**
     * The type (protocol) of the port
     * (Pivio -> transport_protocol).
     */
    @ApiModelProperty(required = true)
    private MicoPortType type = MicoPortType.DEFAULT;

    /**
     * The port of the container.
     */
    @ApiModelProperty(required = true)
    private int targetPort;

}
