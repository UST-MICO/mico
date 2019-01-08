.. java:import:: com.fasterxml.jackson.annotation JsonIdentityInfo

.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: com.fasterxml.jackson.annotation ObjectIdGenerators

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: org.neo4j.ogm.annotation Relationship

.. java:import:: java.util List

Service
=======

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: @JsonIdentityInfo @JsonIgnoreProperties @NodeEntity public class Service

Constructors
------------
Service
^^^^^^^

.. java:constructor:: public Service()
   :outertype: Service

Service
^^^^^^^

.. java:constructor:: public Service(String shortName)
   :outertype: Service

Service
^^^^^^^

.. java:constructor:: public Service(String shortName, String version)
   :outertype: Service

Service
^^^^^^^

.. java:constructor:: public Service(String shortName, String version, String description)
   :outertype: Service

Methods
-------
dependsOn
^^^^^^^^^

.. java:method:: public DependsOn dependsOn(Service serviceEnd)
   :outertype: Service

getContact
^^^^^^^^^^

.. java:method:: public String getContact()
   :outertype: Service

getCrawlingSource
^^^^^^^^^^^^^^^^^

.. java:method:: public CrawlingSource getCrawlingSource()
   :outertype: Service

getDependsOn
^^^^^^^^^^^^

.. java:method:: public List<DependsOn> getDependsOn()
   :outertype: Service

getDescription
^^^^^^^^^^^^^^

.. java:method:: public String getDescription()
   :outertype: Service

getDockerImageName
^^^^^^^^^^^^^^^^^^

.. java:method:: public String getDockerImageName()
   :outertype: Service

getDockerImageUri
^^^^^^^^^^^^^^^^^

.. java:method:: public String getDockerImageUri()
   :outertype: Service

getDockerfile
^^^^^^^^^^^^^

.. java:method:: public String getDockerfile()
   :outertype: Service

getExternalVersion
^^^^^^^^^^^^^^^^^^

.. java:method:: public String getExternalVersion()
   :outertype: Service

getId
^^^^^

.. java:method:: public Long getId()
   :outertype: Service

getLifecycle
^^^^^^^^^^^^

.. java:method:: public String getLifecycle()
   :outertype: Service

getName
^^^^^^^

.. java:method:: public String getName()
   :outertype: Service

getOwner
^^^^^^^^

.. java:method:: public String getOwner()
   :outertype: Service

getPredecessor
^^^^^^^^^^^^^^

.. java:method:: public Service getPredecessor()
   :outertype: Service

getServiceInterfaces
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ServiceInterface> getServiceInterfaces()
   :outertype: Service

getServiceLinks
^^^^^^^^^^^^^^^

.. java:method:: public List<String> getServiceLinks()
   :outertype: Service

getShortName
^^^^^^^^^^^^

.. java:method:: public String getShortName()
   :outertype: Service

getTags
^^^^^^^

.. java:method:: public List<String> getTags()
   :outertype: Service

getType
^^^^^^^

.. java:method:: public String getType()
   :outertype: Service

getVcsRoot
^^^^^^^^^^

.. java:method:: public String getVcsRoot()
   :outertype: Service

getVersion
^^^^^^^^^^

.. java:method:: public String getVersion()
   :outertype: Service

setContact
^^^^^^^^^^

.. java:method:: public void setContact(String contact)
   :outertype: Service

setCrawlingSource
^^^^^^^^^^^^^^^^^

.. java:method:: public void setCrawlingSource(CrawlingSource crawlingSource)
   :outertype: Service

setDependsOn
^^^^^^^^^^^^

.. java:method:: public void setDependsOn(List<DependsOn> dependsOn)
   :outertype: Service

setDescription
^^^^^^^^^^^^^^

.. java:method:: public void setDescription(String description)
   :outertype: Service

setDockerImageName
^^^^^^^^^^^^^^^^^^

.. java:method:: public void setDockerImageName(String dockerImageName)
   :outertype: Service

setDockerImageUri
^^^^^^^^^^^^^^^^^

.. java:method:: public void setDockerImageUri(String dockerImageUri)
   :outertype: Service

setDockerfile
^^^^^^^^^^^^^

.. java:method:: public void setDockerfile(String dockerfile)
   :outertype: Service

setExternalVersion
^^^^^^^^^^^^^^^^^^

.. java:method:: public void setExternalVersion(String externalVersion)
   :outertype: Service

setId
^^^^^

.. java:method:: public void setId(Long id)
   :outertype: Service

setLifecycle
^^^^^^^^^^^^

.. java:method:: public void setLifecycle(String lifecycle)
   :outertype: Service

setLinks
^^^^^^^^

.. java:method:: public void setLinks(List<String> links)
   :outertype: Service

setName
^^^^^^^

.. java:method:: public void setName(String name)
   :outertype: Service

setOwner
^^^^^^^^

.. java:method:: public void setOwner(String owner)
   :outertype: Service

setPredecessor
^^^^^^^^^^^^^^

.. java:method:: public void setPredecessor(Service predecessor)
   :outertype: Service

setServiceInterfaces
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void setServiceInterfaces(List<ServiceInterface> serviceInterfaces)
   :outertype: Service

setShortName
^^^^^^^^^^^^

.. java:method:: public void setShortName(String shortName)
   :outertype: Service

setTags
^^^^^^^

.. java:method:: public void setTags(List<String> tags)
   :outertype: Service

setType
^^^^^^^

.. java:method:: public void setType(String type)
   :outertype: Service

setVcsRoot
^^^^^^^^^^

.. java:method:: public void setVcsRoot(String vcsRoot)
   :outertype: Service

setVersion
^^^^^^^^^^

.. java:method:: public void setVersion(String version)
   :outertype: Service

toString
^^^^^^^^

.. java:method:: @Override public String toString()
   :outertype: Service

