.. java:import:: io.github.ust.mico.core.model MicoEnvironmentVariable

.. java:import:: lombok Getter

.. java:import:: lombok Setter

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.boot.context.properties ConfigurationProperties

.. java:import:: org.springframework.stereotype Component

.. java:import:: javax.validation.constraints NotBlank

.. java:import:: java.util LinkedList

.. java:import:: java.util List

OpenFaaSConfig
==============

.. java:package:: io.github.ust.mico.core.configuration
   :noindex:

.. java:type:: @Slf4j @Component @Setter @Getter @ConfigurationProperties public class OpenFaaSConfig

   Configuration for the OpenFaaS connection.

Methods
-------
getDefaultEnvironmentVariablesForOpenFaaS
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoEnvironmentVariable> getDefaultEnvironmentVariablesForOpenFaaS()
   :outertype: OpenFaaSConfig

