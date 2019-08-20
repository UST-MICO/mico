.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoMessage.Type
================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @AllArgsConstructor public enum Type
   :outertype: MicoMessage

   Enumeration for all types of a \ ``MicoInfoMessage``\ .

Enum Constants
--------------
ERROR
^^^^^

.. java:field:: @JsonProperty public static final MicoMessage.Type ERROR
   :outertype: MicoMessage.Type

INFO
^^^^

.. java:field:: @JsonProperty public static final MicoMessage.Type INFO
   :outertype: MicoMessage.Type

WARNING
^^^^^^^

.. java:field:: @JsonProperty public static final MicoMessage.Type WARNING
   :outertype: MicoMessage.Type

Methods
-------
toString
^^^^^^^^

.. java:method:: @Override public String toString()
   :outertype: MicoMessage.Type

