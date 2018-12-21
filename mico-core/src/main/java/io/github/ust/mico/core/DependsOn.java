package io.github.ust.mico.core;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.neo4j.ogm.annotation.*;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "serviceDependee")
@RelationshipEntity(type = "DEPENDS_ON")
public class DependsOn {

    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @StartNode
    private Service service;
    @EndNode
    private Service serviceDependee;
    private String minVersion;
    private String maxVersion;

    public DependsOn() {
    }

    public DependsOn(Service service) {
        this.service = service;
    }

    public DependsOn(Service serviceStart, Service serviceEnd) {
        this.service = serviceStart;
        this.serviceDependee = serviceEnd;
    }

    public DependsOn(Service service, String minVersion) {
        this.service = service;
        this.minVersion = minVersion;
    }

    public DependsOn(Service service, String minVersion, String maxVersion) {
        this.service = service;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
    }

    //TODO: Verify if all are necessary
    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }

    public String getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(String maxVersion) {
        this.maxVersion = maxVersion;
    }

    /*@Override
    public String toString() {
        return "DependsOn{" +
                "service=" + service.toString() +
                ", minVersion='" + minVersion + '\'' +
                ", maxVersion='" + maxVersion + '\'' +
                '}';
    }*/

    public Service getServiceDependee() {
        return serviceDependee;
    }

    public void setServiceDependee(Service serviceDependee) {
        this.serviceDependee = serviceDependee;
    }
}
