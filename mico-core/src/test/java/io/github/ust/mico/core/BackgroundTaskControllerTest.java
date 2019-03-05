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

import io.github.ust.mico.core.configuration.CorsConfig;
import io.github.ust.mico.core.model.MicoBackgroundTask;
import io.github.ust.mico.core.persistence.MicoBackgroundTaskRepository;
import io.github.ust.mico.core.util.EmbeddedRedisServer;
import io.github.ust.mico.core.web.BackgroundTaskController;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static io.github.ust.mico.core.JsonPathBuilder.*;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.TestConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(BackgroundTaskController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {CorsConfig.class})
public class BackgroundTaskControllerTest {
    public static @ClassRule
    RuleChain rules = RuleChain.outerRule(EmbeddedRedisServer.runningAt(6379).suppressExceptions());

    private static final String JSON_PATH_LINKS_SECTION = "$._links.";

    public static final String BACKGROUNDTASK_LIST = buildPath(EMBEDDED, "micoBackgroundTaskList");
    public static final String SHORT_NAME_PATH = buildPath(ROOT, "micoServiceShortName");
    public static final String STATUS_PATH = buildPath(ROOT, "status");
    public static final String LINKS_CANCEL_HREF = buildPath(LINKS, "cancel", HREF);
    public static final String LINKS_JOBS_HREF = buildPath(LINKS, "jobs", HREF);
    @Autowired
    CorsConfig corsConfig;

    @Autowired
    private MicoBackgroundTaskRepository jobRepository;

    @Autowired
    private MockMvc mvc;

    @After
    public void clean() {
        jobRepository.deleteAll();
    }

    @Test
    public void getAllJobs() throws Exception {
        jobRepository.saveAll(
            Arrays.asList(new MicoBackgroundTask(CompletableFuture.completedFuture(true), SHORT_NAME, VERSION, MicoBackgroundTask.Type.BUILD),
                new MicoBackgroundTask(CompletableFuture.completedFuture(true), SHORT_NAME_1, VERSION, MicoBackgroundTask.Type.BUILD),
                new MicoBackgroundTask(CompletableFuture.completedFuture(true), SHORT_NAME_2, VERSION, MicoBackgroundTask.Type.BUILD)));

        mvc.perform(get("/jobs").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(BACKGROUNDTASK_LIST + "[*]", hasSize(3)))
            .andExpect(jsonPath(BACKGROUNDTASK_LIST + "[?(@.micoServiceShortName=='" + SHORT_NAME + "' && @.micoServiceVersion=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(BACKGROUNDTASK_LIST + "[?(@.micoServiceShortName=='" + SHORT_NAME_1 + "' && @.micoServiceVersion=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(BACKGROUNDTASK_LIST + "[?(@.micoServiceShortName=='" + SHORT_NAME_2 + "' && @.micoServiceVersion=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/jobs")))
            .andReturn();

    }

    @Test
    public void getJobById() throws Exception {

        MicoBackgroundTask doneJob = new MicoBackgroundTask(CompletableFuture.completedFuture(true), SHORT_NAME, VERSION, MicoBackgroundTask.Type.BUILD);
        doneJob.setStatus(MicoBackgroundTask.Status.DONE);
        String id = jobRepository.save(doneJob).getId();

        mvc.perform(get("/jobs/" + id).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isSeeOther())
            .andExpect(redirectedUrl("/services/short-name/1.0.0"))
            .andReturn();

        MicoBackgroundTask pendingJob = new MicoBackgroundTask(CompletableFuture.completedFuture(true), SHORT_NAME, VERSION, MicoBackgroundTask.Type.BUILD);
        id = jobRepository.save(pendingJob).getId();

        mvc.perform(get("/jobs/" + id).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andExpect(jsonPath(STATUS_PATH, is(MicoBackgroundTask.Status.PENDING.toString())))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/jobs/" + id)))
            .andExpect(jsonPath(LINKS_CANCEL_HREF, is("http://localhost/jobs/" + id)))
            .andExpect(jsonPath(LINKS_JOBS_HREF, is("http://localhost/jobs")))
            .andReturn();

        MicoBackgroundTask runningJob = new MicoBackgroundTask(CompletableFuture.completedFuture(true), SHORT_NAME, VERSION, MicoBackgroundTask.Type.BUILD);
        runningJob.setStatus(MicoBackgroundTask.Status.RUNNING);
        id = jobRepository.save(runningJob).getId();

        mvc.perform(get("/jobs/" + id).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andExpect(jsonPath(STATUS_PATH, is(MicoBackgroundTask.Status.RUNNING.toString())))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/jobs/" + id)))
            .andExpect(jsonPath(LINKS_CANCEL_HREF, is("http://localhost/jobs/" + id)))
            .andExpect(jsonPath(LINKS_JOBS_HREF, is("http://localhost/jobs")))
            .andReturn();
    }

    @Test
    public void deleteJob() throws Exception {
        MicoBackgroundTask pendingJob = new MicoBackgroundTask(CompletableFuture.completedFuture(true), SHORT_NAME, VERSION, MicoBackgroundTask.Type.BUILD);
        String id = jobRepository.save(pendingJob).getId();
        mvc.perform(delete("/jobs/" + id).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andReturn();

        jobRepository.findById(id);
    }
}
