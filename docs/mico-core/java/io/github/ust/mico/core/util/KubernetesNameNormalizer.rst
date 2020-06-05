.. java:import:: java.nio.charset StandardCharsets

.. java:import:: java.text Normalizer

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: org.springframework.stereotype Component

KubernetesNameNormalizer
========================

.. java:package:: io.github.ust.mico.core.util
   :noindex:

.. java:type:: @Component public class KubernetesNameNormalizer

   Normalizes names to be valid Kubernetes resource names.

Fields
------
MICO_NAME_MAX_SIZE
^^^^^^^^^^^^^^^^^^

.. java:field:: public static final int MICO_NAME_MAX_SIZE
   :outertype: KubernetesNameNormalizer

   A max limit of the MICO names (\ :java:ref:`MicoApplication`\ , \ :java:ref:`MicoService`\  and \ :java:ref:`MicoServiceInterface`\ ) is required because they are used as values of Kubernetes labels that have a limit of 63. Furthermore the name is used to create a UID that adds 9 characters to it. Therefore the limit have to be set to 54.

Methods
-------
createBuildName
^^^^^^^^^^^^^^^

.. java:method:: public String createBuildName(String serviceShortName, String serviceVersion)
   :outertype: KubernetesNameNormalizer

   Creates a build name based on the short name and version of a service.

   :param serviceShortName: the short name of the \ :java:ref:`MicoService`\ .
   :param serviceVersion: the version of the \ :java:ref:`MicoService`\ .
   :return: the name of the //@link Build.

createBuildName
^^^^^^^^^^^^^^^

.. java:method:: public String createBuildName(MicoService service)
   :outertype: KubernetesNameNormalizer

   Creates a build name based on a service.

   :param service: the \ :java:ref:`MicoService`\ .
   :return: the name of the //@link Build.

normalizeName
^^^^^^^^^^^^^

.. java:method:: public String normalizeName(String name) throws IllegalArgumentException
   :outertype: KubernetesNameNormalizer

   Normalizes a name so it is a valid Kubernetes resource name.

   :return: the normalized name

