.. java:import:: io.fabric8.kubernetes.api.model LoadBalancerIngress

.. java:import:: io.fabric8.kubernetes.api.model LoadBalancerStatus

.. java:import:: io.github.ust.mico.core.exception MicoServiceInterfaceAlreadyExistsException

.. java:import:: io.github.ust.mico.core.exception MicoServiceInterfaceNotFoundException

.. java:import:: io.github.ust.mico.core.exception MicoServiceNotFoundException

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.persistence MicoServiceInterfaceRepository

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util Optional

MicoServiceInterfaceBroker
==========================

.. java:package:: io.github.ust.mico.core.broker
   :noindex:

.. java:type:: @Slf4j @Service public class MicoServiceInterfaceBroker

Methods
-------
deleteMicoServiceInterface
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void deleteMicoServiceInterface(String shortName, String version, String serviceInterfaceName)
   :outertype: MicoServiceInterfaceBroker

getInterfaceOfServiceByName
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceInterface getInterfaceOfServiceByName(String shortName, String version, String interfaceName) throws MicoServiceInterfaceNotFoundException
   :outertype: MicoServiceInterfaceBroker

getInterfacesOfService
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoServiceInterface> getInterfacesOfService(String shortName, String version)
   :outertype: MicoServiceInterfaceBroker

getPublicIpsOfInterfaceByInterfaceName
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<String> getPublicIpsOfInterfaceByInterfaceName(String shortName, String version, String serviceInterfaceName, io.fabric8.kubernetes.api.model.Service kubernetesService)
   :outertype: MicoServiceInterfaceBroker

persistMicoServiceInterface
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceInterface persistMicoServiceInterface(MicoService micoService, MicoServiceInterface micoServiceInterface) throws MicoServiceInterfaceAlreadyExistsException, MicoServiceNotFoundException
   :outertype: MicoServiceInterfaceBroker

updateMicoServiceInterface
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceInterface updateMicoServiceInterface(String shortName, String version, String serviceInterfaceName, MicoServiceInterface micoServiceInterface) throws MicoServiceInterfaceNotFoundException
   :outertype: MicoServiceInterfaceBroker

