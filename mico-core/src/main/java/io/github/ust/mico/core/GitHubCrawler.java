package io.github.ust.mico.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GitHubCrawler {

    private static final String GITHUB_API = "https://api.github.com";
    private static final String REPOS = "repos";
    private static final String RELEASES = "releases";
    private static final String TAGS = "tags";
    private static final String LATEST = "latest";
    private final RestTemplate restTemplate;

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
            service.setExternalVersion(releaseInfoJson.get("tag_name").textValue());
            service.setCrawlingSource(CrawlingSource.GITHUB);
            service.setVersion(makeExternalVersionInternal(service.getExternalVersion()));
            service.setDescription(basicInfoJson.get("description").textValue());
            service.setName(basicInfoJson.get("full_name").textValue());
            service.setVcsRoot(releaseInfoJson.get("url").textValue());

            return service;
        } catch (IOException|VersionNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Java Regex: ^v?\d+\.\d+\.\d+(-?.+)?$
    // This Java Regex matches the following semantic versioning:
    // 1.0.0, 1.0.0-foo, 1.0.0-0.1.1, 1.0.0-rc.1, v1.0.0, v1.0.0-foo, v1.0.0-0.1.1, v1.0.0-rc.1
    // Java Regex: \d+\.\d+\.\d+
    // This Java Regex (Format: X.Y.Z) will be extracted.
    public String makeExternalVersionInternal(String externalVersion) throws VersionNotSupportedException {
        Pattern patternSemanticVersioning = Pattern.compile("^v?\\d+\\.\\d+\\.\\d+(-?.+)?$");
        Matcher matcherSemanticVersioning = patternSemanticVersioning.matcher(externalVersion);
        Pattern patternInternalVersioning = Pattern.compile("\\d+\\.\\d+\\.\\d+");
        Matcher matcherInternalVersioning = patternInternalVersioning.matcher(externalVersion);

        if (matcherSemanticVersioning.find()) {
            matcherInternalVersioning.find();
            return matcherInternalVersioning.group(0);
        } else {
            throw new VersionNotSupportedException("Version " + externalVersion + " does not match format 'X.Y.Z' or similar.");
        }
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
    //TODO: Rename method - it is not crawling ALL releases
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
                try {
                    Service service = new Service();
                    service.setShortName(shortName);
                    service.setExternalVersion(jsonNode.get("tag_name").textValue());
                    service.setCrawlingSource(CrawlingSource.GITHUB);
                    service.setVersion(makeExternalVersionInternal(service.getExternalVersion()));
                    service.setDescription(description);
                    service.setName(fullName);
                    service.setVcsRoot(jsonNode.get("url").textValue());
                    serviceList.add(service);
                } catch (VersionNotSupportedException e) {
                    e.printStackTrace();
                }
            }

            return serviceList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
