.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: io.github.ust.mico.core.dto.request MicoEnvironmentVariableRequestDTO

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

MicoEnvironmentVariable.DefaultNames
====================================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: public enum DefaultNames
   :outertype: MicoEnvironmentVariable

   The default environment variables for a Kafka-enabled MicoServices.

Enum Constants
--------------
KAFKA_BOOTSTRAP_SERVERS
^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final MicoEnvironmentVariable.DefaultNames KAFKA_BOOTSTRAP_SERVERS
   :outertype: MicoEnvironmentVariable.DefaultNames

KAFKA_GROUP_ID
^^^^^^^^^^^^^^

.. java:field:: public static final MicoEnvironmentVariable.DefaultNames KAFKA_GROUP_ID
   :outertype: MicoEnvironmentVariable.DefaultNames

KAFKA_TOPIC_DEAD_LETTER
^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final MicoEnvironmentVariable.DefaultNames KAFKA_TOPIC_DEAD_LETTER
   :outertype: MicoEnvironmentVariable.DefaultNames

KAFKA_TOPIC_INPUT
^^^^^^^^^^^^^^^^^

.. java:field:: public static final MicoEnvironmentVariable.DefaultNames KAFKA_TOPIC_INPUT
   :outertype: MicoEnvironmentVariable.DefaultNames

KAFKA_TOPIC_INVALID_MESSAGE
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final MicoEnvironmentVariable.DefaultNames KAFKA_TOPIC_INVALID_MESSAGE
   :outertype: MicoEnvironmentVariable.DefaultNames

KAFKA_TOPIC_OUTPUT
^^^^^^^^^^^^^^^^^^

.. java:field:: public static final MicoEnvironmentVariable.DefaultNames KAFKA_TOPIC_OUTPUT
   :outertype: MicoEnvironmentVariable.DefaultNames

KAFKA_TOPIC_TEST_MESSAGE_OUTPUT
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final MicoEnvironmentVariable.DefaultNames KAFKA_TOPIC_TEST_MESSAGE_OUTPUT
   :outertype: MicoEnvironmentVariable.DefaultNames

OPENFAAS_GATEWAY
^^^^^^^^^^^^^^^^

.. java:field:: public static final MicoEnvironmentVariable.DefaultNames OPENFAAS_GATEWAY
   :outertype: MicoEnvironmentVariable.DefaultNames

