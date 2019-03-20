.. java:import:: com.fasterxml.jackson.databind DeserializationFeature

.. java:import:: com.fasterxml.jackson.databind JsonNode

.. java:import:: com.fasterxml.jackson.databind ObjectMapper

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceCrawlingOrigin

.. java:import:: io.github.ust.mico.core.util KubernetesNameNormalizer

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.stereotype Component

.. java:import:: org.springframework.web.client RestTemplate

.. java:import:: java.io IOException

.. java:import:: java.util LinkedList

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

.. java:method:: public String adaptUriForGitHubApi(String uri)
   :outertype: GitHubCrawler

crawlGitHubRepoLatestRelease
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService crawlGitHubRepoLatestRelease(String uri) throws IOException
   :outertype: GitHubCrawler

crawlGitHubRepoSpecificRelease
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService crawlGitHubRepoSpecificRelease(String uri, String version) throws IOException
   :outertype: GitHubCrawler

getVersionsFromGitHubRepo
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public LinkedList<String> getVersionsFromGitHubRepo(String uri) throws IOException
   :outertype: GitHubCrawler

