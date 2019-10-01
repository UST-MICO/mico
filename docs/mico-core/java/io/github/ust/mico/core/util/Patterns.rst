Patterns
========

.. java:package:: io.github.ust.mico.core.util
   :noindex:

.. java:type:: public class Patterns

   Contains regular expressions that are used for pattern matching.

Fields
------
KAFKA_TOPIC_NAME_MESSAGE
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String KAFKA_TOPIC_NAME_MESSAGE
   :outertype: Patterns

   Message is used if a match with the \ :java:ref:`Patterns.KAFKA_TOPIC_NAME_REGEX`\  fails.

KAFKA_TOPIC_NAME_REGEX
^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String KAFKA_TOPIC_NAME_REGEX
   :outertype: Patterns

   Kafka topic names must only contain letters, numbers, dots, underscores and minus symbols.

KUBERNETES_ENV_VAR_NAME_MESSAGE
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String KUBERNETES_ENV_VAR_NAME_MESSAGE
   :outertype: Patterns

   Message is used if a match with the \ :java:ref:`Patterns.KUBERNETES_ENV_VAR_NAME_REGEX`\  fails.

KUBERNETES_ENV_VAR_NAME_REGEX
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String KUBERNETES_ENV_VAR_NAME_REGEX
   :outertype: Patterns

   Kubernetes environment variable names must only contain letters, numbers and underscores, and must not start with a digit.

KUBERNETES_LABEL_KEY_MESSAGE
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String KUBERNETES_LABEL_KEY_MESSAGE
   :outertype: Patterns

   Message is used if a match with the \ :java:ref:`Patterns.KUBERNETES_LABEL_KEY_REGEX`\  fails.

KUBERNETES_LABEL_KEY_REGEX
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String KUBERNETES_LABEL_KEY_REGEX
   :outertype: Patterns

   Valid label keys have two segments: an optional prefix and name, separated by a slash (/). The name segment is required and must be 63 characters or less, beginning and ending with an alphanumeric character ([a-z0-9A-Z]) with dashes (-), underscores (_), dots (.), and alphanumerics between. The prefix is optional. If specified, the prefix must be a DNS subdomain: a series of DNS labels separated by dots (.), not longer than 253 characters in total, followed by a slash (/).

KUBERNETES_LABEL_VALUE_MESSAGE
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String KUBERNETES_LABEL_VALUE_MESSAGE
   :outertype: Patterns

   Message is used if a match with the \ :java:ref:`Patterns.KUBERNETES_LABEL_VALUE_REGEX`\  fails.

KUBERNETES_LABEL_VALUE_REGEX
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String KUBERNETES_LABEL_VALUE_REGEX
   :outertype: Patterns

   Kubernetes label values must be 63 characters or less and must be empty or begin and end with an alphanumeric character ([a-z0-9A-Z]) with dashes (-), underscores (_), dots (.), and alphanumerics between.

KUBERNETES_NAMING_MESSAGE
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String KUBERNETES_NAMING_MESSAGE
   :outertype: Patterns

   Message is used if a match with the \ :java:ref:`Patterns.KUBERNETES_NAMING_REGEX`\  fails.

KUBERNETES_NAMING_REGEX
^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String KUBERNETES_NAMING_REGEX
   :outertype: Patterns

   Kubernetes resource names must be a valid DNS-1123 subdomain.

NOT_EMPTY_REGEX
^^^^^^^^^^^^^^^

.. java:field:: public static final String NOT_EMPTY_REGEX
   :outertype: Patterns

   Regex for strings that MUST NOT be empty.

ONLY_LETTERS_OR_EMPTY_REGEX
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String ONLY_LETTERS_OR_EMPTY_REGEX
   :outertype: Patterns

   Regex to ensure to only use letters (may be empty).

OPEN_FAAS_FUNCTION_NAME_MESSAGE
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String OPEN_FAAS_FUNCTION_NAME_MESSAGE
   :outertype: Patterns

   Message is used if a match with the \ :java:ref:`Patterns.OPEN_FAAS_FUNCTION_NAME_REGEX`\  fails.

OPEN_FAAS_FUNCTION_NAME_REGEX
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String OPEN_FAAS_FUNCTION_NAME_REGEX
   :outertype: Patterns

   OpenFaaS function names must be a valid DNS-1123 subdomain.

RELATIVE_PATH_REGEX
^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String RELATIVE_PATH_REGEX
   :outertype: Patterns

   Regex for strings that MUST be a relative path.

SEMANTIC_VERSIONING_MESSAGE
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String SEMANTIC_VERSIONING_MESSAGE
   :outertype: Patterns

   Message is used if a match with the \ :java:ref:`Patterns.SEMANTIC_VERSION_WITH_PREFIX_REGEX`\  fails.

SEMANTIC_VERSION_REGEX
^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String SEMANTIC_VERSION_REGEX
   :outertype: Patterns

   Regex for a semantic version.

SEMANTIC_VERSION_WITH_PREFIX_REGEX
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String SEMANTIC_VERSION_WITH_PREFIX_REGEX
   :outertype: Patterns

   Regex for a semantic version with a prefix (optional) consisting of letters.

