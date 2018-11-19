package io.github.ust.mico.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GitHubCrawlerTests {

    private static final String testRepo = "/repos/UST-MICO/mico";
    private static final String absoluteUri = "https://api.github.com/repos/UST-MICO/mico";

    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    public void testGitHubCrawler(){

        GitHubCrawler crawler = new GitHubCrawler();
        crawler.downloadResource(testRepo);
        crawler.anotherHttpCall(absoluteUri);
        //crawler.simpleHttpCall(absoluteUri);

    }

}
