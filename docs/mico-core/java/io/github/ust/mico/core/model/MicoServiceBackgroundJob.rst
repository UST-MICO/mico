.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.springframework.data.redis.core RedisHash

.. java:import:: org.springframework.data.redis.core.index Indexed

.. java:import:: java.io Serializable

.. java:import:: java.util.concurrent CompletableFuture

MicoServiceBackgroundJob
========================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @RedisHash @Accessors public class MicoServiceBackgroundJob implements Serializable

   Background job for a \ :java:ref:`MicoService`\ .

