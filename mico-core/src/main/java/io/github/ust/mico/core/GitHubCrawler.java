package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceCrawlingOrigin;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitHubCrawler {

    // TODO: Remove if not needed
    private static final String GITHUB_API = "https://api.github.com";
    // TODO: Remove if not needed
    private static final String REPOS = "repos";
    private static final String RELEASES = "releases";
    private static final String TAGS = "tags";
    private static final String LATEST = "latest";
    private static final String GITHUB_HTML_URL = "https://github.com/";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/";
    private final RestTemplate restTemplate;

    public GitHubCrawler(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    private MicoService crawlGitHubRepo(String uriBasicInfo, String uriReleaseInfo) {
        ResponseEntity<String> responseBasicInfo = restTemplate.getForEntity(uriBasicInfo, String.class);
        ResponseEntity<String> responseReleaseInfo = restTemplate.getForEntity(uriReleaseInfo, String.class);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            JsonNode basicInfoJson = mapper.readTree(responseBasicInfo.getBody());
            JsonNode releaseInfoJson = mapper.readTree(responseReleaseInfo.getBody());

            return MicoService.builder()
                .shortName(basicInfoJson.get("name").textValue())
                .name(basicInfoJson.get("full_name").textValue())
                .version(releaseInfoJson.get("tag_name").textValue())
                .description(basicInfoJson.get("description").textValue())
                .serviceCrawlingOrigin(MicoServiceCrawlingOrigin.GITHUB)
                .gitCloneUrl(basicInfoJson.get("clone_url").textValue())
                .gitReleaseInfoUrl(releaseInfoJson.get("url").textValue())
                .build();
        } catch (IOException e) {
            // TODO: Better exception handling
            e.printStackTrace();
            return null;
        }
    }

    public MicoService crawlGitHubRepoLatestRelease(String uri) {
        uri = makeUriToMatchGitHubApi(uri);
        String releaseUrl = uri + "/" + RELEASES + "/" + LATEST;
        return crawlGitHubRepo(uri, releaseUrl);
    }

    public MicoService crawlGitHubRepoSpecificRelease(String uri, String version) {
        uri = makeUriToMatchGitHubApi(uri);
        String releaseUrl = uri + "/" + RELEASES + "/" + TAGS + "/" + version;
        return crawlGitHubRepo(uri, releaseUrl);
    }

    //TODO: Rename method - it is not crawling ALL releases
    public List<MicoService> crawlGitHubRepoAllReleases(String uri) {
        uri = makeUriToMatchGitHubApi(uri);
        String uriBasicInfo = uri;
        String uriReleases = uri + "/" + RELEASES;
        ResponseEntity<String> responseBasicInfo = restTemplate.getForEntity(uriBasicInfo, String.class);
        ResponseEntity<String> responseReleaseInfo = restTemplate.getForEntity(uriReleases, String.class); //TODO: If LINK(next) exists in header, we need to do another Request
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ArrayList<MicoService> serviceList = new ArrayList<>();

        try {
            JsonNode basicInfoJson = mapper.readTree(responseBasicInfo.getBody());
            JsonNode releaseInfoJson = mapper.readTree(responseReleaseInfo.getBody());

            String shortName = basicInfoJson.get("name").textValue();
            String description = basicInfoJson.get("description").textValue();
            String fullName = basicInfoJson.get("full_name").textValue();
            String gitCloneUrl = basicInfoJson.get("clone_url").textValue();

            for (JsonNode jsonNode : releaseInfoJson) {

                serviceList.add(MicoService.builder()
                    .shortName(shortName)
                    .name(fullName)
                    .version(jsonNode.get("tag_name").textValue())
                    .description(description)
                    .serviceCrawlingOrigin(MicoServiceCrawlingOrigin.GITHUB)
                    .gitCloneUrl(gitCloneUrl)
                    .gitReleaseInfoUrl(jsonNode.get("url").textValue())
                    .build());

            }
            return serviceList;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String makeUriToMatchGitHubApi(String uri) {
        return uri.replace(GITHUB_HTML_URL, GITHUB_API_URL);
    }

}
