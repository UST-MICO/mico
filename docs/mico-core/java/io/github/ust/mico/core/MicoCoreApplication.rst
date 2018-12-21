.. java:import:: org.springframework.boot SpringApplication

.. java:import:: org.springframework.boot.autoconfigure SpringBootApplication

.. java:import:: org.springframework.context.annotation Import

.. java:import:: org.springframework.data.neo4j.repository.config EnableNeo4jRepositories

.. java:import:: org.springframework.web.bind.annotation RequestMapping

.. java:import:: org.springframework.web.bind.annotation RestController

MicoCoreApplication
===================

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: @SpringBootApplication @EnableNeo4jRepositories @RestController public class MicoCoreApplication

   Entry point for the MICO core application.

Methods
-------
home
^^^^

.. java:method:: @RequestMapping public String home()
   :outertype: MicoCoreApplication

main
^^^^

.. java:method:: public static void main(String[] args)
   :outertype: MicoCoreApplication

