package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;

public class GitHubCrawler {

    private final RestTemplate restTemplate;

    public GitHubCrawler(RestTemplateBuilder restTemplateBuilder){
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Gets an URI for a GitHub Repo and crawls all necessary information about the service.
     *
     * @param uri URI for the GitHub Repository
     * @return Service object
     */
    public Service crawlGitHubRepo(String uri){
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            JsonNode gitHubJson = mapper.readTree(responseEntity.getBody());
            String tagsUrl = gitHubJson.get("tags_url").textValue();

            ResponseEntity<String> tagsResponse = restTemplate.getForEntity(tagsUrl,String.class);
            JsonNode tagsJson = mapper.readTree(tagsResponse.getBody());

            Service service = new Service(gitHubJson.get("name").textValue(),tagsJson.textValue(),gitHubJson.get("description").textValue());

            service.setName(gitHubJson.get("full_name").textValue());
            service.setVcsRoot(gitHubJson.get("url").textValue());

            return service;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
