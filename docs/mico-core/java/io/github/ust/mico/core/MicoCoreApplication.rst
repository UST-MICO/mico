.. java:import:: io.github.ust.mico.core.persistence MicoBackgroundJobRepository

.. java:import:: org.springframework.boot SpringApplication

.. java:import:: org.springframework.boot.autoconfigure SpringBootApplication

.. java:import:: org.springframework.boot.web.client RestTemplateBuilder

.. java:import:: org.springframework.context.annotation Bean

.. java:import:: org.springframework.context.annotation ComponentScan

.. java:import:: org.springframework.context.annotation FilterType

.. java:import:: org.springframework.data.neo4j.repository.config EnableNeo4jRepositories

.. java:import:: org.springframework.data.redis.repository.configuration EnableRedisRepositories

.. java:import:: org.springframework.scheduling.annotation EnableScheduling

.. java:import:: org.springframework.web.client RestTemplate

MicoCoreApplication
===================

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: @SpringBootApplication @EnableNeo4jRepositories @EnableRedisRepositories @EnableScheduling public class MicoCoreApplication

   Entry point for the MICO core application.

Methods
-------
main
^^^^

.. java:method:: public static void main(String[] args)
   :outertype: MicoCoreApplication

restTemplate
^^^^^^^^^^^^

.. java:method:: @Bean public RestTemplate restTemplate(RestTemplateBuilder builder)
   :outertype: MicoCoreApplication

   :param builder:

   **See also:** \ `RealDeanZhao/autowire-resttemplate.md <https://gist.github.com/RealDeanZhao/38821bc1efeb7e2a9bcd554cc06cdf96>`_\

