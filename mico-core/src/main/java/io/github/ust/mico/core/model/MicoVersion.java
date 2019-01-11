package io.github.ust.mico.core.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;

import lombok.Getter;

/**
 * Wrapper for a {@link Version} that adds
 * the functionality for a version prefix, so
 * that versions like, e.g., 'v1.2.3' are possible.
 */
// TODO: @Jan -> NodeEntity / RelationshipEntity or whatever ...
@Getter
public class MicoVersion implements Comparable<MicoVersion> {
    
    /**
     *  String prefix of this version, e.g., 'v'
     */
    private String prefix;
    
    /**
     *  The actual semantic version
     */
    private Version version;
    
    
    /**
     * Private constructor.
     * 
     * @param prefix the prefix string.
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
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty
     * @throws ParseException when invalid version string is provided
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}
     */
    public static MicoVersion valueOf(String version) {
        Matcher matcher = Pattern.compile("([^\\d]+)(.*)").matcher(version);
        String prefix = matcher.group(1).trim();
        Version semanticVersion = Version.valueOf(matcher.group(2).trim());
        return new MicoVersion(prefix, semanticVersion);
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
     * @param major the major version number.
     * @param minor the minor version number.
     * @param patch the patch version number.
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
     * @return a new instance of the {@code MicoVersion} class.
     */
    public MicoVersion incrementMajorVersion() {
        return new MicoVersion(prefix, version.incrementMajorVersion());
    }
    
    /**
     * Increments the major version and appends the pre-release version.
     *
     * @param preRelease the pre-release version to append.
     * @return a new instance of the {@code MicoVersion} class.
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty.
     * @throws ParseException when invalid version string is provided.
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}.
     */
    public MicoVersion incrementMajorVersion(String preRelease) {
        return new MicoVersion(prefix, version.incrementMajorVersion(preRelease));
    }
    
    /**
     * Increments the minor version.
     *
     * @return a new instance of the {@code MicoVersion} class.
     */
    public MicoVersion incrementMinorVersion() {
        return new MicoVersion(prefix, version.incrementMinorVersion());
    }
    
    /**
     * Increments the minor version and appends the pre-release version.
     *
     * @param preRelease the pre-release version to append.
     * @return a new instance of the {@code MicoVersion} class.
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty.
     * @throws ParseException when invalid version string is provided.
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}.
     */
    public MicoVersion incrementMinorVersion(String preRelease) {
        return new MicoVersion(prefix, version.incrementMinorVersion(preRelease));
    }
    
    /**
     * Increments the path version.
     *
     * @return a new instance of the {@code MicoVersion} class.
     */
    public MicoVersion incrementPathVersion() {
        return new MicoVersion(prefix, version.incrementPatchVersion());
    }
    
    /**
     * Increments the patch version and appends the pre-release version.
     *
     * @param preRelease the pre-release version to append.
     * @return a new instance of the {@code MicoVersion} class.
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty.
     * @throws ParseException when invalid version string is provided.
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}.
     */
    public MicoVersion incrementPatchVersion(String preRelease) {
        return new MicoVersion(prefix, version.incrementPatchVersion(preRelease));
    }

    /**
     * Increments the pre-release version.
     *
     * @return a new instance of the {@code MicoVersion} class.
     */
    public MicoVersion incrementPreReleaseVersion() {
        return new MicoVersion(prefix, version.incrementPreReleaseVersion());
    }

    /**
     * Increments the build metadata.
     *
     * @return a new instance of the {@code MicoVersion} class.
     */
    public MicoVersion incrementBuildMetadata() {
        return new MicoVersion(prefix, version.incrementBuildMetadata());
    }

    /**
     * Sets the pre-release version.
     *
     * @param preRelease the pre-release version to set.
     * @return a new instance of the {@code MicoVersion} class.
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty.
     * @throws ParseException when invalid version string is provided.
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}.
     */
    public MicoVersion setPreReleaseVersion(String preRelease) {
        return new MicoVersion(prefix, version.setPreReleaseVersion(preRelease));
    }

    /**
     * Sets the build metadata.
     *
     * @param build the build metadata to set.
     * @return a new instance of the {@code MicoVersion} class.
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty.
     * @throws ParseException when invalid version string is provided.
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}.
     */
    public MicoVersion setBuildMetadata(String build) {
        return new MicoVersion(prefix, version.setBuildMetadata(build));
    }
    


    /**
     * Checks if this version is greater than the other version.
     *
     * @param other the other version to compare to.
     * @return {@code true} if this version is greater than the other version
     *         or {@code false} otherwise.
     */
    public boolean greaterThan(MicoVersion other) {
        return version.greaterThan(other.version);
    }

    /**
     * Checks if this version is greater than or equal to the other version.
     *
     * @param other the other version to compare to.
     * @return {@code true} if this version is greater than or equal
     *         to the other version or {@code false} otherwise.
     */
    public boolean greaterThanOrEqualTo(MicoVersion other) {
        return version.greaterThanOrEqualTo(other.version);
    }

    /**
     * Checks if this version is less than the other version.
     *
     * @param other the other version to compare to.
     * @return {@code true} if this version is less than the other version
     *         or {@code false} otherwise.
     */
    public boolean lessThan(MicoVersion other) {
        return version.lessThan(other.version);
    }

    /**
     * Checks if this version is less than or equal to the other version.
     *
     * @param other the other version to compare to.
     * @return {@code true} if this version is less than or equal
     *         to the other version or {@code false} otherwise.
     */
    public boolean lessThanOrEqualTo(MicoVersion other) {
        return version.lessThanOrEqualTo(other.version);
    }

    /**
     * Checks if this version equals the other version.
     *
     * @param other the other version to compare to.
     * @return {@code true} if this version equals the other version
     *         or {@code false} otherwise.
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
     *
     * This method does not take into account the versions' build
     * metadata. If you want to compare the versions' build metadata
     * use the {@code Version.compareWithBuildsTo} method or the
     * {@code Version.BUILD_AWARE_ORDER} comparator.
     *
     * @param other the other version to compare to.
     * @return a negative integer, zero or a positive integer if this version
     *         is less than, equal to or greater the the specified version.
     * @see #compareWithBuildsTo(MicoVersion other)
     */
    @Override
    public int compareTo(MicoVersion other) {
        return version.compareTo(other.version);
    }

    /**
     * Compare this version to the other version
     * taking into account the build metadata.
     *
     * The method makes use of the {@code Version.BUILD_AWARE_ORDER} comparator.
     *
     * @param other the other version to compare to
     * @return integer result of comparison compatible with
     *         that of the {@code Comparable.compareTo} method
     * @see #BUILD_AWARE_ORDER
     */
    public int compareWithBuildsTo(MicoVersion other) {
        return version.compareWithBuildsTo(other.version);
    }

}
