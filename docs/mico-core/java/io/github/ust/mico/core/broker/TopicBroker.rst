.. java:import:: io.github.ust.mico.core.persistence MicoTopicRepository

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: java.util LinkedList

.. java:import:: java.util List

TopicBroker
===========

.. java:package:: io.github.ust.mico.core.broker
   :noindex:

.. java:type:: @Slf4j @Service public class TopicBroker

Methods
-------
getAllTopics
^^^^^^^^^^^^

.. java:method:: public List<String> getAllTopics()
   :outertype: TopicBroker

   Reads all topics from the topic repository

