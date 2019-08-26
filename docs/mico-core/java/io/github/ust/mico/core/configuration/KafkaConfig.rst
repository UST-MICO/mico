.. java:import:: io.github.ust.mico.core.model MicoEnvironmentVariable

.. java:import:: io.github.ust.mico.core.model MicoEnvironmentVariable.DefaultNames

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.github.ust.mico.core.model MicoTopic

.. java:import:: io.github.ust.mico.core.model MicoTopicRole

.. java:import:: lombok Getter

.. java:import:: lombok Setter

.. java:import:: org.springframework.boot.context.properties ConfigurationProperties

.. java:import:: org.springframework.stereotype Component

.. java:import:: javax.validation.constraints NotBlank

.. java:import:: java.util LinkedList

.. java:import:: java.util List

KafkaConfig
===========

.. java:package:: io.github.ust.mico.core.configuration
   :noindex:

.. java:type:: @Component @Setter @Getter @ConfigurationProperties public class KafkaConfig

   Configuration of the Kafka connection.

Methods
-------
getDefaultEnvironmentVariablesForKafka
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoEnvironmentVariable> getDefaultEnvironmentVariablesForKafka()
   :outertype: KafkaConfig

getDefaultTopics
^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoTopicRole> getDefaultTopics(MicoServiceDeploymentInfo sdi)
   :outertype: KafkaConfig

