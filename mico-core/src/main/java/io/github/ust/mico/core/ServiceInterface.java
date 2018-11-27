package io.github.ust.mico.core;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

public class ServiceInterface {

    @Id
    @GeneratedValue
    public Long id;

    private String port;
    private String type;
    private String description;
    private String service_name; //with _ to match pivio description //TODO: Do we want to match Pivio description?
    private String protocol;
    private String transport_protocol;
    private String public_dns;

    //TODO: Verify if all are necessary
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getTransport_protocol() {
        return transport_protocol;
    }

    public void setTransport_protocol(String transport_protocol) {
        this.transport_protocol = transport_protocol;
    }

    public String getPublic_dns() {
        return public_dns;
    }

    public void setPublic_dns(String public_dns) {
        this.public_dns = public_dns;
    }
}
