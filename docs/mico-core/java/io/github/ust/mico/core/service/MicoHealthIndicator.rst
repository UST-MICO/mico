.. java:import:: io.github.ust.mico.core.service.imagebuilder ImageBuilder

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.boot.actuate.health Health

.. java:import:: org.springframework.boot.actuate.health HealthIndicator

.. java:import:: org.springframework.stereotype Component

MicoHealthIndicator
===================

.. java:package:: io.github.ust.mico.core.service
   :noindex:

.. java:type:: @Component public class MicoHealthIndicator implements HealthIndicator

Constructors
------------
MicoHealthIndicator
^^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public MicoHealthIndicator(ImageBuilder imageBuilder)
   :outertype: MicoHealthIndicator

Methods
-------
health
^^^^^^

.. java:method:: @Override public Health health()
   :outertype: MicoHealthIndicator

