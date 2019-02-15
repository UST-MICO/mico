.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Builder

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: java.util List

UiDeploymentInformation
=======================

.. java:package:: io.github.ust.mico.core.dto
   :noindex:

.. java:type:: @Data @Builder @NoArgsConstructor @AllArgsConstructor public class UiDeploymentInformation

Fields
------
availableReplicas
^^^^^^^^^^^^^^^^^

.. java:field::  int availableReplicas
   :outertype: UiDeploymentInformation

interfacesInformation
^^^^^^^^^^^^^^^^^^^^^

.. java:field::  List<UiExternalMicoInterfaceInformation> interfacesInformation
   :outertype: UiDeploymentInformation

podInfo
^^^^^^^

.. java:field::  List<UiPodInfo> podInfo
   :outertype: UiDeploymentInformation

requestedReplicas
^^^^^^^^^^^^^^^^^

.. java:field::  int requestedReplicas
   :outertype: UiDeploymentInformation

