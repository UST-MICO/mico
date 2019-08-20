.. java:import:: io.github.ust.mico.core.exception MicoServiceInterfaceAlreadyExistsException

.. java:import:: io.github.ust.mico.core.exception MicoServiceInterfaceNotFoundException

.. java:import:: io.github.ust.mico.core.exception MicoServiceIsDeployedException

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.persistence MicoServiceInterfaceRepository

.. java:import:: io.github.ust.mico.core.service MicoKubernetesClient

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

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

.. java:method:: public void deleteMicoServiceInterface(MicoService micoService, String serviceInterfaceName) throws MicoServiceIsDeployedException
   :outertype: MicoServiceInterfaceBroker

getInterfaceOfServiceByName
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceInterface getInterfaceOfServiceByName(String shortName, String version, String interfaceName) throws MicoServiceInterfaceNotFoundException
   :outertype: MicoServiceInterfaceBroker

getInterfacesOfService
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoServiceInterface> getInterfacesOfService(String shortName, String version)
   :outertype: MicoServiceInterfaceBroker

persistMicoServiceInterface
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceInterface persistMicoServiceInterface(MicoService micoService, MicoServiceInterface micoServiceInterface) throws MicoServiceInterfaceAlreadyExistsException, MicoServiceIsDeployedException
   :outertype: MicoServiceInterfaceBroker

updateMicoServiceInterface
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceInterface updateMicoServiceInterface(MicoService micoService, String serviceInterfaceName, MicoServiceInterface micoServiceInterface) throws MicoServiceInterfaceNotFoundException, MicoServiceIsDeployedException
   :outertype: MicoServiceInterfaceBroker

