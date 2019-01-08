package io.github.ust.mico.core;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.swagger.annotations.ApiModelProperty;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.LinkedList;
import java.util.List;

@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id")
@JsonIgnoreProperties(ignoreUnknown = true)
@NodeEntity
public class Service {
    //mandatory fields
    @Id
    @GeneratedValue
    private Long id;
    @ApiModelProperty(required = true)
    private String version; //internal MICO version (Format: X.Y.Z) //TODO: Do we want to rename 'version' to 'micoVersion'?
    @ApiModelProperty(required = true)
    private String shortName;
    @ApiModelProperty(required = true)
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
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Relationship(type = "DEPENDS_ON")
    private List<DependsOn> dependsOn;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Relationship(direction = Relationship.UNDIRECTED)
    private List<ServiceInterface> serviceInterfaces = new LinkedList<>();

    //crawling information
    private String externalVersion;
    private CrawlingSource crawlingSource;
    //docker information
    private String dockerImageName;
    private String dockerImageUri;

    public Service() {
    }

    public Service(String shortName) {
        this.shortName = shortName;
    }

    public Service(String shortName, String version) {
        this.version = version;
        this.shortName = shortName;
    }

    public Service(String shortName, String version, String description) {
        this.version = version;
        this.shortName = shortName;
        this.description = description;
    }

    public DependsOn dependsOn(Service serviceEnd) {
        DependsOn dependsOnObj = new DependsOn(this, serviceEnd);
        this.dependsOn.add(dependsOnObj);
        return dependsOnObj;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<String> getServiceLinks() {
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

    public List<ServiceInterface> getServiceInterfaces() {
        return serviceInterfaces;
    }

    public void setServiceInterfaces(List<ServiceInterface> serviceInterfaces) {
        this.serviceInterfaces = serviceInterfaces;
    }

    public CrawlingSource getCrawlingSource() {
        return crawlingSource;
    }

    public void setCrawlingSource(CrawlingSource crawlingSource) {
        this.crawlingSource = crawlingSource;
    }

    public String getDockerImageName() {
        return dockerImageName;
    }

    public void setDockerImageName(String dockerImageName) {
        this.dockerImageName = dockerImageName;
    }

    public String getDockerImageUri() {
        return dockerImageUri;
    }

    public void setDockerImageUri(String dockerImageUri) {
        this.dockerImageUri = dockerImageUri;
    }

    public String getExternalVersion() {
        return externalVersion;
    }

    public void setExternalVersion(String externalVersion) {
        this.externalVersion = externalVersion;
    }

    @Override
    public String toString() {
        return "Service{" +
            "id=" + id +
            ", version='" + version + '\'' +
            ", shortName='" + shortName + '\'' +
            ", description='" + description + '\'' +
            ", predecessor=" + predecessor +
            ", vcsRoot='" + vcsRoot + '\'' +
            ", name='" + name + '\'' +
            ", dockerfile='" + dockerfile + '\'' +
            ", contact='" + contact + '\'' +
            ", tags=" + tags +
            ", lifecycle='" + lifecycle + '\'' +
            ", links=" + links +
            ", type='" + type + '\'' +
            ", owner='" + owner + '\'' +
            ", dependsOn=" + dependsOn +
            ", serviceInterfaces=" + serviceInterfaces +
            '}';
    }
}
