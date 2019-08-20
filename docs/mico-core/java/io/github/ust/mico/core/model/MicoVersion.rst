.. java:import:: java.util.regex Pattern

.. java:import:: com.github.zafarkhaja.semver ParseException

.. java:import:: com.github.zafarkhaja.semver UnexpectedCharacterException

.. java:import:: com.github.zafarkhaja.semver Version

.. java:import:: io.github.ust.mico.core.exception VersionNotSupportedException

.. java:import:: io.github.ust.mico.core.util Patterns

.. java:import:: lombok Getter

MicoVersion
===========

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: public class MicoVersion implements Comparable<MicoVersion>

   Wrapper for a \ :java:ref:`Version`\  that adds the functionality for a version prefix, so that versions like, e.g., 'v1.2.3' are possible.

   Note that this class is only used for business logic purposes and instances are not persisted.

Methods
-------
compareTo
^^^^^^^^^

.. java:method:: @Override public int compareTo(MicoVersion other)
   :outertype: MicoVersion

   Compares this version to the other version.

   This method does not take into account the versions' build metadata. If you want to compare the versions' build metadata use the \ ``Version.compareWithBuildsTo``\  method or the \ ``Version.BUILD_AWARE_ORDER``\  comparator.

   :param other: the other version to compare to.
   :return: a negative integer, zero or a positive integer if this version is less than, equal to or greater the the specified version.

   **See also:** :java:ref:`.compareWithBuildsTo(MicoVersionother)`

compareWithBuildsTo
^^^^^^^^^^^^^^^^^^^

.. java:method:: public int compareWithBuildsTo(MicoVersion other)
   :outertype: MicoVersion

   Compare this version to the other version taking into account the build metadata.

   The method makes use of the \ ``Version.BUILD_AWARE_ORDER``\  comparator.

   :param other: the other version to compare to
   :return: integer result of comparison compatible with that of the \ ``Comparable.compareTo``\  method

   **See also:** :java:ref:`.BUILD_AWARE_ORDER`

equals
^^^^^^

.. java:method:: @Override public boolean equals(Object other)
   :outertype: MicoVersion

   Checks if this version equals the other version.

   :param other: the other version to compare to.
   :return: \ ``true``\  if this version equals the other version or \ ``false``\  otherwise.

   **See also:** :java:ref:`.compareTo(MicoVersionother)`

forIntegers
^^^^^^^^^^^

.. java:method:: public static MicoVersion forIntegers(int major, int minor, int patch)
   :outertype: MicoVersion

   Creates a new instance of \ ``MicoVersion``\  for the specified version numbers.

   :param major: the major version number.
   :param minor: the minor version number.
   :param patch: the patch version number.
   :throws IllegalArgumentException: if a negative integer is passed.
   :return: a new instance of the \ ``MicoVersion``\  class.

forIntegersWithPrefix
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public static MicoVersion forIntegersWithPrefix(String prefix, int major, int minor, int patch)
   :outertype: MicoVersion

   Creates a new instance of \ ``MicoVersion``\  for the specified version numbers with the specified prefix string.

   :param prefix: the prefix string.
   :param major: the major version number.
   :param minor: the minor version number.
   :param patch: the patch version number.
   :throws IllegalArgumentException: if a negative integer is passed.
   :return: a new instance of the \ ``MicoVersion``\  class.

getBuildMetadata
^^^^^^^^^^^^^^^^

.. java:method:: public String getBuildMetadata()
   :outertype: MicoVersion

   Returns the string representation of the build metadata.

   :return: the string representation of the build metadata

getMajorVersion
^^^^^^^^^^^^^^^

.. java:method:: public int getMajorVersion()
   :outertype: MicoVersion

   Returns the major version number.

   :return: the major version number

getMinorVersion
^^^^^^^^^^^^^^^

.. java:method:: public int getMinorVersion()
   :outertype: MicoVersion

   Returns the minor version number.

   :return: the minor version number

getPatchVersion
^^^^^^^^^^^^^^^

.. java:method:: public int getPatchVersion()
   :outertype: MicoVersion

   Returns the patch version number.

   :return: the patch version number

getPreReleaseVersion
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public String getPreReleaseVersion()
   :outertype: MicoVersion

   Returns the string representation of the pre-release version.

   :return: the string representation of the pre-release version

greaterThan
^^^^^^^^^^^

.. java:method:: public boolean greaterThan(MicoVersion other)
   :outertype: MicoVersion

   Checks if this version is greater than the other version.

   :param other: the other version to compare to.
   :return: \ ``true``\  if this version is greater than the other version or \ ``false``\  otherwise.

greaterThanOrEqualTo
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean greaterThanOrEqualTo(MicoVersion other)
   :outertype: MicoVersion

   Checks if this version is greater than or equal to the other version.

   :param other: the other version to compare to.
   :return: \ ``true``\  if this version is greater than or equal to the other version or \ ``false``\  otherwise.

hashCode
^^^^^^^^

