.. java:import:: lombok Value

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.validation FieldError

.. java:import:: org.springframework.web.context.request RequestContextHolder

.. java:import:: org.springframework.web.context.request ServletRequestAttributes

.. java:import:: javax.servlet.http HttpServletRequest

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

ValidationErrorDTO
==================

.. java:package:: io.github.ust.mico.core.dto
   :noindex:

.. java:type:: @Value public class ValidationErrorDTO

Fields
------
HTTP_STATUS
^^^^^^^^^^^

.. java:field:: public static final HttpStatus HTTP_STATUS
   :outertype: ValidationErrorDTO

   HTTP status code that is used for validation errors.

   "422 Unprocessable Entity" is used instead of "400 Bad Request" because the request was syntactically correct, but semantically incorrect.

Constructors
------------
ValidationErrorDTO
^^^^^^^^^^^^^^^^^^

.. java:constructor:: public ValidationErrorDTO(List<FieldError> fieldErrorList)
   :outertype: ValidationErrorDTO

