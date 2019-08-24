.. java:import:: io.github.ust.mico.core.configuration MicoKubernetesConfig

.. java:import:: io.github.ust.mico.core.configuration OpenFaaSConfig

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.service MicoKubernetesClient

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: java.net MalformedURLException

.. java:import:: java.net URL

.. java:import:: java.util List

.. java:import:: java.util Optional

OpenFaasBroker
==============

.. java:package:: io.github.ust.mico.core.broker
   :noindex:

.. java:type:: @Slf4j @Service public class OpenFaasBroker

Fields
------
OPEN_FAAS_UI_PROTOCOL
^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String OPEN_FAAS_UI_PROTOCOL
   :outertype: OpenFaasBroker

   The supported protocol of the OpenFaaS UI

micoKubernetesClient
^^^^^^^^^^^^^^^^^^^^

.. java:field:: @Autowired  MicoKubernetesClient micoKubernetesClient
   :outertype: OpenFaasBroker

micoKubernetesConfig
^^^^^^^^^^^^^^^^^^^^

.. java:field:: @Autowired  MicoKubernetesConfig micoKubernetesConfig
   :outertype: OpenFaasBroker

openFaaSConfig
^^^^^^^^^^^^^^

.. java:field:: @Autowired  OpenFaaSConfig openFaaSConfig
   :outertype: OpenFaasBroker

Methods
-------
getExternalAddress
^^^^^^^^^^^^^^^^^^

.. java:method:: public Optional<URL> getExternalAddress() throws MalformedURLException, KubernetesResourceException
   :outertype: OpenFaasBroker

   Requests the external address of the OpenFaaS UI and returns it or \ ``null``\  if OpenFaaS does not exist.

   :throws MalformedURLException: if the address is not in the URL format.
   :throws KubernetesResourceException: if the IP address or the ports of the external gateway svc can't be retrieved
   :return: the external address of the OpenFaaS UI or \ ``null``\ .

