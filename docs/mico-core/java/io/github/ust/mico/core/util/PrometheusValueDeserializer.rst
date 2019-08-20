.. java:import:: com.fasterxml.jackson.core JsonParser

.. java:import:: com.fasterxml.jackson.databind DeserializationContext

.. java:import:: com.fasterxml.jackson.databind JsonNode

.. java:import:: com.fasterxml.jackson.databind.deser.std StdDeserializer

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: java.io IOException

PrometheusValueDeserializer
===========================

.. java:package:: io.github.ust.mico.core.util
   :noindex:

.. java:type:: @Slf4j public class PrometheusValueDeserializer extends StdDeserializer<Integer>

   Custom deserializer for a response, which is received from Prometheus for CPU load / memory usage requests.

Constructors
------------
PrometheusValueDeserializer
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public PrometheusValueDeserializer()
   :outertype: PrometheusValueDeserializer

Methods
-------
deserialize
^^^^^^^^^^^

.. java:method:: @Override public Integer deserialize(JsonParser parser, DeserializationContext context)
   :outertype: PrometheusValueDeserializer

