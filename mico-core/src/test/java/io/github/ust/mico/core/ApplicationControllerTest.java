package io.github.ust.mico.core;

import static io.github.ust.mico.core.JsonPathBuilder.*;
import static io.github.ust.mico.core.TestConstants.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Optional;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.ust.mico.core.REST.ApplicationController;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoVersion;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(ApplicationController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
public class ApplicationControllerTest {

    private static final String JSON_PATH_LINKS_SECTION = "$._links.";
    private static final String SELF_HREF = "self.href";

    public static final String APPLICATION_LIST = buildPath(EMBEDDED, "micoApplicationList");
    public static final String SHORT_NAME_PATH = buildPath(ROOT, "shortName");
    public static final String VERSION_MAJOR_PATH = buildPath(ROOT, "version", "majorVersion");
    public static final String VERSION_MINOR_PATH = buildPath(ROOT, "version", "minorVersion");
    public static final String VERSION_PATCH_PATH = buildPath(ROOT, "version", "patchVersion");

    private static final String BASE_PATH = "/applications";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getAllApplications() throws Exception {
        given(applicationRepository.findAll()).willReturn(
                Arrays.asList(MicoApplication.builder().shortName(SHORT_NAME).version(VERSION_1_0_1).build(),
                        MicoApplication.builder().shortName(SHORT_NAME).version(VERSION).build(),
                        MicoApplication.builder().shortName(SHORT_NAME_1).version(VERSION).build()));

        mvc.perform(get("/applications").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath(APPLICATION_LIST + "[*]", hasSize(3)))
                .andExpect(jsonPath(APPLICATION_LIST + "[?(" + SHORT_NAME_MATCHER + "&&" + VERSION_1_0_1_MATCHER + ")]", hasSize(1)))
                .andExpect(jsonPath(APPLICATION_LIST + "[?(" + SHORT_NAME_MATCHER + "&&" + VERSION_MATCHER + ")]", hasSize(1)))
                .andExpect(jsonPath(APPLICATION_LIST + "[?(" + SHORT_NAME_1_MATCHER + "&&" + VERSION_MATCHER + ")]", hasSize(1)))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "self.href", is("http://localhost/applications")))
                .andReturn();

    }

    @Test
    public void getApplicationByShortNameAndVersion() throws Exception {
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION.toString())).willReturn(
                Optional.of(MicoApplication.builder().shortName(SHORT_NAME).version(VERSION).build()));

        mvc.perform(get("/applications/" + SHORT_NAME + "/" + VERSION.toString()).accept(MediaTypes.HAL_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
                .andExpect(jsonPath(VERSION_MAJOR_PATH, is(Integer.valueOf(VERSION.getMajorVersion()))))
                .andExpect(jsonPath(VERSION_MINOR_PATH, is(Integer.valueOf(VERSION.getMinorVersion()))))
                .andExpect(jsonPath(VERSION_PATCH_PATH, is(Integer.valueOf(VERSION.getPatchVersion()))))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/applications/" + SHORT_NAME + "/" + VERSION.toString())))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "applications.href", is("http://localhost/applications")))
                .andReturn();
    }

    @Test
    public void createApplication() throws Exception {
        MicoApplication application = MicoApplication.builder()
                .shortName(SHORT_NAME)
                .version(VERSION)
                .description(DESCRIPTION)
                .build();

        given(applicationRepository.save(any(MicoApplication.class))).willReturn(application);

        final ResultActions result = mvc.perform(post(BASE_PATH)
                .content(mapper.writeValueAsBytes(application))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        result.andExpect(status().isCreated());
    }

    @Test
    public void updateApplication() throws Exception {
        MicoApplication application = MicoApplication.builder()
                .shortName(SHORT_NAME)
                .version(VERSION)
                .description(DESCRIPTION)
                .build();

        MicoApplication updatedApplication = MicoApplication.builder()
                .shortName(SHORT_NAME)
                .version(VERSION)
                .description("newDesc")
                .build();

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION.toString())).willReturn(Optional.of(application));
        given(applicationRepository.save(any(MicoApplication.class))).willReturn(updatedApplication);

        ResultActions resultUpdate = mvc.perform(put(BASE_PATH + SHORT_NAME + "/" + VERSION)
                .content(mapper.writeValueAsBytes(updatedApplication))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(jsonPath("$.id", is(application.getId())))
                .andExpect(jsonPath("$.description", is(updatedApplication.getDescription())))
                .andExpect(jsonPath("$.shortName", is(updatedApplication.getShortName())))
                .andExpect(jsonPath("$.version", is(updatedApplication.getVersion())));

        resultUpdate.andExpect(status().isOk());
    }

}
