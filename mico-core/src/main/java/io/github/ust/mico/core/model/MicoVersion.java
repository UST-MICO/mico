/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core.model;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;
import io.github.ust.mico.core.exception.VersionNotSupportedException;
import lombok.Getter;

/**
 * Wrapper for a {@link Version} that adds
 * the functionality for a version prefix, so
 * that versions like, e.g., 'v1.2.3' are possible.
 */
public class MicoVersion implements Comparable<MicoVersion> {

    /**
     * String prefix of this version, e.g., 'v'.
     */
    @Getter
    private String prefix;

    /**
     * The actual semantic version.
     */
    private Version version;

    /**
     * Private constructor.
     *
     * @param prefix  the prefix string.
     * @param version the actual semantic version.
     */
    private MicoVersion(String prefix, Version version) {
        this.prefix = prefix;
        this.version = version;
    }

    /**
     * Creates a new instance of {@code MicoVersion} as a
     * result of parsing the specified version string. Prefixes
     * are possible as everything before the first digit in the
     * given version string is treated as a prefix to the actual
     * semantic version.
     *
     * @param version the version string to parse (may include a prefix).
     * @return a new instance of the {@code MicoVersion} class.
     * @throws VersionNotSupportedException if the version is not a semantic version
     *                                      with a string prefix.
     */
    public static MicoVersion valueOf(String version) throws VersionNotSupportedException {
        String[] arr = version.split("\\d+", 2);
        String prefix = arr[0].trim();
        Version semanticVersion;
        try {
            semanticVersion = Version.valueOf(version.substring(prefix.length()).trim());
            return new MicoVersion(prefix, semanticVersion);
        } catch (IllegalArgumentException | ParseException e) {
            throw new VersionNotSupportedException("Version " + version
                + " can not be processed. Only semantic version formats with a string prefix are allowed.");
        }
    }

    /**
     * Creates a new instance of {@code MicoVersion}
     * for the specified version numbers.
     *
     * @param major the major version number.
     * @param minor the minor version number.
     * @param patch the patch version number.
     * @return a new instance of the {@code MicoVersion} class.
     * @throws IllegalArgumentException if a negative integer is passed.
     */
    public static MicoVersion forIntegers(int major, int minor, int patch) {
        return new MicoVersion("", Version.forIntegers(major, minor, patch));
    }

    /**
     * Creates a new instance of {@code MicoVersion}
     * for the specified version numbers with the
     * specified prefix string.
     *
     * @param prefix the prefix string.
     * @param major  the major version number.
     * @param minor  the minor version number.
     * @param patch  the patch version number.
     * @return a new instance of the {@code MicoVersion} class.
     * @throws IllegalArgumentException if a negative integer is passed.
     */
    public static MicoVersion forIntegersWithPrefix(String prefix, int major, int minor, int patch) {
        return new MicoVersion(prefix == null ? "" : prefix, Version.forIntegers(major, minor, patch));
    }

    /**
     * Returns the major version number.
     *
     * @return the major version number
     */
    public int getMajorVersion() {
        return version.getMajorVersion();
    }

    /**
     * Returns the minor version number.
     *
     * @return the minor version number
     */
    public int getMinorVersion() {
        return version.getMinorVersion();
    }

    /**
     * Returns the patch version number.
     *
     * @return the patch version number
     */
    public int getPatchVersion() {
        return version.getPatchVersion();
    }

    /**
     * Returns the string representation of the pre-release version.
     *
     * @return the string representation of the pre-release version
     */
    public String getPreReleaseVersion() {
        return version.getPreReleaseVersion();
    }

    /**
     * Returns the string representation of the build metadata.
     *
     * @return the string representation of the build metadata
     */
    public String getBuildMetadata() {
        return version.getBuildMetadata();
    }

    /**
     * Increments the major version.
     *
     * @return the updated instance of the {@code MicoVersion} class.
     */
    public MicoVersion incrementMajorVersion() {
        version = version.incrementMajorVersion();
        return this;
    }

    /**
     * Increments the major version and appends the pre-release version.
     *
     * @param preRelease the pre-release version to append.
     * @return the updated instance of the {@code MicoVersion} class.
     * @throws IllegalArgumentException     if the input string is {@code NULL} or empty.
     * @throws ParseException               when invalid version string is provided.
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}.
     */
    public MicoVersion incrementMajorVersion(String preRelease) {
        version = version.incrementMajorVersion(preRelease);
        return this;
    }

    /**
     * Increments the minor version.
     *
     * @return the updated instance of the {@code MicoVersion} class.
     */
    public MicoVersion incrementMinorVersion() {
        version = version.incrementMinorVersion();
        return this;
    }

    /**
     * Increments the minor version and appends the pre-release version.
     *
     * @param preRelease the pre-release version to append.
     * @return the updated instance of the {@code MicoVersion} class.
     * @throws IllegalArgumentException     if the input string is {@code NULL} or empty.
     * @throws ParseException               when invalid version string is provided.
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}.
     */
    public MicoVersion incrementMinorVersion(String preRelease) {
        version = version.incrementMinorVersion(preRelease);
        return this;
    }

