.. java:import:: io.github.ust.mico.core.dto ValidationErrorDTO

.. java:import:: org.springframework.core Ordered

.. java:import:: org.springframework.core.annotation Order

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.validation BindingResult

.. java:import:: org.springframework.validation FieldError

.. java:import:: org.springframework.web.bind MethodArgumentNotValidException

.. java:import:: java.util List

GlobalExceptionHandler
======================

.. java:package:: io.github.ust.mico.core.exception
   :noindex:

.. java:type:: @Order @ControllerAdvice public class GlobalExceptionHandler

   Global exception handler

   Inspired by Petri Kainulainen https://www.petrikainulainen.net/programming/spring-framework/spring-from-the-trenches-adding-validation-to-a-rest-api/

Methods
-------
methodArgumentNotValidException
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @ResponseStatus @ResponseBody @ExceptionHandler public ValidationErrorDTO methodArgumentNotValidException(MethodArgumentNotValidException ex)
   :outertype: GlobalExceptionHandler

