/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.github.ust.mico.core;

import io.github.ust.mico.core.broker.BackgroundTaskBroker;
import io.github.ust.mico.core.configuration.CorsConfig;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.resource.BackgroundTaskResource;
import io.github.ust.mico.core.util.EmbeddedRedisServer;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.github.ust.mico.core.JsonPathBuilder.*;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.TestConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(BackgroundTaskResource.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {CorsConfig.class})
public class BackgroundTaskResourceTest {
    public static @ClassRule
    RuleChain rules = RuleChain.outerRule(EmbeddedRedisServer.runningAt(6379).suppressExceptions());

    private static final String JSON_PATH_LINKS_SECTION = "$._links.";

    public static final String BACKGROUND_TASK_LIST = buildPath(EMBEDDED, "micoBackgroundTaskDTOList");
    public static final String SHORT_NAME_PATH = buildPath(ROOT, "micoServiceShortName");
    public static final String STATUS_PATH = buildPath(ROOT, "status");
    public static final String LINKS_CANCEL_HREF = buildPath(LINKS, "cancel", HREF);
    public static final String LINKS_JOBS_HREF = buildPath(LINKS, "jobs", HREF);

    @Autowired
    CorsConfig corsConfig;

    @MockBean
    private BackgroundTaskBroker backgroundTaskBroker;
    @MockBean
    private MicoApplicationRepository applicationRepository;
    @Autowired
    private MockMvc mvc;

    @Test
    public void getAllJobs() throws Exception {
        List<MicoBackgroundTask> jobList = Arrays.asList(new MicoBackgroundTask().setJob(CompletableFuture.completedFuture(true)).setMicoServiceShortName(SHORT_NAME).setMicoServiceVersion(VERSION).setType(MicoBackgroundTask.Type.BUILD),
            new MicoBackgroundTask().setJob(CompletableFuture.completedFuture(true)).setMicoServiceShortName(SHORT_NAME_1).setMicoServiceVersion(VERSION).setType(MicoBackgroundTask.Type.BUILD),
            new MicoBackgroundTask().setJob(CompletableFuture.completedFuture(true)).setMicoServiceShortName(SHORT_NAME_2).setMicoServiceVersion(VERSION).setType(MicoBackgroundTask.Type.BUILD));

        given(backgroundTaskBroker.getAllJobs()).willReturn(jobList);

        mvc.perform(get("/jobs").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(BACKGROUND_TASK_LIST + "[*]", hasSize(3)))
            .andExpect(jsonPath(BACKGROUND_TASK_LIST + "[?(@.micoServiceShortName=='" + SHORT_NAME + "' && @.micoServiceVersion=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(BACKGROUND_TASK_LIST + "[?(@.micoServiceShortName=='" + SHORT_NAME_1 + "' && @.micoServiceVersion=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(BACKGROUND_TASK_LIST + "[?(@.micoServiceShortName=='" + SHORT_NAME_2 + "' && @.micoServiceVersion=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/jobs")))
            .andReturn();

    }

    @Test
    public void checkStatus() throws Exception {
        MicoService existingService = new MicoService()
            .setId(ID_1)
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(VERSION);
        MicoService existingService2 = new MicoService()
            .setId(ID_1)
            .setShortName(SERVICE_SHORT_NAME_1)
            .setVersion(VERSION);
        MicoApplication existingApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);
        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setApplication(existingApplication)
            .setService(existingService);
        MicoServiceDeploymentInfo serviceDeploymentInfo2 = new MicoServiceDeploymentInfo()
            .setApplication(existingApplication)
            .setService(existingService2);

        existingApplication.getServiceDeploymentInfos().addAll(Arrays.asList(serviceDeploymentInfo, serviceDeploymentInfo2));
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingApplication));

