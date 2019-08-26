.. java:import:: io.github.ust.mico.core.service.imagebuilder ImageBuilder

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.boot.actuate.health Health

.. java:import:: org.springframework.boot.actuate.health HealthIndicator

.. java:import:: org.springframework.stereotype Component

ImageBuilderHealthIndicator
===========================

.. java:package:: io.github.ust.mico.core.service
   :noindex:

.. java:type:: @Component public class ImageBuilderHealthIndicator implements HealthIndicator

Constructors
------------
ImageBuilderHealthIndicator
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public ImageBuilderHealthIndicator(ImageBuilder imageBuilder)
   :outertype: ImageBuilderHealthIndicator

Methods
-------
health
^^^^^^

.. java:method:: @Override public Health health()
   :outertype: ImageBuilderHealthIndicator

