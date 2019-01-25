package io.github.ust.mico.core;

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
    private static final String RELEASE = "v4.12.0";

    @Test
    public void testMakeUriToMatchGitHubApi() {
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);

        assertEquals(REPO_URI_API, crawler.makeUriToMatchGitHubApi(REPO_URI_HTML));
    }
}
