.. java:import:: org.springframework.context.annotation Bean

.. java:import:: org.springframework.context.annotation Configuration

.. java:import:: org.springframework.util AntPathMatcher

.. java:import:: org.springframework.web.filter CommonsRequestLoggingFilter

.. java:import:: javax.servlet Filter

.. java:import:: javax.servlet.http HttpServletRequest

.. java:import:: java.util Collections

.. java:import:: java.util List

RequestLoggingFilterConfig.MicoRequestLoggingFilter
===================================================

.. java:package:: io.github.ust.mico.core.configuration
   :noindex:

.. java:type:: public static class MicoRequestLoggingFilter extends CommonsRequestLoggingFilter
   :outertype: RequestLoggingFilterConfig

Methods
-------
shouldNotFilter
^^^^^^^^^^^^^^^

.. java:method:: @Override protected boolean shouldNotFilter(HttpServletRequest request)
   :outertype: RequestLoggingFilterConfig.MicoRequestLoggingFilter

