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

import io.github.ust.mico.core.broker.BackgroundJobBroker;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.EmbeddedRedisServer;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
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
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("unit-testing")
public class BackgroundJobResourceTest {

    @ClassRule
    public static RuleChain rules = RuleChain.outerRule(EmbeddedRedisServer.runningAt(6379).suppressExceptions());

    private static final String JSON_PATH_LINKS_SECTION = "$._links.";

    public static final String BACKGROUND_JOBS_LIST = buildPath(EMBEDDED, "micoServiceBackgroundJobResponseDTOList");
    public static final String SHORT_NAME_PATH = buildPath(ROOT, "serviceShortName");
    public static final String STATUS_PATH = buildPath(ROOT, "status");
    public static final String LINKS_CANCEL_HREF = buildPath(LINKS, "cancel", HREF);
    public static final String LINKS_JOBS_HREF = buildPath(LINKS, "jobs", HREF);

    @MockBean
    private BackgroundJobBroker backgroundJobBroker;

    @MockBean
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MockMvc mvc;

    @Test
    public void getAllJobs() throws Exception {
        List<MicoServiceBackgroundJob> jobList = CollectionUtils.listOf(new MicoServiceBackgroundJob().setFuture(CompletableFuture.completedFuture(true)).setServiceShortName(SHORT_NAME).setServiceVersion(VERSION).setType(MicoServiceBackgroundJob.Type.BUILD),
            new MicoServiceBackgroundJob().setFuture(CompletableFuture.completedFuture(true)).setServiceShortName(SHORT_NAME_1).setServiceVersion(VERSION).setType(MicoServiceBackgroundJob.Type.BUILD),
            new MicoServiceBackgroundJob().setFuture(CompletableFuture.completedFuture(true)).setServiceShortName(SHORT_NAME_2).setServiceVersion(VERSION).setType(MicoServiceBackgroundJob.Type.BUILD));

        given(backgroundJobBroker.getAllJobs()).willReturn(jobList);

        mvc.perform(get("/jobs").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(BACKGROUND_JOBS_LIST + "[*]", hasSize(3)))
            .andExpect(jsonPath(BACKGROUND_JOBS_LIST + "[?(@.serviceShortName=='" + SHORT_NAME + "' && @.serviceVersion=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(BACKGROUND_JOBS_LIST + "[?(@.serviceShortName=='" + SHORT_NAME_1 + "' && @.serviceVersion=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(BACKGROUND_JOBS_LIST + "[?(@.serviceShortName=='" + SHORT_NAME_2 + "' && @.serviceVersion=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/jobs")))
            .andReturn();
    }

    @Test
    public void checkStatus() throws Exception {
        MicoService service1 = new MicoService()
            .setId(ID_1)
            .setShortName(SERVICE_SHORT_NAME).setVersion(VERSION);

        MicoService service2 = new MicoService()
            .setId(ID_1)
            .setShortName(SERVICE_SHORT_NAME_1).setVersion(VERSION);

        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION);

        MicoServiceDeploymentInfo serviceDeploymentInfo1 = new MicoServiceDeploymentInfo()
            .setService(service1)
            .setInstanceId(INSTANCE_ID_1);
        MicoServiceDeploymentInfo serviceDeploymentInfo2 = new MicoServiceDeploymentInfo()
            .setService(service2)
            .setInstanceId(INSTANCE_ID_2);

        application.getServices().add(service1);
        application.getServices().add(service2);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo1);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo2);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));

        MicoServiceBackgroundJob pendingJob = new MicoServiceBackgroundJob()
            .setFuture(CompletableFuture.completedFuture(true))
            .setServiceShortName(SHORT_NAME)
            .setServiceVersion(VERSION)
            .setType(MicoServiceBackgroundJob.Type.BUILD);

        MicoServiceBackgroundJob runningJob = new MicoServiceBackgroundJob()
            .setFuture(CompletableFuture.completedFuture(true))
            .setServiceShortName(SHORT_NAME)
            .setServiceVersion(VERSION)
            .setType(MicoServiceBackgroundJob.Type.BUILD)
            .setStatus(MicoServiceBackgroundJob.Status.RUNNING);

        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(SHORT_NAME, VERSION))
            .willReturn(new MicoApplicationJobStatus()
                .setApplicationShortName(SHORT_NAME)
                .setApplicationVersion(VERSION)
                .setStatus(MicoServiceBackgroundJob.Status.PENDING)
                .setJobs(Arrays.asList(runningJob, pendingJob)));

        mvc.perform(get("/jobs/" + SHORT_NAME + "/" + VERSION + "/status").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(STATUS_PATH, is(MicoServiceBackgroundJob.Status.PENDING.toString())))
            .andReturn();
    }

    @Test
    public void getJobById() throws Exception {
        MicoServiceBackgroundJob doneJob = new MicoServiceBackgroundJob()
            .setFuture(CompletableFuture.completedFuture(true))
            .setServiceShortName(SHORT_NAME)
            .setServiceVersion(VERSION)
            .setType(MicoServiceBackgroundJob.Type.BUILD)
            .setStatus(MicoServiceBackgroundJob.Status.DONE);

        given(backgroundJobBroker.getJobById(STRING_ID)).willReturn(Optional.of(doneJob));

        mvc.perform(get("/jobs/" + STRING_ID).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isSeeOther())
            .andExpect(redirectedUrl("/services/" + SHORT_NAME + "/" + VERSION))
            .andReturn();

        MicoServiceBackgroundJob pendingJob = new MicoServiceBackgroundJob()
            .setFuture(CompletableFuture.completedFuture(true))
            .setServiceShortName(SHORT_NAME)
            .setServiceVersion(VERSION)
            .setType(MicoServiceBackgroundJob.Type.BUILD);
        pendingJob.setId(STRING_ID);

        given(backgroundJobBroker.getJobById(STRING_ID)).willReturn(Optional.of(pendingJob));

        mvc.perform(get("/jobs/" + STRING_ID).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andExpect(jsonPath(STATUS_PATH, is(MicoServiceBackgroundJob.Status.PENDING.toString())))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/jobs/" + STRING_ID)))
            .andExpect(jsonPath(LINKS_CANCEL_HREF, is("http://localhost/jobs/" + STRING_ID)))
            .andExpect(jsonPath(LINKS_JOBS_HREF, is("http://localhost/jobs")))
            .andReturn();

        MicoServiceBackgroundJob runningJob = new MicoServiceBackgroundJob()
            .setFuture(CompletableFuture.completedFuture(true))
            .setServiceShortName(SHORT_NAME)
            .setServiceVersion(VERSION)
            .setType(MicoServiceBackgroundJob.Type.BUILD)
            .setStatus(MicoServiceBackgroundJob.Status.RUNNING);

        runningJob.setId(STRING_ID);

        given(backgroundJobBroker.getJobById(STRING_ID)).willReturn(Optional.of(runningJob));

        mvc.perform(get("/jobs/" + STRING_ID).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andExpect(jsonPath(STATUS_PATH, is(MicoServiceBackgroundJob.Status.RUNNING.toString())))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/jobs/" + STRING_ID)))
            .andExpect(jsonPath(LINKS_CANCEL_HREF, is("http://localhost/jobs/" + STRING_ID)))
            .andExpect(jsonPath(LINKS_JOBS_HREF, is("http://localhost/jobs")))
            .andReturn();
    }

    @Test
    public void deleteJob() throws Exception {
        MicoServiceBackgroundJob pendingJob = new MicoServiceBackgroundJob()
            .setFuture(CompletableFuture.completedFuture(true))
            .setServiceShortName(SHORT_NAME)
            .setServiceVersion(VERSION)
            .setType(MicoServiceBackgroundJob.Type.BUILD);

        given(backgroundJobBroker.getJobById(STRING_ID)).willReturn(Optional.of(pendingJob));

        mvc.perform(delete("/jobs/" + STRING_ID).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent())
            .andReturn();
    }

}
