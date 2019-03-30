.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoMessage
===========

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @NodeEntity public class MicoMessage

   A simple message associated with a \ :java:ref:`Type`\ .

Methods
-------
error
^^^^^

.. java:method:: public static final MicoMessage error(String content)
   :outertype: MicoMessage

   Creates a new \ ``MicoMessage``\  instance with the type \ :java:ref:`Type.ERROR`\  and the given message content.

   :param content: the message content as \ ``String``\ .
   :return: a \ :java:ref:`MicoMessage`\ .

info
^^^^

.. java:method:: public static final MicoMessage info(String content)
   :outertype: MicoMessage

   Creates a new \ ``MicoMessage``\  instance with the type \ :java:ref:`Type.INFO`\  and the given message content.

   :param content: the message content as \ ``String``\ .
   :return: a \ :java:ref:`MicoMessage`\ .

warning
^^^^^^^

.. java:method:: public static final MicoMessage warning(String content)
   :outertype: MicoMessage

   Creates a new \ ``MicoMessage``\  instance with the type \ :java:ref:`Type.WARNING`\  and the given message content.

   :param content: the message content as \ ``String``\ .
   :return: a \ :java:ref:`MicoMessage`\ .

