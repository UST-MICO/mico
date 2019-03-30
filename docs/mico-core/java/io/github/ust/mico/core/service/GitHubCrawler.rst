.. java:import:: java.io IOException

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.stereotype Component

.. java:import:: org.springframework.web.client RestTemplate

.. java:import:: com.fasterxml.jackson.databind DeserializationFeature

.. java:import:: com.fasterxml.jackson.databind JsonNode

.. java:import:: com.fasterxml.jackson.databind ObjectMapper

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceCrawlingOrigin

.. java:import:: io.github.ust.mico.core.util KubernetesNameNormalizer

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.apache.commons.lang3 StringUtils

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.lang Nullable

.. java:import:: org.springframework.web.client HttpClientErrorException

.. java:import:: org.springframework.web.client HttpStatusCodeException

.. java:import:: org.springframework.web.util UriComponents

.. java:import:: org.springframework.web.util UriComponentsBuilder

.. java:import:: java.net URI

GitHubCrawler
=============

.. java:package:: io.github.ust.mico.core.service
   :noindex:

.. java:type:: @Slf4j @Component public class GitHubCrawler

Constructors
------------
GitHubCrawler
^^^^^^^^^^^^^

.. java:constructor:: @Autowired public GitHubCrawler(RestTemplate restTemplate, KubernetesNameNormalizer kubernetesNameNormalizer)
   :outertype: GitHubCrawler

Methods
-------
adaptUriForGitHubApi
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public String adaptUriForGitHubApi(String url)
   :outertype: GitHubCrawler

crawlGitHubRepoLatestRelease
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService crawlGitHubRepoLatestRelease(String gitHubRepoUrl, String dockerfilePath) throws IOException
   :outertype: GitHubCrawler

crawlGitHubRepoLatestRelease
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService crawlGitHubRepoLatestRelease(String gitHubRepoUrl) throws IOException
   :outertype: GitHubCrawler

crawlGitHubRepoSpecificRelease
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService crawlGitHubRepoSpecificRelease(String gitHubRepoUrl, String version, String dockerfilePath) throws IOException
   :outertype: GitHubCrawler

crawlGitHubRepoSpecificRelease
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService crawlGitHubRepoSpecificRelease(String gitHubRepoUrl, String version) throws IOException
   :outertype: GitHubCrawler

getVersionsFromGitHubRepo
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<String> getVersionsFromGitHubRepo(String gitHubRepoUrl) throws IOException
   :outertype: GitHubCrawler

