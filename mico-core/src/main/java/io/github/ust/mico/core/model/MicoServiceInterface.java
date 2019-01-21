package io.github.ust.mico.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Represents a interface, e.g., REST API, of a {@link MicoService}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@NodeEntity
public class MicoServiceInterface {

    /**
     * The id of this service interface.
     */
    @JsonIgnore
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     *
     */
    @ApiModelProperty(required = true)
    @NotEmpty
    private String serviceInterfaceName;

    /**
     * The list of ports.
     */
    @ApiModelProperty(required = true)
    @Singular
    private List<MicoServicePort> ports;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The public DNS.
     */
    private String publicDns;

    /**
     * Human readable description of this service interface,
     * e.g., the functionality provided.
     */
    private String description;

    /**
     * The protocol of this interface, e.g., HTTPS.
     */
    private String protocol;

    /**
     * The transport protocol, e.g., TCP.
     */
    private String transportProtocol;

}
