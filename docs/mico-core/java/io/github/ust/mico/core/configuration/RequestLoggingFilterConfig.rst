.. java:import:: org.springframework.context.annotation Bean

.. java:import:: org.springframework.context.annotation Configuration

.. java:import:: org.springframework.util AntPathMatcher

.. java:import:: org.springframework.web.filter CommonsRequestLoggingFilter

.. java:import:: javax.servlet Filter

.. java:import:: javax.servlet.http HttpServletRequest

.. java:import:: java.util Collections

.. java:import:: java.util List

RequestLoggingFilterConfig
==========================

.. java:package:: io.github.ust.mico.core.configuration
   :noindex:

.. java:type:: @Configuration public class RequestLoggingFilterConfig

Methods
-------
logFilter
^^^^^^^^^

.. java:method:: @Bean public Filter logFilter()
   :outertype: RequestLoggingFilterConfig

