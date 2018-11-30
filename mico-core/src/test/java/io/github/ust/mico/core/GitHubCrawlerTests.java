package io.github.ust.mico.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GitHubCrawlerTests {
    private static final String absoluteUri = "https://api.github.com/repos/UST-MICO/mico";

    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private DependsOnRepository dependsOnRepository;
    @Autowired
    private ServiceInterfaceRepository serviceInterfaceRepository;

    @Test
    public void testGitHubCrawler(){
        serviceRepository.deleteAll();
        dependsOnRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();

        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        Service service = crawler.crawlGitHubRepo(absoluteUri);
        serviceRepository.save(service);
        System.out.println(service.toString());
        Service readService = serviceRepository.findByShortName(service.getShortName());
        System.out.println(readService.toString());
        assertEquals(service.getShortName(),readService.getShortName());
        assertEquals(service.getDescription(),readService.getDescription());
        assertEquals(service.getId(),readService.getId());
        assertEquals(service.getVersion(),readService.getVersion());
        assertEquals(service.getVcsRoot(),readService.getVcsRoot());
        assertEquals(service.getName(),readService.getName());
    }

    @Test
    public void cleanupDatabase() {
        serviceRepository.deleteAll();
        dependsOnRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();
    }
}
