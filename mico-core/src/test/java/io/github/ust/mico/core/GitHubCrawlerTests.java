package io.github.ust.mico.core;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

// TODO: Setup proper integration testing with neo4j
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class GitHubCrawlerTests {
    private static final String REPO_URI = "https://api.github.com/repos/octokit/octokit.rb";
    private static final String RELEASE = "v4.12.0";

    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private DependsOnRepository dependsOnRepository;
    @Autowired
    private ServiceInterfaceRepository serviceInterfaceRepository;

    @Test
    public void testGitHubCrawlerLatestRelease() {
        serviceRepository.deleteAll();
        dependsOnRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();

        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        Service service = crawler.crawlGitHubRepoLatestRelease(REPO_URI);
        serviceRepository.save(service);

        Service readService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion());
        assertEquals(service.getShortName(), readService.getShortName());
        assertEquals(service.getDescription(), readService.getDescription());
        assertEquals(service.getId(), readService.getId());
        assertEquals(service.getVersion(), readService.getVersion());
        assertEquals(service.getVcsRoot(), readService.getVcsRoot());
        assertEquals(service.getName(), readService.getName());
    }

    @Test
    public void testGitHubCrawlerSpecificRelease() {
        serviceRepository.deleteAll();
        dependsOnRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();

        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        Service service = crawler.crawlGitHubRepoSpecificRelease(REPO_URI, RELEASE);
        serviceRepository.save(service);

        Service readService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion());
        assertEquals(service.getShortName(), readService.getShortName());
        assertEquals(service.getDescription(), readService.getDescription());
        assertEquals(service.getId(), readService.getId());
        assertEquals(service.getVersion(), readService.getVersion());
        assertEquals(service.getVcsRoot(), readService.getVcsRoot());
        assertEquals(service.getName(), readService.getName());
    }

    @Test
    public void testGitHubCrawlerAllReleases() {
        serviceRepository.deleteAll();
        dependsOnRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();

        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        List<Service> serviceList = crawler.crawlGitHubRepoAllReleases(REPO_URI);
        serviceRepository.saveAll(serviceList);

        Service readService = serviceRepository.findByShortNameAndVersion(serviceList.get(0).getShortName(), serviceList.get(0).getVersion());
        assertEquals(serviceList.get(0).getShortName(), readService.getShortName());
        assertEquals(serviceList.get(0).getDescription(), readService.getDescription());
        assertEquals(serviceList.get(0).getId(), readService.getId());
        assertEquals(serviceList.get(0).getVersion(), readService.getVersion());
        assertEquals(serviceList.get(0).getVcsRoot(), readService.getVcsRoot());
        assertEquals(serviceList.get(0).getName(), readService.getName());
    }

    @Test
    public void cleanupDatabase() {
        serviceRepository.deleteAll();
        dependsOnRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();
    }
}
