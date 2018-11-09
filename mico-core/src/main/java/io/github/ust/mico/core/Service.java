package io.github.ust.mico.core;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.List;

@NodeEntity
public class Service {
    //mandatory fields
    @Id
    @GeneratedValue
    private Long id;
    private String version;
    private String shortName;
    private String description;

    //additional fields
    private Service predecessor;
    private String vcsRoot;
    private String name;
    private String dockerfile;
    private String contact;
    private List<String> tags;
    private String lifecycle;
    private List<String> links;
    private String type;
    private String owner;
    private List<DependsOn> dependsOn;
    private List<ServiceDescription> serviceDescriptions;

    public Service(){};

    public Service(String shortName,String version) {
        this.version = version;
        this.shortName = shortName;
    }

    //TODO: Verify if all are necessary
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Service getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(Service predecessor) {
        this.predecessor = predecessor;
    }

    public String getVcsRoot() {
        return vcsRoot;
    }

    public void setVcsRoot(String vcsRoot) {
        this.vcsRoot = vcsRoot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDockerfile() {
        return dockerfile;
    }

    public void setDockerfile(String dockerfile) {
        this.dockerfile = dockerfile;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(String lifecycle) {
        this.lifecycle = lifecycle;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<DependsOn> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(List<DependsOn> dependsOn) {
        this.dependsOn = dependsOn;
    }

    public List<ServiceDescription> getServiceDescriptions() {
        return serviceDescriptions;
    }

    public void setServiceDescriptions(List<ServiceDescription> serviceDescriptions) {
        this.serviceDescriptions = serviceDescriptions;
    }
}
