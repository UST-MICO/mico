.. java:import:: io.github.ust.mico.core.service MicoKubernetesClient

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.beans.factory.annotation Qualifier

.. java:import:: org.springframework.boot.web.client RestTemplateBuilder

.. java:import:: org.springframework.web.client RestTemplate

.. java:import:: org.springframework.web.context WebApplicationContext

.. java:import:: java.net PasswordAuthentication

RestTemplates
=============

.. java:package:: io.github.ust.mico.core.util
   :noindex:

.. java:type:: @Slf4j @Configuration public class RestTemplates

Fields
------
QUALIFIER_AUTHENTICATED_OPEN_FAAS_REST_TEMPLATE
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String QUALIFIER_AUTHENTICATED_OPEN_FAAS_REST_TEMPLATE
   :outertype: RestTemplates

micoKubernetesClient
^^^^^^^^^^^^^^^^^^^^

.. java:field:: @Autowired  MicoKubernetesClient micoKubernetesClient
   :outertype: RestTemplates

Methods
-------
getAuthenticatedOpenFaaSRestTemplate
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Bean @Qualifier @Scope public RestTemplate getAuthenticatedOpenFaaSRestTemplate(RestTemplateBuilder builder)
   :outertype: RestTemplates

   Constructs the rest template to be able to connect the OpenFaaS Portal. It uses the OpenFaaS credentials that are stored inside a Kubernetes secret. The Spring Bean Request Scope in proxy mode \ ``ScopedProxyMode.TARGET_CLASS``\  is used, so that it will be instantiated when it is needed (prevents errors during application context is loading).

   :param builder: the \ :java:ref:`RestTemplateBuilder`\
   :return: the \ :java:ref:`RestTemplate`\

getRestTemplate
^^^^^^^^^^^^^^^

.. java:method:: @Primary @Bean public RestTemplate getRestTemplate(RestTemplateBuilder builder)
   :outertype: RestTemplates

   Prefer the not authenticated rest template

   :param builder:

