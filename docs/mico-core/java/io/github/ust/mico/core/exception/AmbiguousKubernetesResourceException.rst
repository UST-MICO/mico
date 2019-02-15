.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.web.bind.annotation ResponseStatus

AmbiguousKubernetesResourceException
====================================

.. java:package:: io.github.ust.mico.core.exception
   :noindex:

.. java:type:: @ResponseStatus public class AmbiguousKubernetesResourceException extends Exception

   Used to indicate that a query for some Kubernetes resource(s) is ambiguous, e.g., if looking for a single deployment and the result is a list of mroe than one deployment.

Constructors
------------
AmbiguousKubernetesResourceException
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public AmbiguousKubernetesResourceException()
   :outertype: AmbiguousKubernetesResourceException

AmbiguousKubernetesResourceException
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public AmbiguousKubernetesResourceException(String message)
   :outertype: AmbiguousKubernetesResourceException

