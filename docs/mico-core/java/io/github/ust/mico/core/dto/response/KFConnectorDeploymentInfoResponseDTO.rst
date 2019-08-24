.. java:import:: io.github.ust.mico.core.dto.request KFConnectorDeploymentInfoRequestDTO

.. java:import:: io.github.ust.mico.core.model KFConnectorDeploymentInfo

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

KFConnectorDeploymentInfoResponseDTO
====================================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @NoArgsConstructor @Accessors public class KFConnectorDeploymentInfoResponseDTO extends KFConnectorDeploymentInfoRequestDTO

   DTO for \ :java:ref:`MicoServiceDeploymentInfo`\  intended to use with responses only.

Constructors
------------
KFConnectorDeploymentInfoResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public KFConnectorDeploymentInfoResponseDTO(KFConnectorDeploymentInfo kfConnectorDeploymentInfo)
   :outertype: KFConnectorDeploymentInfoResponseDTO

   Creates an instance of \ ``KFConnectorDeploymentInfoResponseDTO``\  based on a \ ``KFConnectorDeploymentInfo``\ .

   :param kfConnectorDeploymentInfo: the \ :java:ref:`KFConnectorDeploymentInfo`\ .

