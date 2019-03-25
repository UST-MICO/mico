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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.GitHubCrawler;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTests.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
public class GitHubCrawlerIntegrationTests extends Neo4jTestClass {

    private static final String REPO_URI_API = "https://api.github.com/repos/octokit/octokit.rb";
    private static final String REPO_URI_HTML = "https://github.com/octokit/octokit.rb";
    private static final String RELEASE = "v4.12.0";

    private static final String REPO_HELLO_URI_API = "https://api.github.com/repos/UST-MICO/hello";
    private static final String HELLO_REPO_SUB_DIR_DOCKERFILE = "DockerfileSubDir/Dockerfile";

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private GitHubCrawler crawler;

    @Test
    public void testGitHubCrawlerLatestReleaseByApiUri() throws IOException {
        MicoService service = crawler.crawlGitHubRepoLatestRelease(REPO_URI_API);
        serviceRepository.save(service);

        MicoService readService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion()).get();
        assertEquals(service.getShortName(), readService.getShortName());
        assertEquals(service.getDescription(), readService.getDescription());
        assertEquals(service.getId(), readService.getId());
        assertEquals(service.getVersion(), readService.getVersion());
        assertEquals(service.getGitCloneUrl(), readService.getGitCloneUrl());
        assertEquals(service.getName(), readService.getName());
    }

    @Test
    public void testGitHubCrawlerLatestReleaseByHtmlUri() throws IOException {
        MicoService service = crawler.crawlGitHubRepoLatestRelease(REPO_URI_HTML);
        serviceRepository.save(service);

        MicoService readService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion()).get();
        assertEquals(service.getShortName(), readService.getShortName());
        assertEquals(service.getDescription(), readService.getDescription());
        assertEquals(service.getId(), readService.getId());
        assertEquals(service.getVersion(), readService.getVersion());
        assertEquals(service.getGitCloneUrl(), readService.getGitCloneUrl());
        assertEquals(service.getName(), readService.getName());
    }

    @Test
    public void testGitHubCrawlerSpecificRelease() throws IOException {
        MicoService service = crawler.crawlGitHubRepoSpecificRelease(REPO_URI_API, RELEASE);
        serviceRepository.save(service);

        MicoService readService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion()).get();
        assertEquals(service.getShortName(), readService.getShortName());
        assertEquals(service.getDescription(), readService.getDescription());
        assertEquals(service.getId(), readService.getId());
        assertEquals(service.getVersion(), readService.getVersion());
        assertEquals(service.getGitCloneUrl(), readService.getGitCloneUrl());
        assertEquals(service.getName(), readService.getName());
    }

    @Test
    public void invalidRepoNameIsNormalized() throws IOException {
        MicoService service = crawler.crawlGitHubRepoLatestRelease(REPO_URI_API);

        System.out.println("Crawled MICO service from API URI:");
        prettyPrint(service);

        assertEquals("Expected that repo name 'octokit.rb' is normalized", "octokit-rb", service.getShortName());
    }

    @Test
    public void testCrawlerInSubDir() throws IOException {
        MicoService service = crawler.crawlGitHubRepoLatestRelease(REPO_HELLO_URI_API, HELLO_REPO_SUB_DIR_DOCKERFILE);
        assertEquals(HELLO_REPO_SUB_DIR_DOCKERFILE, service.getDockerfilePath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCrawlerInSubDirNotThere() throws IOException {
        String dockerfilePath = HELLO_REPO_SUB_DIR_DOCKERFILE + "NOT_THERE";
        crawler.crawlGitHubRepoLatestRelease(REPO_HELLO_URI_API, dockerfilePath);
    }

    private void prettyPrint(Object object) {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            String json = mapper.writeValueAsString(object);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
