package io.github.ust.mico.core;

import io.github.ust.mico.core.REST.ApplicationController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@WebMvcTest(ApplicationController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
public class ApplicationControllerTest {

    private static final String JSON_PATH_LINKS_SECTION = "$._links.";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ApplicationRepository applicationRepository;

    @Test
    public void getCompleteApplicationList() throws Exception {
        given(applicationRepository.findAll()).willReturn(
                Arrays.asList(
                        new Application("ShortName1", "1.0.1"),
                        new Application("ShortName1", "1.0.0"),
                        new Application("ShortName2", "1.0.0")));

        mvc.perform(get("/applications").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$._embedded.applicationList[*]", hasSize(3)))
                .andExpect(jsonPath("$._embedded.applicationList[?(@.shortName =='ShortName1' && @.version == '1.0.0' )]", hasSize(1)))
                .andExpect(jsonPath("$._embedded.applicationList[?(@.shortName =='ShortName1' && @.version == '1.0.1' )]", hasSize(1)))
                .andExpect(jsonPath("$._embedded.applicationList[?(@.shortName =='ShortName2' && @.version == '1.0.0' )]", hasSize(1)))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "self.href", is("http://localhost/applications")))
                .andReturn();

    }
}
