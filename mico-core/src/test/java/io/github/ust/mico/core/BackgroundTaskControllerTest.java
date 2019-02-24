package io.github.ust.mico.core;

import io.github.ust.mico.core.configuration.CorsConfig;
import io.github.ust.mico.core.model.MicoBackgroundTask;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.persistence.MicoBackgroundTaskRepository;
import io.github.ust.mico.core.web.BackgroundTaskController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static io.github.ust.mico.core.JsonPathBuilder.EMBEDDED;
import static io.github.ust.mico.core.JsonPathBuilder.buildPath;
import static io.github.ust.mico.core.TestConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(BackgroundTaskController.class)
//@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {CorsConfig.class})
//@SuppressWarnings({"rawtypes", "unchecked"})
public class BackgroundTaskControllerTest {
    private static final String JSON_PATH_LINKS_SECTION = "$._links.";

    public static final String BACKGROUNDTASK_LIST=buildPath(EMBEDDED, "micoBackgroundTaskList");

    @Autowired
    CorsConfig corsConfig;

    @Autowired
    private MicoBackgroundTaskRepository jobRepository;

    @Autowired
    private MockMvc mvc;
    @Test
    public void getAllJobs() throws Exception{
        given(jobRepository.findAll()).willReturn(
            Arrays.asList(new MicoBackgroundTask(CompletableFuture.completedFuture(true), new MicoService().setShortName(SHORT_NAME).setVersion(VERSION), MicoBackgroundTask.Type.BUILD),
                new MicoBackgroundTask(CompletableFuture.completedFuture(true), new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION), MicoBackgroundTask.Type.BUILD),
                new MicoBackgroundTask(CompletableFuture.completedFuture(true), new MicoService().setShortName(SHORT_NAME_2).setVersion(VERSION), MicoBackgroundTask.Type.BUILD)));

        mvc.perform(get("/jobs").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(BACKGROUNDTASK_LIST + "[*]", hasSize(3)))
            .andExpect(jsonPath(BACKGROUNDTASK_LIST + "[?(" + SHORT_NAME_MATCHER + "&& @.version=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(BACKGROUNDTASK_LIST+ "[?(" + SHORT_NAME_1_MATCHER + "&& @.version=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(BACKGROUNDTASK_LIST + "[?(" + SHORT_NAME_2_MATCHER + "&& @.version=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "self.href", is("http://localhost/jobs")))
            .andReturn();

    }

    @Test
    public void getJobById() {

    }

    @Test
    public void deleteJob() {

    }
}
