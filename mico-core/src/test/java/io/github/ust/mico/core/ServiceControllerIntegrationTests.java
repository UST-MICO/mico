package io.github.ust.mico.core;

import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import static io.github.ust.mico.core.TestConstants.*;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ServiceControllerIntegrationTests extends Neo4jTestClass {

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MockMvc mvc;

    @Test
    public void deleteService() throws Exception {
        MicoService micoService = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION_1_0_1);
        serviceRepository.save(micoService);
        mvc.perform(delete( SERVICES_PATH + "/" + SHORT_NAME + "/" + "/" + VERSION_1_0_1).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent())
            .andReturn();
        Optional<MicoService> micoServiceOptional = serviceRepository.findByShortNameAndVersion(SHORT_NAME,VERSION_1_0_1);
        assertFalse("The service was not deleted properly",micoServiceOptional.isPresent());
    }
}
