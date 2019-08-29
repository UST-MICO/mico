.. java:import:: io.github.ust.mico.core.broker MicoServiceBroker

.. java:import:: io.github.ust.mico.core.configuration KafkaFaasConnectorConfig

.. java:import:: io.github.ust.mico.core.exception MicoServiceAlreadyExistsException

.. java:import:: io.github.ust.mico.core.exception VersionNotSupportedException

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoVersion

.. java:import:: io.github.ust.mico.core.persistence MicoBackgroundJobRepository

.. java:import:: io.github.ust.mico.core.service GitHubCrawler

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.boot SpringApplication

.. java:import:: org.springframework.boot.autoconfigure SpringBootApplication

.. java:import:: org.springframework.boot.context.event ApplicationReadyEvent

.. java:import:: org.springframework.boot.web.client RestTemplateBuilder

.. java:import:: org.springframework.context ApplicationListener

.. java:import:: org.springframework.context.annotation Bean

.. java:import:: org.springframework.context.annotation ComponentScan

.. java:import:: org.springframework.context.annotation FilterType

.. java:import:: org.springframework.core.env Environment

.. java:import:: org.springframework.core.env Profiles

.. java:import:: org.springframework.data.neo4j.repository.config EnableNeo4jRepositories

.. java:import:: org.springframework.data.redis.repository.configuration EnableRedisRepositories

.. java:import:: org.springframework.scheduling.annotation EnableScheduling

.. java:import:: org.springframework.web.client RestTemplate

.. java:import:: java.io IOException

.. java:import:: java.util List

.. java:import:: java.util Objects

.. java:import:: java.util Optional

MicoCoreApplication
===================

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: @Slf4j @SpringBootApplication @EnableNeo4jRepositories @EnableRedisRepositories @EnableScheduling public class MicoCoreApplication implements ApplicationListener<ApplicationReadyEvent>

   Entry point for the MICO core application.

Fields
------
gitHubCrawler
^^^^^^^^^^^^^

.. java:field:: @Autowired  GitHubCrawler gitHubCrawler
   :outertype: MicoCoreApplication

kafkaFaasConnectorConfig
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: @Autowired  KafkaFaasConnectorConfig kafkaFaasConnectorConfig
   :outertype: MicoCoreApplication

micoServiceBroker
^^^^^^^^^^^^^^^^^

.. java:field:: @Autowired  MicoServiceBroker micoServiceBroker
   :outertype: MicoCoreApplication

Methods
-------
main
^^^^

.. java:method:: public static void main(String[] args)
   :outertype: MicoCoreApplication

onApplicationEvent
^^^^^^^^^^^^^^^^^^

.. java:method:: public void onApplicationEvent(ApplicationReadyEvent event)
   :outertype: MicoCoreApplication

   Runs when application is ready.

   :param event: the \ :java:ref:`ApplicationReadyEvent`\

restTemplate
^^^^^^^^^^^^

.. java:method:: @Bean public RestTemplate restTemplate(RestTemplateBuilder builder)
   :outertype: MicoCoreApplication

   :param builder:

   **See also:** \ `RealDeanZhao/autowire-resttemplate.md <https://gist.github.com/RealDeanZhao/38821bc1efeb7e2a9bcd554cc06cdf96>`_\