    /**
     * Increments the path version.
     *
     * @return the updated instance of the {@code MicoVersion} class.
     */
    public MicoVersion incrementPatchVersion() {
        version = version.incrementPatchVersion();
        return this;
    }

    /**
     * Increments the patch version and appends the pre-release version.
     *
     * @param preRelease the pre-release version to append.
     * @return the updated instance of the {@code MicoVersion} class.
     * @throws IllegalArgumentException     if the input string is {@code NULL} or empty.
     * @throws ParseException               when invalid version string is provided.
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}.
     */
    public MicoVersion incrementPatchVersion(String preRelease) {
        version = version.incrementPatchVersion(preRelease);
        return this;
    }

    /**
     * Increments the pre-release version.
     *
     * @return the updated instance of the {@code MicoVersion} class.
     */
    public MicoVersion incrementPreReleaseVersion() {
        version = version.incrementPreReleaseVersion();
        return this;
    }

    /**
     * Increments the build metadata.
     *
     * @return the updated instance of the {@code MicoVersion} class.
     */
    public MicoVersion incrementBuildMetadata() {
        version = version.incrementBuildMetadata();
        return this;
    }

    /**
     * Sets the pre-release version.
     *
     * @param preRelease the pre-release version to set.
     * @return the updated instance of the {@code MicoVersion} class.
     * @throws IllegalArgumentException     if the input string is {@code NULL} or empty.
     * @throws ParseException               when invalid version string is provided.
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}.
     */
    public MicoVersion setPreReleaseVersion(String preRelease) {
        version = version.setPreReleaseVersion(preRelease);
        return this;
    }

    /**
     * Sets the build metadata.
     *
     * @param build the build metadata to set.
     * @return the updated instance of the {@code MicoVersion} class.
     * @throws IllegalArgumentException     if the input string is {@code NULL} or empty.
     * @throws ParseException               when invalid version string is provided.
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}.
     */
    public MicoVersion setBuildMetadata(String build) {
        version = version.setBuildMetadata(build);
        return this;
    }

    /**
     * Checks if this version is greater than the other version.
     *
     * @param other the other version to compare to.
     * @return {@code true} if this version is greater than the other version
     * or {@code false} otherwise.
     */
    public boolean greaterThan(MicoVersion other) {
        return version.greaterThan(other.version);
    }

    /**
     * Checks if this version is greater than or equal to the other version.
     *
     * @param other the other version to compare to.
     * @return {@code true} if this version is greater than or equal
     * to the other version or {@code false} otherwise.
     */
    public boolean greaterThanOrEqualTo(MicoVersion other) {
        return version.greaterThanOrEqualTo(other.version);
    }

    /**
     * Checks if this version is less than the other version.
     *
     * @param other the other version to compare to.
     * @return {@code true} if this version is less than the other version
     * or {@code false} otherwise.
     */
    public boolean lessThan(MicoVersion other) {
        return version.lessThan(other.version);
    }

    /**
     * Checks if this version is less than or equal to the other version.
     *
     * @param other the other version to compare to.
     * @return {@code true} if this version is less than or equal
     * to the other version or {@code false} otherwise.
     */
    public boolean lessThanOrEqualTo(MicoVersion other) {
        return version.lessThanOrEqualTo(other.version);
    }

    /**
     * Checks if this version equals the other version.
     *
     * @param other the other version to compare to.
     * @return {@code true} if this version equals the other version
     * or {@code false} otherwise.
     * @see #compareTo(MicoVersion other)
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof MicoVersion)) {
            return false;
        }

        MicoVersion that = (MicoVersion) other;

        return prefix.equals(that.prefix) && compareTo(that) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + prefix.hashCode();
        hash = 97 * hash + version.hashCode();
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return prefix + version.toString();
    }

    /**
     * Compares this version to the other version.
     * <p>
     * This method does not take into account the versions' build
     * metadata. If you want to compare the versions' build metadata
     * use the {@code Version.compareWithBuildsTo} method or the
     * {@code Version.BUILD_AWARE_ORDER} comparator.
     *
     * @param other the other version to compare to.
     * @return a negative integer, zero or a positive integer if this version
     * is less than, equal to or greater the the specified version.
     * @see #compareWithBuildsTo(MicoVersion other)
     */
    @Override
    public int compareTo(MicoVersion other) {
        return version.compareTo(other.version);
    }

    /**
     * Compare this version to the other version
     * taking into account the build metadata.
     * <p>
     * The method makes use of the {@code Version.BUILD_AWARE_ORDER} comparator.
     *
     * @param other the other version to compare to
     * @return integer result of comparison compatible with
     * that of the {@code Comparable.compareTo} method
     * @see #BUILD_AWARE_ORDER
     */
    public int compareWithBuildsTo(MicoVersion other) {
        return version.compareWithBuildsTo(other.version);
    }

}
