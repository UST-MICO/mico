package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.broker.MicoServiceInterfaceBroker;
import io.github.ust.mico.core.model.MicoPortType;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoServiceInterfaceBrokerTests {

    private static final String INTERFACE_NAME = "interface-name";
    private static final String INTERFACE_NAME_INVALID = "interface_NAME";
    private static final int INTERFACE_PORT = 1024;
    private static final MicoPortType INTERFACE_PORT_TYPE = MicoPortType.TCP;
    private static final int INTERFACE_TARGET_PORT = 1025;
    private static final String INTERFACE_DESCRIPTION = "This is a service interface.";
    private static final String INTERFACE_PUBLIC_DNS = "DNS";

    @MockBean
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoServiceInterfaceBroker micoServiceInterfaceBroker;

    @Test
    public void getAllInterfacesOfService() throws Exception {
        MicoServiceInterface serviceInterface0 = new MicoServiceInterface().setServiceInterfaceName("ServiceInterface0");
        MicoServiceInterface serviceInterface1 = new MicoServiceInterface().setServiceInterfaceName("ServiceInterface1");
        List<MicoServiceInterface> serviceInterfaces = Arrays.asList(serviceInterface0, serviceInterface1);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
                Optional.of(new MicoService().setServiceInterfaces(serviceInterfaces)));

        Optional<List<MicoServiceInterface>> optionalMicoServiceInterfaceList = micoServiceInterfaceBroker.getAllInterfacesOfService(SHORT_NAME, VERSION);

        assertThat(optionalMicoServiceInterfaceList.get().size()).isEqualTo(2);
        assertThat(optionalMicoServiceInterfaceList.get().get(0)).isEqualTo(serviceInterface0);
        assertThat(optionalMicoServiceInterfaceList.get().get(1)).isEqualTo(serviceInterface1);
    }

    @Test
    public void getServiceInterfaceByServiceInterfaceName() throws Exception {
        MicoServiceInterface serviceInterface = getTestServiceInterface();
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
                Optional.of(new MicoService().setServiceInterfaces(CollectionUtils.listOf(serviceInterface))));

        Optional<MicoServiceInterface> micoServiceInterface = micoServiceInterfaceBroker.getServiceInterfaceByServiceInterfaceName(SHORT_NAME, VERSION, INTERFACE_NAME);

        assertThat(micoServiceInterface.get()).isEqualTo(serviceInterface);
    }

    @Test
    public void createServiceInterface() throws Exception {
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
                Optional.of(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION))
        );

        MicoServiceInterface serviceInterface = getTestServiceInterface();

        MicoServiceInterface createdMicoServiceInterface = micoServiceInterfaceBroker.createServiceInterface(SHORT_NAME, VERSION, serviceInterface);

        assertThat(createdMicoServiceInterface).isEqualTo(serviceInterface);
    }

    private MicoServiceInterface getTestServiceInterface() {
        return new MicoServiceInterface()
                .setServiceInterfaceName(INTERFACE_NAME)
                .setPorts(CollectionUtils.listOf(new MicoServicePort()
                        .setPort(INTERFACE_PORT)
                        .setType(INTERFACE_PORT_TYPE)
                        .setTargetPort(INTERFACE_TARGET_PORT)))
                .setDescription(INTERFACE_DESCRIPTION)
                .setPublicDns(INTERFACE_PUBLIC_DNS);
    }

}
