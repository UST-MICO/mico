.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceDeploymentInfoRequestDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceDeploymentInfoResponseDTO

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: org.neo4j.ogm.annotation Relationship

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

MicoServiceDeploymentInfo.ImagePullPolicy
=========================================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @AllArgsConstructor public enum ImagePullPolicy
   :outertype: MicoServiceDeploymentInfo

   Enumeration for the different policies specifying when to pull an image.

Enum Constants
--------------
ALWAYS
^^^^^^

.. java:field:: @JsonProperty public static final MicoServiceDeploymentInfo.ImagePullPolicy ALWAYS
   :outertype: MicoServiceDeploymentInfo.ImagePullPolicy

IF_NOT_PRESENT
^^^^^^^^^^^^^^

.. java:field:: @JsonProperty public static final MicoServiceDeploymentInfo.ImagePullPolicy IF_NOT_PRESENT
   :outertype: MicoServiceDeploymentInfo.ImagePullPolicy

NEVER
^^^^^

.. java:field:: @JsonProperty public static final MicoServiceDeploymentInfo.ImagePullPolicy NEVER
   :outertype: MicoServiceDeploymentInfo.ImagePullPolicy

Methods
-------
toString
^^^^^^^^

.. java:method:: @Override public String toString()
   :outertype: MicoServiceDeploymentInfo.ImagePullPolicy