        System.out.println(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION));

        MicoBackgroundTask pendingJob = new MicoBackgroundTask()
            .setJob(CompletableFuture.completedFuture(true))
            .setMicoServiceShortName(SHORT_NAME)
            .setMicoServiceVersion(VERSION)
            .setType(MicoBackgroundTask.Type.BUILD);
        MicoBackgroundTask runningJob = new MicoBackgroundTask()
            .setJob(CompletableFuture.completedFuture(true))
            .setMicoServiceShortName(SHORT_NAME)
            .setMicoServiceVersion(VERSION)
            .setType(MicoBackgroundTask.Type.BUILD)
            .setStatus(MicoBackgroundTask.Status.RUNNING);

        given(backgroundTaskBroker.getJobStatusByApplicationShortNameAndVersion(SHORT_NAME, VERSION))
            .willReturn(new MicoApplicationJobStatus()
                .setApplicationShortName(SHORT_NAME)
                .setApplicationVersion(VERSION)
                .setStatus(MicoBackgroundTask.Status.PENDING)
                .setJobs(Arrays.asList(runningJob, pendingJob)));

        mvc.perform(get("/jobs/" + SHORT_NAME + "/" + VERSION + "/jobstatus").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(STATUS_PATH, is(MicoBackgroundTask.Status.PENDING.toString())))
            .andReturn();
    }

    @Test
    public void getJobById() throws Exception {
        MicoBackgroundTask doneJob = new MicoBackgroundTask()
            .setJob(CompletableFuture.completedFuture(true))
            .setMicoServiceShortName(SHORT_NAME)
            .setMicoServiceVersion(VERSION)
            .setType(MicoBackgroundTask.Type.BUILD)
            .setStatus(MicoBackgroundTask.Status.DONE);

        given(backgroundTaskBroker.getJobById(STRING_ID)).willReturn(Optional.of(doneJob));

        mvc.perform(get("/jobs/" + STRING_ID).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isSeeOther())
            .andExpect(redirectedUrl("/services/" + SHORT_NAME + "/" + VERSION))
            .andReturn();

        MicoBackgroundTask pendingJob = new MicoBackgroundTask()
            .setJob(CompletableFuture.completedFuture(true))
            .setMicoServiceShortName(SHORT_NAME)
            .setMicoServiceVersion(VERSION)
            .setType(MicoBackgroundTask.Type.BUILD);
        pendingJob.setId(STRING_ID);

        given(backgroundTaskBroker.getJobById(STRING_ID)).willReturn(Optional.of(pendingJob));

        mvc.perform(get("/jobs/" + STRING_ID).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andExpect(jsonPath(STATUS_PATH, is(MicoBackgroundTask.Status.PENDING.toString())))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/jobs/" + STRING_ID)))
            .andExpect(jsonPath(LINKS_CANCEL_HREF, is("http://localhost/jobs/" + STRING_ID)))
            .andExpect(jsonPath(LINKS_JOBS_HREF, is("http://localhost/jobs")))
            .andReturn();

        MicoBackgroundTask runningJob = new MicoBackgroundTask()
            .setJob(CompletableFuture.completedFuture(true))
            .setMicoServiceShortName(SHORT_NAME)
            .setMicoServiceVersion(VERSION)
            .setType(MicoBackgroundTask.Type.BUILD)
            .setStatus(MicoBackgroundTask.Status.RUNNING);

        runningJob.setId(STRING_ID);

        given(backgroundTaskBroker.getJobById(STRING_ID)).willReturn(Optional.of(runningJob));

        mvc.perform(get("/jobs/" + STRING_ID).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andExpect(jsonPath(STATUS_PATH, is(MicoBackgroundTask.Status.RUNNING.toString())))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/jobs/" + STRING_ID)))
            .andExpect(jsonPath(LINKS_CANCEL_HREF, is("http://localhost/jobs/" + STRING_ID)))
            .andExpect(jsonPath(LINKS_JOBS_HREF, is("http://localhost/jobs")))
            .andReturn();
    }

    @Test
    public void deleteJob() throws Exception {
        MicoBackgroundTask pendingJob = new MicoBackgroundTask()
            .setJob(CompletableFuture.completedFuture(true))
            .setMicoServiceShortName(SHORT_NAME)
            .setMicoServiceVersion(VERSION)
            .setType(MicoBackgroundTask.Type.BUILD);

        given(backgroundTaskBroker.getJobById(STRING_ID)).willReturn(Optional.of(pendingJob));
        given(backgroundTaskBroker.deleteJob(STRING_ID)).willReturn(Optional.of(pendingJob));

        mvc.perform(delete("/jobs/" + STRING_ID).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent())
            .andReturn();
    }
}
