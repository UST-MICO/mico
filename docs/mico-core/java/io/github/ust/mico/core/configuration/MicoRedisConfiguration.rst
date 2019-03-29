.. java:import:: lombok Getter

.. java:import:: lombok Setter

.. java:import:: org.springframework.boot.context.properties ConfigurationProperties

.. java:import:: org.springframework.context.annotation Bean

.. java:import:: org.springframework.context.annotation Configuration

.. java:import:: org.springframework.data.redis.connection.lettuce LettuceConnectionFactory

.. java:import:: org.springframework.data.redis.core RedisTemplate

.. java:import:: org.springframework.stereotype Component

.. java:import:: javax.validation.constraints NotBlank

MicoRedisConfiguration
======================

.. java:package:: io.github.ust.mico.core.configuration
   :noindex:

.. java:type:: @Component @Configuration @Getter @Setter @ConfigurationProperties public class MicoRedisConfiguration

   Configuration that includes information about Redis Database

Methods
-------
redisConnectionFactory
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Bean public LettuceConnectionFactory redisConnectionFactory()
   :outertype: MicoRedisConfiguration

redisTemplate
^^^^^^^^^^^^^

.. java:method:: @Bean public RedisTemplate<?, ?> redisTemplate()
   :outertype: MicoRedisConfiguration

