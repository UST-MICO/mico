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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.GitHubCrawler;

@Category(IntegrationTests.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class GitHubCrawlerIntegrationTests extends Neo4jTestClass {

    private static final String REPO_URI_API = "https://api.github.com/repos/octokit/octokit.rb";
    private static final String REPO_URI_HTML = "https://github.com/octokit/octokit.rb";
    private static final String RELEASE = "v4.12.0";

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Test
    public void testGitHubCrawlerLatestReleaseByApiUri() throws IOException {
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        MicoService service = crawler.crawlGitHubRepoLatestRelease(REPO_URI_API);
        serviceRepository.save(service);

        MicoService readService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion()).get();
        assertEquals(service.getShortName(), readService.getShortName());
        assertEquals(service.getDescription(), readService.getDescription());
        assertEquals(service.getId(), readService.getId());
        assertEquals(service.getVersion(), readService.getVersion());
        assertEquals(service.getGitCloneUrl(), readService.getGitCloneUrl());
        assertEquals(service.getGitReleaseInfoUrl(), readService.getGitReleaseInfoUrl());
        assertEquals(service.getName(), readService.getName());
    }

    @Test
    public void testGitHubCrawlerLatestReleaseByHtmlUri() throws IOException {
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        MicoService service = crawler.crawlGitHubRepoLatestRelease(REPO_URI_HTML);
        serviceRepository.save(service);

        MicoService readService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion()).get();
        assertEquals(service.getShortName(), readService.getShortName());
        assertEquals(service.getDescription(), readService.getDescription());
        assertEquals(service.getId(), readService.getId());
        assertEquals(service.getVersion(), readService.getVersion());
        assertEquals(service.getGitCloneUrl(), readService.getGitCloneUrl());
        assertEquals(service.getGitReleaseInfoUrl(), readService.getGitReleaseInfoUrl());
        assertEquals(service.getName(), readService.getName());
    }

    @Test
    public void testGitHubCrawlerSpecificRelease() throws IOException {
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        MicoService service = crawler.crawlGitHubRepoSpecificRelease(REPO_URI_API, RELEASE);
        serviceRepository.save(service);

        MicoService readService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion()).get();
        assertEquals(service.getShortName(), readService.getShortName());
        assertEquals(service.getDescription(), readService.getDescription());
        assertEquals(service.getId(), readService.getId());
        assertEquals(service.getVersion(), readService.getVersion());
        assertEquals(service.getGitCloneUrl(), readService.getGitCloneUrl());
        assertEquals(service.getGitReleaseInfoUrl(), readService.getGitReleaseInfoUrl());
        assertEquals(service.getName(), readService.getName());
    }
}
