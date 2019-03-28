.. java:import:: java.util ArrayList

.. java:import:: java.util Arrays

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.model MicoMessage.Type

.. java:import:: io.github.ust.mico.core.util CollectionUtils

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok RequiredArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoApplicationDeploymentStatus
===============================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @RequiredArgsConstructor @AllArgsConstructor @Accessors @NodeEntity public class MicoApplicationDeploymentStatus

   Wraps the deployment status of a \ :java:ref:`MicoApplication`\  and some messages (optional) with more detailed information.

Methods
-------
deployed
^^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus deployed(String... messages)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.DEPLOYED`\  as well as a \ ``MicoMessage``\  with the given message content and type \ :java:ref:`Info <Type.INFO>`\ .

   :param messages: one or messages.
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

deployed
^^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus deployed(List<String> messages)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.DEPLOYED`\  as well as a \ ``MicoMessage``\  with the given message content and type \ :java:ref:`Info <Type.INFO>`\ .

   :param messages: the \ :java:ref:`List`\  of messages as \ ``String``\ .
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

deployed
^^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus deployed(String message, Type messageType)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.DEPLOYED`\  as well as a \ ``MicoMessage``\  with the given message content and type.

   :param message: the content of the message.
   :param messageType: the \ :java:ref:`Type`\  of the message.
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

incomplete
^^^^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus incomplete(String... messages)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.INCOMPLETE`\  as well as a \ ``MicoMessage``\  with the given message content and type \ :java:ref:`Error <Type.ERROR>`\ .

   :param messages: one or messages.
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

incomplete
^^^^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus incomplete(List<String> messages)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.INCOMPLETE`\  as well as a \ ``MicoMessage``\  with the given message content and type \ :java:ref:`Error <Type.ERROR>`\ .

   :param messages: the \ :java:ref:`List`\  of messages as \ ``String``\ .
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

incomplete
^^^^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus incomplete(String message, Type messageType)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.INCOMPLETE`\  as well as a \ ``MicoMessage``\  with the given message content and type.

   :param message: the content of the message.
   :param messageType: the \ :java:ref:`Type`\  of the message.
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

pending
^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus pending(String... messages)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.PENDING`\  as well as a \ ``MicoMessage``\  with the given message content and type \ :java:ref:`Info <Type.INFO>`\ .

   :param messages: one or messages.
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

pending
^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus pending(List<String> messages)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.PENDING`\  as well as a \ ``MicoMessage``\  with the given message content and type \ :java:ref:`Info <Type.INFO>`\ .

   :param messages: the \ :java:ref:`List`\  of messages as \ ``String``\ .
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

pending
^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus pending(String message, Type messageType)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.PENDING`\  as well as a \ ``MicoMessage``\  with the given message content and type.

   :param message: the content of the message.
   :param messageType: the \ :java:ref:`Type`\  of the message.
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

undeployed
^^^^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus undeployed(String... messages)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.UNDEPLOYED`\  as well as a \ ``MicoMessage``\  with the given message content and type \ :java:ref:`Info <Type.INFO>`\ .

   :param messages: one or messages.
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

undeployed
^^^^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus undeployed(List<String> messages)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.UNDEPLOYED`\  as well as a \ ``MicoMessage``\  with the given message content and type \ :java:ref:`Info <Type.INFO>`\ .

   :param messages: the \ :java:ref:`List`\  of messages as \ ``String``\ .
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

undeployed
^^^^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus undeployed(String message, Type messageType)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.UNDEPLOYED`\  as well as a \ ``MicoMessage``\  with the given message content and type.

   :param message: the content of the message.
   :param messageType: the \ :java:ref:`Type`\  of the message.
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

unknown
^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus unknown(String... messages)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.UNKNOWN`\  as well as a \ ``MicoMessage``\  with the given message content and type \ :java:ref:`Info <Type.INFO>`\ .

   :param messages: one or messages.
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

unknown
^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus unknown(List<String> messages)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.UNKNOWN`\  as well as a \ ``MicoMessage``\  with the given message content and type \ :java:ref:`Info <Type.INFO>`\ .

   :param messages: the \ :java:ref:`List`\  of messages as \ ``String``\ .
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

unknown
^^^^^^^

.. java:method:: public static final MicoApplicationDeploymentStatus unknown(String message, Type messageType)
   :outertype: MicoApplicationDeploymentStatus

   Creates a new \ ``MicoApplicationDeploymentStatus``\  instance with the value \ :java:ref:`Value.UNKNOWN`\  as well as a \ ``MicoMessage``\  with the given message content and type.

   :param message: the content of the message.
   :param messageType: the \ :java:ref:`Type`\  of the message.
   :return: a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

