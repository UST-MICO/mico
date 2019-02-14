export const VERSION_REGEX = /(^\w+)?(\d+)\.(\d+)\.(\d+)(-(?:\w+\.)*\w+)?/;

/**
 * Comparator for semantic version strings. Can be used in the array.sort method.
 *
 * @param versionA semantic version string e.g. '3.1.2-alpha'
 * @param versionB semantic version string e.g. '3.1.2-alpha'
 * @return number 1, 0 or -1 if a>b, a=b, a<b
 */
export function versionComparator(versionA: string, versionB: string): number {
    const mA = versionA.match(VERSION_REGEX);
    const mB = versionB.match(VERSION_REGEX);
    if (mA == null && mB == null) {
        return 0;
    }
    if (mA == null) {
        return 1;
    }
    if (mB == null) {
        return -1;
    }

    let result = 0;
    result = parseInt(mA[2], 10) - parseInt(mB[2], 10);
    if (result !== 0) {
        return (result > 0) ? 1 : -1;
    }
    result = parseInt(mA[3], 10) - parseInt(mB[3], 10);
    if (result !== 0) {
        return (result > 0) ? 1 : -1;
    }
    result = parseInt(mA[4], 10) - parseInt(mB[4], 10);
    if (result !== 0) {
        return (result > 0) ? 1 : -1;
    }
    if (mA[5] != null && mB[5] != null) {
        if (mA[5] > mB[5]) {
            return 1;
        }
        if (mA[5] < mB[5]) {
            return -1;
        }
    }
    return 0;
}

export function incrementVersion(version: String) {
    const match = version.match(VERSION_REGEX);
    let versionString = '';

    // optional letters in front
    if (match[1] != null) {
        versionString += match[1];
    }

    // major
    if (match[2] != null) {
        versionString += match[2];
    } else {
        versionString += '0';
    }

    versionString += '.';

    // minor
    if (match[3] != null) {
        // increment version number
        versionString += parseInt(match[3], 10) + 1;
    } else {
        versionString += '0';
    }

    versionString += '.';

    // patch
    if (match[4] != null) {
        versionString += match[4];
    } else {
        versionString += '0';
    }

    // optional letters in the end
    if (match[5] != null) {
        versionString += match[5];
    }

    return versionString;
}
