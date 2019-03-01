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

import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';
import { ApiObject } from '../api/apiobject';
import { versionComparator } from '../api/semantic-version';

export interface Service extends ApiObject {
    name: string;
}

@Component({
    selector: 'mico-service-detail',
    templateUrl: './service-detail.component.html',
    styleUrls: ['./service-detail.component.css']
})
export class ServiceDetailComponent implements OnInit, OnDestroy {

    private subService: Subscription;
    private subParam: Subscription;

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
        private router: Router,
    ) { }

    service: Service;
    shortName: string;
    selectedVersion;
    versions: any = [];

    ngOnInit() {
        this.subParam = this.route.params.subscribe(params => {

            const shortName = params['shortName'];
            const version = params['version'];
            this.update(shortName, version);
        });
    }

    ngOnDestroy() {
        // unsubscribe if observable is not null
        if (this.subService != null) {
            this.subService.unsubscribe();
        }
        if (this.subParam != null) {
            this.subParam.unsubscribe();
        }
    }

    /**
     * loads a defined service and displays the service
     * Is to be called during initialization/when the url changes
     * uses: GET services/{shortName}
     *
     * @param shortName shortName of the service to be loaded
     * @param givenVersion version of the service to be loaded
     */
    update(shortName, givenVersion) {

        if (this.selectedVersion === givenVersion && givenVersion != null) {
            // no version change
            return;
        }

        if (this.subService != null) {
            this.subService.unsubscribe();
        }

        // get latest version
        this.subService = this.apiService.getServiceVersions(shortName)
            .subscribe(serviceVersions => {

                this.versions = serviceVersions;

                if (givenVersion == null) {
                    this.setLatestVersion(shortName, serviceVersions);
                } else {
                    let found = false;
                    found = serviceVersions.some(element => {

                        if (element.version === givenVersion) {
                            this.selectedVersion = givenVersion;
                            this.service = element as Service;
                            this.setVersion(shortName, givenVersion);
                            return true;
                        }
                    });

                    if (!found) {
                        // given version was not found in the versions list, take latest instead
                        this.setLatestVersion(shortName, serviceVersions);
                    }
                }

            });
    }

    /**
     * takes a list of services and sets this.service to the service with the latest version
     * this.version is set accoringly. Passes the shortName so it can be set at the sime time
     * as the version.
     */
    setLatestVersion(shortName, list) {

        list.forEach(element => {

            let version = '0.0.0';

            if (versionComparator(element.version, version) > 0) {
                version = element.version;
                this.service = element;

            } else {
                console.log(false);
            }
            this.setVersion(shortName, version);
        });
    }


    /**
     * call-back from the version picker
     */
    updateVersion(version) {
        this.selectedVersion = version;
        this.router.navigate(['service-detail', this.shortName, version]);
    }

    /**
     * setter for shortName and version to ensure they are set at the same time when they are retrieved via url parameters.
     * Had triggered some errors due to @Input() usage, when both was changed, but the fields where not set at the same time.
     *
     * @param shortName shortName to be set
     * @param version version to be set
     */
    setVersion(shortName, version) {
        this.selectedVersion = version;
        this.shortName = shortName;
        this.router.navigate(['service-detail', this.shortName, version]);
    }

}
