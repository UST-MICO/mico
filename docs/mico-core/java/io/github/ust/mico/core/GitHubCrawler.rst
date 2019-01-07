.. java:import:: com.fasterxml.jackson.databind DeserializationFeature

.. java:import:: com.fasterxml.jackson.databind JsonNode

.. java:import:: com.fasterxml.jackson.databind ObjectMapper

.. java:import:: org.springframework.boot.web.client RestTemplateBuilder

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.client RestTemplate

.. java:import:: java.io IOException

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util.regex Matcher

.. java:import:: java.util.regex Pattern

GitHubCrawler
=============

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: public class GitHubCrawler

Constructors
------------
GitHubCrawler
^^^^^^^^^^^^^

.. java:constructor:: public GitHubCrawler(RestTemplateBuilder restTemplateBuilder)
   :outertype: GitHubCrawler

Methods
-------
crawlGitHubRepoAllReleases
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<Service> crawlGitHubRepoAllReleases(String uri)
   :outertype: GitHubCrawler

crawlGitHubRepoLatestRelease
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Service crawlGitHubRepoLatestRelease(String uri)
   :outertype: GitHubCrawler

crawlGitHubRepoSpecificRelease
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Service crawlGitHubRepoSpecificRelease(String uri, String version)
   :outertype: GitHubCrawler

makeExternalVersionInternal
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public String makeExternalVersionInternal(String externalVersion) throws VersionNotSupportedException
   :outertype: GitHubCrawler

