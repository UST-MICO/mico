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
    private String serviceInterfaceName;
    private String protocol;
    private String transportProtocol;
    private String publicDns;

    public ServiceInterface(String serviceInterfaceName) {
        this.serviceInterfaceName = serviceInterfaceName;
    }

    public ServiceInterface() {
    }

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

    public String getServiceInterfaceName() {
        return serviceInterfaceName;
    }

    public void setServiceInterfaceName(String serviceInterfaceName) {
        this.serviceInterfaceName = serviceInterfaceName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getTransportProtocol() {
        return transportProtocol;
    }

    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    public String getPublicDns() {
        return publicDns;
    }

    public void setPublicDns(String publicDns) {
        this.publicDns = publicDns;
    }
}
