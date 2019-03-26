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
import { MatDialog } from '@angular/material';
import { YesNoDialogComponent } from '../dialogs/yes-no-dialog/yes-no-dialog.component';
import { safeUnsubscribe } from '../util/utils';
import { CreateNextVersionComponent } from '../dialogs/create-next-version/create-next-version.component';
import { take } from 'rxjs/operators';

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
    private subCreateNextVersion: Subscription;

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
        private router: Router,
        private dialog: MatDialog,
    ) { }

    service: Service;
    shortName: string;
    selectedVersion;
    allVersions: any = [];

    ngOnInit() {
        this.subParam = this.route.params.subscribe(params => {

            this.shortName = params['shortName'];
            this.selectedVersion = params['version'];
            this.update();
        });
    }

    ngOnDestroy() {
        // unsubscribe if observable is not null
        safeUnsubscribe(this.subService);
        safeUnsubscribe(this.subParam);
        safeUnsubscribe(this.subCreateNextVersion);
    }


    /**
     * loads a defined service and displays the service
     * Is to be called during initialization/when the url changes
     * uses: GET services/{shortName}
     */
    update() {

        // get latest version
        safeUnsubscribe(this.subService);
        this.subService = this.apiService.getServiceVersions(this.shortName)
            .subscribe(serviceVersions => {

                // sort by version
                this.allVersions = JSON.parse(JSON.stringify(serviceVersions)).sort((n1, n2) => versionComparator(n1.version, n2.version));

                if (this.allVersions.length === 0) {
                    // back to service list, if there is no version of this service left
                    this.router.navigate(['../service-detail/service-list']);
                }

                const latestVersion = this.getLatestVersion();

                // adapt url path
                if (this.selectedVersion == null || !this.allVersions.some(v => v.version === this.selectedVersion)) {
                    this.router.navigate(['service-detail', this.shortName, latestVersion]);
                    // prevent further api calls (navigate will cause a reload anyway)
                    return;
                } else {
                    let found = false;
                    found = this.allVersions.some(element => {

                        if (element.version === this.selectedVersion) {
                            this.service = element as Service;
                            return true;
                        }
                    });

                    if (!found) {
                        // given version was not found in the versions list, take latest instead
                        this.router.navigate(['service-detail', this.shortName, latestVersion]);
                    }
                }
            });
    }

    /**
     * returns the latest version
     */
    getLatestVersion() {
        if (this.allVersions != null && this.allVersions.length > 0) {
            return this.allVersions[this.allVersions.length - 1].version;
        }
    }

    /**
     * call-back from the version picker to open the selected version
     */
    updateVersion(version) {
        if (version != null) {
            this.selectedVersion = version;
            this.router.navigate(['service-detail', this.shortName, version]);
        } else {
            this.router.navigate(['service-detail', this.shortName]);
        }
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

    /**
     * action triggered in the ui to delete the current service version
     */
    deleteService() {

        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: { name: this.service.name, shortName: this.shortName, version: this.selectedVersion },
                question: 'deleteService'
            }
        });

        const subDeleteDependency = dialogRef.afterClosed().subscribe(result => {
            if (result) {

                this.apiService.deleteService(this.shortName, this.selectedVersion)
                    .subscribe(val => {
                        this.updateVersion(this.shortName);
                    });
                safeUnsubscribe(subDeleteDependency);
            }
        });
    }

    /**
     * opens a dialog to choose the version part to be increased.
     * Triggers the creation of the next service version afterwards.
     */
    promoteNextVersion() {

        // open dialog
        const dialogRef = this.dialog.open(CreateNextVersionComponent, {
            data: {
                version: this.selectedVersion,
            }
        });

        safeUnsubscribe(this.subCreateNextVersion);


        // handle dialog result
        this.subCreateNextVersion = dialogRef.afterClosed().subscribe(nextVersion => {

            if (nextVersion) {
                this.apiService.promoteService(this.shortName, this.selectedVersion, nextVersion)
                    .pipe(take(1))
                    .subscribe(val => {

                        this.updateVersion(val.version);
                    });
            }
        });
    }

}
