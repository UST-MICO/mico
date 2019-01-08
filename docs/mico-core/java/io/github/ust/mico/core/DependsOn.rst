.. java:import:: com.fasterxml.jackson.annotation JsonIdentityInfo

.. java:import:: com.fasterxml.jackson.annotation JsonIgnore

.. java:import:: com.fasterxml.jackson.annotation ObjectIdGenerators

DependsOn
=========

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: @JsonIdentityInfo @RelationshipEntity public class DependsOn

Constructors
------------
DependsOn
^^^^^^^^^

.. java:constructor:: public DependsOn()
   :outertype: DependsOn

DependsOn
^^^^^^^^^

.. java:constructor:: public DependsOn(Service service)
   :outertype: DependsOn

DependsOn
^^^^^^^^^

.. java:constructor:: public DependsOn(Service serviceStart, Service serviceEnd)
   :outertype: DependsOn

DependsOn
^^^^^^^^^

.. java:constructor:: public DependsOn(Service service, String minVersion)
   :outertype: DependsOn

DependsOn
^^^^^^^^^

.. java:constructor:: public DependsOn(Service service, String minVersion, String maxVersion)
   :outertype: DependsOn

Methods
-------
getMaxVersion
^^^^^^^^^^^^^

.. java:method:: public String getMaxVersion()
   :outertype: DependsOn

getMinVersion
^^^^^^^^^^^^^

.. java:method:: public String getMinVersion()
   :outertype: DependsOn

getService
^^^^^^^^^^

.. java:method:: public Service getService()
   :outertype: DependsOn

getServiceDependee
^^^^^^^^^^^^^^^^^^

.. java:method:: public Service getServiceDependee()
   :outertype: DependsOn

setMaxVersion
^^^^^^^^^^^^^

.. java:method:: public void setMaxVersion(String maxVersion)
   :outertype: DependsOn

setMinVersion
^^^^^^^^^^^^^

.. java:method:: public void setMinVersion(String minVersion)
   :outertype: DependsOn

setService
^^^^^^^^^^

.. java:method:: public void setService(Service service)
   :outertype: DependsOn

setServiceDependee
^^^^^^^^^^^^^^^^^^

.. java:method:: public void setServiceDependee(Service serviceDependee)
   :outertype: DependsOn

