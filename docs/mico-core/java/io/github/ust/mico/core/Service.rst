.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: java.util List

Service
=======

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: @NodeEntity public class Service

Constructors
------------
Service
^^^^^^^

.. java:constructor:: public Service()
   :outertype: Service

Service
^^^^^^^

.. java:constructor:: public Service(String shortName, String version)
   :outertype: Service

Methods
-------
getContact
^^^^^^^^^^

.. java:method:: public String getContact()
   :outertype: Service

getDependsOn
^^^^^^^^^^^^

.. java:method:: public List<DependsOn> getDependsOn()
   :outertype: Service

getDescription
^^^^^^^^^^^^^^

.. java:method:: public String getDescription()
   :outertype: Service

getDockerfile
^^^^^^^^^^^^^

.. java:method:: public String getDockerfile()
   :outertype: Service

getLifecycle
^^^^^^^^^^^^

.. java:method:: public String getLifecycle()
   :outertype: Service

getLinks
^^^^^^^^

.. java:method:: public List<String> getLinks()
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

getServiceDescriptions
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ServiceDescription> getServiceDescriptions()
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

setDependsOn
^^^^^^^^^^^^

.. java:method:: public void setDependsOn(List<DependsOn> dependsOn)
   :outertype: Service

setDescription
^^^^^^^^^^^^^^

.. java:method:: public void setDescription(String description)
   :outertype: Service

setDockerfile
^^^^^^^^^^^^^

.. java:method:: public void setDockerfile(String dockerfile)
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

setServiceDescriptions
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void setServiceDescriptions(List<ServiceDescription> serviceDescriptions)
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

