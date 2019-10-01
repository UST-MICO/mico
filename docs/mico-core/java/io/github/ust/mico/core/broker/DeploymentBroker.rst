.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.github.ust.mico.core.persistence MicoServiceDeploymentInfoRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: io.github.ust.mico.core.service MicoKubernetesClient

.. java:import:: io.github.ust.mico.core.service.imagebuilder ImageBuilder

.. java:import:: io.github.ust.mico.core.util FutureUtils

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.apache.commons.lang3 NotImplementedException

.. java:import:: org.apache.commons.lang3.exception ExceptionUtils

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util Objects

.. java:import:: java.util Optional

.. java:import:: java.util.concurrent CompletableFuture

.. java:import:: java.util.concurrent CompletionException

.. java:import:: java.util.concurrent ExecutionException

.. java:import:: java.util.concurrent TimeoutException

.. java:import:: java.util.stream Collectors

DeploymentBroker
================

.. java:package:: io.github.ust.mico.core.broker
   :noindex:

.. java:type:: @Slf4j @Service public class DeploymentBroker

Methods
-------
checkIfKafkaFaasConnectorIsDeployable
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void checkIfKafkaFaasConnectorIsDeployable(MicoServiceDeploymentInfo kfConnectorDeploymentInfo) throws DeploymentRequirementsOfKafkaFaasConnectorNotMetException
   :outertype: DeploymentBroker

   Checks if the properties of the \ :java:ref:`MicoServiceDeploymentInfo`\  are valid so the corresponding KafkaFaasConnector is considered deployable.

   :param kfConnectorDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :throws DeploymentRequirementsOfKafkaFaasConnectorNotMetException: if the requirements are not met

deployApplication
^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplicationJobStatus deployApplication(String shortName, String version) throws MicoApplicationNotFoundException, MicoServiceInterfaceNotFoundException, DeploymentRequirementsOfKafkaFaasConnectorNotMetException
   :outertype: DeploymentBroker

   Deploys an application with all its included services and KafkaFaasConnector instances.

   :param shortName: the short name of the \ :java:ref:`MicoApplication`\
   :param version: the version of the \ :java:ref:`MicoApplication`\
   :throws MicoApplicationNotFoundException: if the \ :java:ref:`MicoApplication`\  does not exist
   :throws MicoServiceInterfaceNotFoundException: if the \ :java:ref:`MicoServiceInterface`\  does not exist
   :return: the \ :java:ref:`MicoApplicationJobStatus`\

undeployApplication
^^^^^^^^^^^^^^^^^^^

.. java:method:: public void undeployApplication(String shortName, String version) throws MicoApplicationNotFoundException, MicoApplicationIsDeployingException
   :outertype: DeploymentBroker

   Undeploys an application with all its included services and KafkaFaasConnector instances.

   :param shortName: the short name of the \ :java:ref:`MicoApplication`\
   :param version: the version of the \ :java:ref:`MicoApplication`\
   :throws MicoApplicationNotFoundException: if the \ :java:ref:`MicoApplication`\  does not exist
   :throws MicoApplicationIsDeployingException: if the \ :java:ref:`MicoApplication`\  is currently deploying

