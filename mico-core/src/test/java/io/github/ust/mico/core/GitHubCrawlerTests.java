package io.github.ust.mico.core;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.persistence.MicoServiceRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GitHubCrawlerTests extends Neo4jTestClass {

    private static final String REPO_URI_API = "https://api.github.com/repos/octokit/octokit.rb";
    private static final String REPO_URI_HTML = "https://github.com/octokit/octokit.rb";
    private static final String RELEASE = "v4.12.0";

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Test
    @Ignore
    public void testGitHubCrawlerLatestReleaseByApiUri() {
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        MicoService service = crawler.crawlGitHubRepoLatestRelease(REPO_URI_API);
        serviceRepository.save(service);

        MicoService readService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion().toString()).get();
        assertEquals(service.getShortName(), readService.getShortName());
        assertEquals(service.getDescription(), readService.getDescription());
        assertEquals(service.getId(), readService.getId());
        assertEquals(service.getVersion(), readService.getVersion());
        assertEquals(service.getVcsRoot(), readService.getVcsRoot());
        assertEquals(service.getName(), readService.getName());
    }

    @Test
    @Ignore
    public void testGitHubCrawlerLatestReleaseByHtmlUri() {
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        MicoService service = crawler.crawlGitHubRepoLatestRelease(REPO_URI_HTML);
        serviceRepository.save(service);

        MicoService readService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion().toString()).get();
        assertEquals(service.getShortName(), readService.getShortName());
        assertEquals(service.getDescription(), readService.getDescription());
        assertEquals(service.getId(), readService.getId());
        assertEquals(service.getVersion(), readService.getVersion());
        assertEquals(service.getVcsRoot(), readService.getVcsRoot());
        assertEquals(service.getName(), readService.getName());
    }

    @Test
    @Ignore
    public void testGitHubCrawlerSpecificRelease() {
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        MicoService service = crawler.crawlGitHubRepoSpecificRelease(REPO_URI_API, RELEASE);
        serviceRepository.save(service);

        MicoService readService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion().toString()).get();
        assertEquals(service.getShortName(), readService.getShortName());
        assertEquals(service.getDescription(), readService.getDescription());
        assertEquals(service.getId(), readService.getId());
        assertEquals(service.getVersion(), readService.getVersion());
        assertEquals(service.getVcsRoot(), readService.getVcsRoot());
        assertEquals(service.getName(), readService.getName());
    }

    @Test
    @Ignore
    public void testGitHubCrawlerAllReleases() {
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        List<MicoService> serviceList = crawler.crawlGitHubRepoAllReleases(REPO_URI_API);
        serviceRepository.saveAll(serviceList);

        MicoService readService = serviceRepository.findByShortNameAndVersion(serviceList.get(0).getShortName(), serviceList.get(0).getVersion().toString()).get();
        assertEquals(serviceList.get(0).getShortName(), readService.getShortName());
        assertEquals(serviceList.get(0).getDescription(), readService.getDescription());
        assertEquals(serviceList.get(0).getId(), readService.getId());
        assertEquals(serviceList.get(0).getVersion(), readService.getVersion());
        assertEquals(serviceList.get(0).getVcsRoot(), readService.getVcsRoot());
        assertEquals(serviceList.get(0).getName(), readService.getName());
    }

    @Test
    public void testMakeUriToMatchGitHubApi() {
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);

        assertEquals(REPO_URI_API, crawler.makeUriToMatchGitHubApi(REPO_URI_HTML));
    }
}
