.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.web.bind.annotation ResponseStatus

KubernetesResourceException
===========================

.. java:package:: io.github.ust.mico.core.exception
   :noindex:

.. java:type:: @ResponseStatus public class KubernetesResourceException extends Exception

   Used to indicate that there is a problem concerning a Kubernetes resource, e.g., a Deployment cannot be found or there are multiple results for a query for a resource that is expected to be unique.

Constructors
------------
KubernetesResourceException
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public KubernetesResourceException()
   :outertype: KubernetesResourceException

KubernetesResourceException
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public KubernetesResourceException(String message)
   :outertype: KubernetesResourceException

