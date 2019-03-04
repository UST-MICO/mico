/**
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

export const VERSION_REGEX = /^([a-zA-Z]+)?(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(-(((0|[1-9]\d*|\d*[A-Z-a-z-][\dA-Za-z-]*))(\.(0|[1-9]\d*|\d*[A-Za-z-][\dA-Za-z-]*))*))?(\+([\dA-Za-z-]+(\.[\dA-Za-z-]*)*))?$/;
// /(^\w+)?(\d+)\.(\d+)\.(\d+)(-(?:\w+\.)*\w+)?/;
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
    // compare major
    result = parseInt(mA[2], 10) - parseInt(mB[2], 10);
    if (result !== 0) {
        return (result > 0) ? 1 : -1;
    }
    // compare minor
    result = parseInt(mA[3], 10) - parseInt(mB[3], 10);
    if (result !== 0) {
        return (result > 0) ? 1 : -1;
    }
    // compare fix
    result = parseInt(mA[4], 10) - parseInt(mB[4], 10);
    if (result !== 0) {
        return (result > 0) ? 1 : -1;
    }
    // compare prefix if both versions have one
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

export enum versionComponents {
    major,
    minor,
    patch,
}

/**
 * takes a version string and creates the consecutive major/minor/patch version
 * @param version the 'old' version
 * @param incrementComponent the version level to be incremented.
 */
export function incrementVersion(version: String, incrementComponent: versionComponents) {
    const match = version.match(VERSION_REGEX);
    let versionString = '';

    let versionAppendix = '';
    if (match[5] != null) {
        versionAppendix = match[5];
    }

    // optional letters in front
    if (match[1] != null) {
        versionString += match[1];
    }

    // major
    if (match[2] != null) {

        if (incrementComponent === versionComponents.major) {
            // increment major version number
            versionString += (parseInt(match[2], 10) + 1).toString();
            versionString += '.0.0' + versionAppendix;

            return versionString;

        } else {
            versionString += match[2];
        }

    } else {
        versionString += '0';
    }

    versionString += '.';

    // minor
    if (match[3] != null) {

        if (incrementComponent === versionComponents.minor) {

            // increment minor version number
            versionString += (parseInt(match[3], 10) + 1).toString();
            versionString += '.0' + versionAppendix;

            return versionString;
        } else {
            versionString += match[3];
        }

    } else {
        versionString += '0';
    }

    versionString += '.';

    // patch
    if (match[4] != null) {
        // since no value was returned yet, the incrementComponent must be 'patch' or some kind of undefined.
        versionString += (parseInt(match[4], 10) + 1).toString();

        return versionString + versionAppendix;
    }

    throw new Error('given version is malformed');
}
