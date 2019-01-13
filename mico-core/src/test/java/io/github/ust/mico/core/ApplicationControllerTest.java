package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.ResultActions;
import scala.App;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ApplicationController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
public class ApplicationControllerTest {

    private static final String JSON_PATH_LINKS_SECTION = "$._links.";
    private static final String SELF_HREF = "self.href";
    private static final String SHORT_NAME = "ApplicationShortName";
    private static final String VERSION = "1.0.0";
    private static final String DESCRIPTION = "Some Application description";

    private static final String BASE_PATH = "/applications/";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ApplicationRepository applicationRepository;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getAllApplications() throws Exception {
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

    @Test
    public void getApplicationByShortNameAndVersion() throws Exception {
        given(applicationRepository.findByShortNameAndVersion("ApplicationShortName", "1.1.0")).willReturn(
            Optional.of(new Application("ApplicationShortName", "1.1.0")));

        mvc.perform(get("/applications/ApplicationShortName/1.1.0").accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.shortName", is("ApplicationShortName")))
            .andExpect(jsonPath("$.version", is("1.1.0")))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/applications/ApplicationShortName/1.1.0")))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "applications.href", is("http://localhost/applications")))
            .andReturn();
    }

    @Test
    public void createApplication() throws Exception {
        Application application = new Application(SHORT_NAME, VERSION);
        application.setDescription(DESCRIPTION);

        given(applicationRepository.save(any(Application.class))).willReturn(application);

        final ResultActions result = mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(application))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isCreated());
    }

    @Test
    public void deleteService() throws Exception {
        Application application = new Application(SHORT_NAME, VERSION, DESCRIPTION);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));

        ResultActions resultDelete = mvc.perform(delete(BASE_PATH + SHORT_NAME + "/" + VERSION)
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        resultDelete.andExpect(status().isOk());
    }

}
