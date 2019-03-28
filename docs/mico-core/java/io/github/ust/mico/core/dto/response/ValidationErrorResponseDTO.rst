.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

.. java:import:: javax.servlet.http HttpServletRequest

.. java:import:: lombok Value

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.validation FieldError

.. java:import:: org.springframework.web.context.request RequestContextHolder

.. java:import:: org.springframework.web.context.request ServletRequestAttributes

ValidationErrorResponseDTO
==========================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Value public class ValidationErrorResponseDTO

Fields
------
HTTP_STATUS
^^^^^^^^^^^

.. java:field:: public static final HttpStatus HTTP_STATUS
   :outertype: ValidationErrorResponseDTO

   HTTP status code that is used for validation errors.

   "422 Unprocessable Entity" is used instead of "400 Bad Request" because the request was syntactically correct, but semantically incorrect.

Constructors
------------
ValidationErrorResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public ValidationErrorResponseDTO(List<FieldError> fieldErrorList)
   :outertype: ValidationErrorResponseDTO

