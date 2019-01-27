export const VERSION_REGEX = /(^\w+)?(\d+)\.(\d+).(\d+)(-(?:\w+.)*\w+)?/;

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
