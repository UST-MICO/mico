package io.github.ust.mico.core;

import io.github.ust.mico.core.broker.ServiceInterfaceBroker;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
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
public class ServiceInterfaceBrokerTests {

    @MockBean
    private MicoServiceRepository serviceRepository;

    @Autowired
    private ServiceInterfaceBroker serviceInterfaceBroker;

    @Test
    public void getAllInterfacesOfService() throws Exception {
        MicoServiceInterface serviceInterface0 = new MicoServiceInterface().setServiceInterfaceName("ServiceInterface0");
        MicoServiceInterface serviceInterface1 = new MicoServiceInterface().setServiceInterfaceName("ServiceInterface1");
        List<MicoServiceInterface> serviceInterfaces = Arrays.asList(serviceInterface0, serviceInterface1);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
                Optional.of(new MicoService().setServiceInterfaces(serviceInterfaces)));

        Optional<List<MicoServiceInterface>> optionalMicoServiceInterfaceList = serviceInterfaceBroker.getAllInterfacesOfService(SHORT_NAME, VERSION);

        assertThat(optionalMicoServiceInterfaceList.get().size()).isEqualTo(2);
        assertThat(optionalMicoServiceInterfaceList.get().get(0)).isEqualTo(serviceInterface0);
        assertThat(optionalMicoServiceInterfaceList.get().get(1)).isEqualTo(serviceInterface1);

    }

}
