package io.github.ust.mico.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GitHubCrawlerTests {

    private static final String testRepo = "/repos/UST-MICO/mico";
    private static final String absoluteUri = "https://api.github.com/repos/UST-MICO/mico";

    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    public void testGitHubCrawler(){
        serviceRepository.deleteAll();
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        Service service = crawler.restTemplateRestCall(absoluteUri);
        serviceRepository.save(service);
    }

}
