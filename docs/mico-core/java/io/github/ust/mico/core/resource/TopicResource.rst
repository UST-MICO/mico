.. java:import:: io.github.ust.mico.core.broker TopicBroker

.. java:import:: io.github.ust.mico.core.dto.response TopicDTO

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.hateoas Resource

.. java:import:: org.springframework.hateoas Resources

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.bind.annotation GetMapping

.. java:import:: org.springframework.web.bind.annotation RequestMapping

.. java:import:: org.springframework.web.bind.annotation RestController

.. java:import:: java.util LinkedList

.. java:import:: java.util List

TopicResource
=============

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class TopicResource

Fields
------
TOPIC_BASE_PATH
^^^^^^^^^^^^^^^

.. java:field:: public static final String TOPIC_BASE_PATH
   :outertype: TopicResource

Methods
-------
getAllTopics
^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<TopicDTO>>> getAllTopics()
   :outertype: TopicResource

