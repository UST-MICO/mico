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

import io.github.ust.mico.core.service.GitHubCrawler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GitHubCrawlerTests extends Neo4jTestClass {

    private static final String REPO_URI_API = "https://api.github.com/repos/octokit/octokit.rb";
    private static final String REPO_URI_HTML = "https://github.com/octokit/octokit.rb";
    private static final String REPO_URI_WITH_SLASH = "https://github.com/octokit/octokit.rb/";
    private static final String REPO_URI_WITH_SPACES = " https://github.com/octokit/octokit.rb ";
    private static final String RELEASE = "v4.12.0";

    @Test
    public void testMakeUriToMatchGitHubApi() {
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);

        assertEquals(REPO_URI_API, crawler.adaptUriForGitHubApi(REPO_URI_HTML));
        assertEquals(REPO_URI_API, crawler.adaptUriForGitHubApi(REPO_URI_WITH_SLASH));
        assertEquals(REPO_URI_API, crawler.adaptUriForGitHubApi(REPO_URI_WITH_SPACES));
    }
}
