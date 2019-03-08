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

package io.github.ust.mico.core.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceCrawlingOrigin;
import io.github.ust.mico.core.util.KubernetesNameNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.LinkedList;

@Slf4j
@Component
public class GitHubCrawler {

    private static final String RELEASES = "releases";
    private static final String TAGS = "tags";
    private static final String LATEST = "latest";
    private static final String GITHUB_HTML_URL = "https://github.com/";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/";

    private final RestTemplate restTemplate;
    private final KubernetesNameNormalizer kubernetesNameNormalizer;

    @Autowired
    public GitHubCrawler(RestTemplate restTemplate, KubernetesNameNormalizer kubernetesNameNormalizer) {
        this.restTemplate = restTemplate;
        this.kubernetesNameNormalizer = kubernetesNameNormalizer;
    }

    private MicoService crawlGitHubRepo(String uriBasicInfo, String uriReleaseInfo) throws IOException {
        log.debug("Crawl GitHub basic information from '{}' and release information from '{}'", uriBasicInfo, uriReleaseInfo);

        ResponseEntity<String> responseBasicInfo = restTemplate.getForEntity(uriBasicInfo, String.class);
        ResponseEntity<String> responseReleaseInfo = restTemplate.getForEntity(uriReleaseInfo, String.class);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            JsonNode basicInfoJson = mapper.readTree(responseBasicInfo.getBody());
            JsonNode releaseInfoJson = mapper.readTree(responseReleaseInfo.getBody());

            String name = basicInfoJson.get("name").textValue();
            String normalizedName = kubernetesNameNormalizer.normalizeName(name);

            return new MicoService()
                .setShortName(normalizedName)
                .setName(basicInfoJson.get("full_name").textValue())
                .setVersion(releaseInfoJson.get("tag_name").textValue())
                .setDescription(basicInfoJson.get("description").textValue())
                .setServiceCrawlingOrigin(MicoServiceCrawlingOrigin.GITHUB)
                .setGitCloneUrl(basicInfoJson.get("clone_url").textValue());

        } catch (IOException e) {
            log.error(e.getStackTrace().toString());
            log.error("Getting exception '{}'", e.getMessage());
            throw e;
        }
    }

    public MicoService crawlGitHubRepoLatestRelease(String uri) throws IOException {
        uri = adaptUriForGitHubApi(uri);
        String releaseUrl = uri + "/" + RELEASES + "/" + LATEST;

        return crawlGitHubRepo(uri, releaseUrl);
    }

    public MicoService crawlGitHubRepoSpecificRelease(String uri, String version) throws IOException {
        uri = adaptUriForGitHubApi(uri);
        String releaseUrl = uri + "/" + RELEASES + "/" + TAGS + "/" + version;

        return crawlGitHubRepo(uri, releaseUrl);
    }

    public String adaptUriForGitHubApi(String uri) {
        uri = uri.trim();
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }

        return uri.replace(GITHUB_HTML_URL, GITHUB_API_URL);
    }

    public LinkedList<String> getVersionsFromGitHubRepo(String uri) throws IOException {
        uri = adaptUriForGitHubApi(uri);
        String releasesUrl = uri + "/" + RELEASES;
        log.debug("Getting release tags from '{}'", releasesUrl);

        ResponseEntity<String> response = restTemplate.getForEntity(releasesUrl, String.class);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            JsonNode responseJson = mapper.readTree(response.getBody());

            LinkedList<String> versionList = new LinkedList<>();
            responseJson.forEach(release -> versionList.add(release.get("tag_name").textValue()));

            return versionList;
        } catch (IOException e) {
            log.error(e.getStackTrace().toString());
            log.error("Getting exception '{}'", e.getMessage());
            throw e;
        }
    }

}
