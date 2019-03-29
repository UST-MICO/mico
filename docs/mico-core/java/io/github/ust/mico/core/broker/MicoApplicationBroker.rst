.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceDeploymentInfoRequestDTO

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas Link

.. java:import:: org.springframework.stereotype Service

.. java:import:: io.github.ust.mico.core.dto.response.status MicoApplicationStatusResponseDTO

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoApplicationDeploymentStatus

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.github.ust.mico.core.resource ApplicationResource

.. java:import:: io.github.ust.mico.core.service MicoKubernetesClient

.. java:import:: io.github.ust.mico.core.service MicoStatusService

MicoApplicationBroker
=====================

.. java:package:: io.github.ust.mico.core.broker
   :noindex:

.. java:type:: @Slf4j @Service public class MicoApplicationBroker

Methods
-------
addMicoServiceToMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplication addMicoServiceToMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion) throws MicoApplicationNotFoundException, MicoServiceNotFoundException, MicoServiceAlreadyAddedToMicoApplicationException, MicoServiceAddedMoreThanOnceToMicoApplicationException
   :outertype: MicoApplicationBroker

copyAndUpgradeMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplication copyAndUpgradeMicoApplicationByShortNameAndVersion(String shortName, String version, String newVersion) throws MicoApplicationNotFoundException, MicoApplicationAlreadyExistsException
   :outertype: MicoApplicationBroker

createMicoApplication
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplication createMicoApplication(MicoApplication micoApplication) throws MicoApplicationAlreadyExistsException
   :outertype: MicoApplicationBroker

deleteMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void deleteMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException, MicoApplicationIsDeployedException
   :outertype: MicoApplicationBroker

deleteMicoApplicationsByShortName
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void deleteMicoApplicationsByShortName(String shortName) throws MicoApplicationNotFoundException, MicoApplicationIsDeployedException
   :outertype: MicoApplicationBroker

getApplicationDeploymentStatus
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplicationDeploymentStatus getApplicationDeploymentStatus(String shortName, String version)
   :outertype: MicoApplicationBroker

getLinksOfMicoApplication
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Iterable<Link> getLinksOfMicoApplication(MicoApplication application)
   :outertype: MicoApplicationBroker

getMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplication getMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException
   :outertype: MicoApplicationBroker

getMicoApplicationStatusOfMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplicationStatusResponseDTO getMicoApplicationStatusOfMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException
   :outertype: MicoApplicationBroker

getMicoApplications
^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoApplication> getMicoApplications()
   :outertype: MicoApplicationBroker

getMicoApplicationsByShortName
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoApplication> getMicoApplicationsByShortName(String shortName) throws MicoApplicationNotFoundException
   :outertype: MicoApplicationBroker

getMicoServiceDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDeploymentInfo getMicoServiceDeploymentInformation(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoServiceDeploymentInformationNotFoundException, MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException
   :outertype: MicoApplicationBroker

getMicoServicesOfMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoService> getMicoServicesOfMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException
   :outertype: MicoApplicationBroker

removeMicoServiceFromMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplication removeMicoServiceFromMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException
   :outertype: MicoApplicationBroker

updateMicoApplication
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplication updateMicoApplication(String shortName, String version, MicoApplication micoApplication) throws MicoApplicationNotFoundException, ShortNameOfMicoApplicationDoesNotMatchException, VersionOfMicoApplicationDoesNotMatchException
   :outertype: MicoApplicationBroker

updateMicoServiceDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDeploymentInfo updateMicoServiceDeploymentInformation(String applicationShortName, String applicationVersion, String serviceShortName, MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDTO) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException, MicoServiceDeploymentInformationNotFoundException, KubernetesResourceException
   :outertype: MicoApplicationBroker

