.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.nio.charset StandardCharsets

.. java:import:: java.text Normalizer

KubernetesNameNormalizer
========================

.. java:package:: io.github.ust.mico.core.util
   :noindex:

.. java:type:: @Slf4j @Component public class KubernetesNameNormalizer

   Normalizes names to be valid Kubernetes resource names.

Methods
-------
normalizeName
^^^^^^^^^^^^^

.. java:method:: public String normalizeName(String name) throws IllegalArgumentException
   :outertype: KubernetesNameNormalizer

   Normalizes a name so it is a valid Kubernetes resource name.

   :return: the normalized name