.. java:method:: @Override public int hashCode()
   :outertype: MicoVersion

   {@inheritDoc}

incrementBuildMetadata
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoVersion incrementBuildMetadata()
   :outertype: MicoVersion

   Increments the build metadata.

   :return: the updated instance of the \ ``MicoVersion``\  class.

incrementMajorVersion
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoVersion incrementMajorVersion()
   :outertype: MicoVersion

   Increments the major version.

   :return: the updated instance of the \ ``MicoVersion``\  class.

incrementMajorVersion
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoVersion incrementMajorVersion(String preRelease)
   :outertype: MicoVersion

   Increments the major version and appends the pre-release version.

   :param preRelease: the pre-release version to append.
   :throws IllegalArgumentException: if the input string is \ ``NULL``\  or empty.
   :throws ParseException: when invalid version string is provided.
   :throws UnexpectedCharacterException: is a special case of \ ``ParseException``\ .
   :return: the updated instance of the \ ``MicoVersion``\  class.

incrementMinorVersion
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoVersion incrementMinorVersion()
   :outertype: MicoVersion

   Increments the minor version.

   :return: the updated instance of the \ ``MicoVersion``\  class.

incrementMinorVersion
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoVersion incrementMinorVersion(String preRelease)
   :outertype: MicoVersion

   Increments the minor version and appends the pre-release version.

   :param preRelease: the pre-release version to append.
   :throws IllegalArgumentException: if the input string is \ ``NULL``\  or empty.
   :throws ParseException: when invalid version string is provided.
   :throws UnexpectedCharacterException: is a special case of \ ``ParseException``\ .
   :return: the updated instance of the \ ``MicoVersion``\  class.

incrementPatchVersion
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoVersion incrementPatchVersion()
   :outertype: MicoVersion

   Increments the path version.

   :return: the updated instance of the \ ``MicoVersion``\  class.

incrementPatchVersion
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoVersion incrementPatchVersion(String preRelease)
   :outertype: MicoVersion

   Increments the patch version and appends the pre-release version.

   :param preRelease: the pre-release version to append.
   :throws IllegalArgumentException: if the input string is \ ``NULL``\  or empty.
   :throws ParseException: when invalid version string is provided.
   :throws UnexpectedCharacterException: is a special case of \ ``ParseException``\ .
   :return: the updated instance of the \ ``MicoVersion``\  class.

incrementPreReleaseVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoVersion incrementPreReleaseVersion()
   :outertype: MicoVersion

   Increments the pre-release version.

   :return: the updated instance of the \ ``MicoVersion``\  class.

lessThan
^^^^^^^^

.. java:method:: public boolean lessThan(MicoVersion other)
   :outertype: MicoVersion

   Checks if this version is less than the other version.

   :param other: the other version to compare to.
   :return: \ ``true``\  if this version is less than the other version or \ ``false``\  otherwise.

lessThanOrEqualTo
^^^^^^^^^^^^^^^^^

.. java:method:: public boolean lessThanOrEqualTo(MicoVersion other)
   :outertype: MicoVersion

   Checks if this version is less than or equal to the other version.

   :param other: the other version to compare to.
   :return: \ ``true``\  if this version is less than or equal to the other version or \ ``false``\  otherwise.

setBuildMetadata
^^^^^^^^^^^^^^^^

.. java:method:: public MicoVersion setBuildMetadata(String build)
   :outertype: MicoVersion

   Sets the build metadata.

   :param build: the build metadata to set.
   :throws IllegalArgumentException: if the input string is \ ``NULL``\  or empty.
   :throws ParseException: when invalid version string is provided.
   :throws UnexpectedCharacterException: is a special case of \ ``ParseException``\ .
   :return: the updated instance of the \ ``MicoVersion``\  class.

setPreReleaseVersion
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoVersion setPreReleaseVersion(String preRelease)
   :outertype: MicoVersion

   Sets the pre-release version.

   :param preRelease: the pre-release version to set.
   :throws IllegalArgumentException: if the input string is \ ``NULL``\  or empty.
   :throws ParseException: when invalid version string is provided.
   :throws UnexpectedCharacterException: is a special case of \ ``ParseException``\ .
   :return: the updated instance of the \ ``MicoVersion``\  class.

toString
^^^^^^^^

.. java:method:: @Override public String toString()
   :outertype: MicoVersion

   {@inheritDoc}

valueOf
^^^^^^^

.. java:method:: public static MicoVersion valueOf(String version) throws VersionNotSupportedException
   :outertype: MicoVersion

   Creates a new instance of \ ``MicoVersion``\  as a result of parsing the specified version string. Prefixes are possible as everything before the first digit in the given version string is treated as a prefix to the actual semantic version. Note that the prefix can only consist of letters.

   :param version: the version string to parse (may include a prefix).
   :throws VersionNotSupportedException: if the version is not a semantic version with a string prefix.
   :return: a new instance of the \ ``MicoVersion``\  class.

