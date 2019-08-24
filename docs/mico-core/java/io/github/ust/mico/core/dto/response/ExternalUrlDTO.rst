.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

.. java:import:: java.net URL

ExternalUrlDTO
==============

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class ExternalUrlDTO

Fields
------
externalUrl
^^^^^^^^^^^

.. java:field::  URL externalUrl
   :outertype: ExternalUrlDTO

   An url for a service which is externally reachable.

isExternalUrlAvailable
^^^^^^^^^^^^^^^^^^^^^^

.. java:field::  boolean isExternalUrlAvailable
   :outertype: ExternalUrlDTO

   Is true if the url is available.

