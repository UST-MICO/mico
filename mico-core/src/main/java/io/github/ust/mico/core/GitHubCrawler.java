package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitHubCrawler {

    private final RestTemplate restTemplate;

    private static final String GITHUB_API = "https://api.github.com";
    private static final String REPOS = "repos";
    private static final String RELEASES = "releases";
    private static final String TAGS = "tags";
    private static final String LATEST = "latest";

    public GitHubCrawler(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    private Service crawlGitHubRepo(String uriBasicInfo, String uriReleaseInfo) {
        ResponseEntity<String> responseBasicInfo = restTemplate.getForEntity(uriBasicInfo, String.class);
        ResponseEntity<String> responseReleaseInfo = restTemplate.getForEntity(uriReleaseInfo, String.class);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            JsonNode basicInfoJson = mapper.readTree(responseBasicInfo.getBody());
            JsonNode releaseInfoJson = mapper.readTree(responseReleaseInfo.getBody());

            Service service = new Service();
            service.setShortName(basicInfoJson.get("name").textValue());
            service.setVersion(releaseInfoJson.get("tag_name").textValue());
            service.setDescription(basicInfoJson.get("description").textValue());
            service.setName(basicInfoJson.get("full_name").textValue());
            service.setVcsRoot(releaseInfoJson.get("url").textValue());

            return service;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //TODO: Change input URI to owner + repo
    public Service crawlGitHubRepoLatestRelease(String uri) {
        String releaseUrl = uri + "/" + RELEASES + "/" + LATEST;
        return crawlGitHubRepo(uri, releaseUrl);
    }

    //TODO: Change input URI to owner + repo
    public Service crawlGitHubRepoSpecificRelease(String uri, String version) {
        String releaseUrl = uri + "/" + RELEASES + "/" + TAGS + "/" + version;
        return crawlGitHubRepo(uri, releaseUrl);
    }

    //TODO: Change input URI to owner + repo
    public List<Service> crawlGitHubRepoAllReleases(String uri) {
        String uriBasicInfo = uri;
        String uriReleases = uri + "/" + RELEASES;
        ResponseEntity<String> responseBasicInfo = restTemplate.getForEntity(uriBasicInfo, String.class);
        ResponseEntity<String> responseReleaseInfo = restTemplate.getForEntity(uriReleases, String.class); //TODO: If LINK(next) exists in header, we need to do another Request
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ArrayList<Service> serviceList = new ArrayList<>();

        try {
            JsonNode basicInfoJson = mapper.readTree(responseBasicInfo.getBody());
            JsonNode releaseInfoJson = mapper.readTree(responseReleaseInfo.getBody());

            String shortName = basicInfoJson.get("name").textValue();
            String description = basicInfoJson.get("description").textValue();
            String fullName = basicInfoJson.get("full_name").textValue();

            for (JsonNode jsonNode : releaseInfoJson) {
                Service service = new Service();
                service.setShortName(shortName);
                service.setVersion(jsonNode.get("tag_name").textValue());
                service.setDescription(description);
                service.setName(fullName);
                service.setVcsRoot(jsonNode.get("url").textValue());
                serviceList.add(service);
            }

            return serviceList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
