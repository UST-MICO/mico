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

import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { Subscription } from 'rxjs';
import { versionComparator } from '../api/semantic-version';
import { CreateNextVersionComponent } from '../dialogs/create-next-version/create-next-version.component';
import { MatDialog, MatSnackBar } from '@angular/material';
import { UtilsService } from '../util/utils.service';

@Component({
    selector: 'mico-app-detail',
    templateUrl: './app-detail.component.html',
    styleUrls: ['./app-detail.component.css']
})
export class AppDetailComponent implements OnInit, OnDestroy {

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
        private router: Router,
        private dialog: MatDialog,
        private snackBar: MatSnackBar,
        private util: UtilsService,
    ) { }

    subRouteParams: Subscription;
    subApplicationVersions: Subscription;
    subDeploy: Subscription;
    subPublicIps: Subscription[] = [];
    subApplication: Subscription;
    subServiceDependency: Subscription;
    subCreateNextVersion: Subscription;


    // immutable application  object which is updated, when new data is pushed
    application: ApiObject;
    shortName: string;
    selectedVersion;
    allVersions;
    publicIps: string[] = [];

    // modifiable application object
    applicationData;
    edit: Boolean = false;

    /**
     * checks if the selected version is the latest application version
     */
    isLatestVersion = () => {
        if (this.allVersions != null) {
            if (this.selectedVersion === this.getLatestVersion()) {
                return true;
            }
        }
        return false;
    }

    ngOnInit() {

        this.subRouteParams = this.route.params.subscribe(params => {
            this.shortName = params['shortName'];
            const givenVersion = params['version'];

            // get all application versions
            this.subApplicationVersions = this.apiService.getApplicationVersions(this.shortName)
                .subscribe(versions => {

                    // sort by version
                    this.allVersions = JSON.parse(JSON.stringify(versions)).sort((n1, n2) => versionComparator(n1.version, n2.version));
                    const latestVersion = this.getLatestVersion();

                    // call the selected version, latest if no version is specified
                    if (givenVersion == null) {
                        this.subscribeApplication(latestVersion);
                    } else {
                        let found = false;
                        found = versions.some(element => {

                            if (element.version === givenVersion) {
                                this.subscribeApplication(element.version);
                                return true;
                            }
                        });
                        if (!found) {
                            // given version was not found in the versions list, take latest instead
                            this.subscribeApplication(latestVersion);
                        }
                    }
                });
        });

    }

    /**
     * subscribe to the given shortName/version and subscribe to its interfaces
     *
     * @param shortName shortName of the application to be displayed
     * @param version version of the application to be displayed
     */
    subscribeApplication(version: string) {

        this.selectedVersion = version;

        this.util.safeUnsubscribe(this.subApplication);
        // get the application
        this.subApplication = this.apiService.getApplication(this.shortName, version).subscribe(val => {
            this.application = val;

            // get the public ips
            this.application.services.forEach(service => {

                if (service.serviceInterfaces != null) {

                    // assumption: one public ip per interface
                    service.serviceInterfaces.forEach(micoInterface => {
                        this.subPublicIps.push(this.apiService
                            .getServiceInterfacePublicIp(service.shortName, service.version, micoInterface.serviceInterfaceName)
                            .subscribe(listOfPublicIps => {
                                const tempPublicIps = [];
                                listOfPublicIps.forEach(publicIp => {
                                    tempPublicIps.push(publicIp);
                                });
                                this.publicIps = tempPublicIps;
                            }));
                    });
                }
            });
        });
    }

    /**
     * unsubscribe from all subscriptions
     */
    ngOnDestroy() {
        this.util.safeUnsubscribe(this.subRouteParams);
        this.util.safeUnsubscribe(this.subApplicationVersions);
        this.util.safeUnsubscribe(this.subDeploy);
        this.subPublicIps.forEach(subscription => {
            this.util.safeUnsubscribe(subscription);
        });
        this.util.safeUnsubscribe(this.subApplication);
        this.util.safeUnsubscribe(this.subServiceDependency);
        this.util.safeUnsubscribe(this.subCreateNextVersion);
    }


    /**
     * calls the deploy endpoint
     * uses: POST application/{shortName}/{version}/deploy
     */
    deployApplication() {
        this.subDeploy = this.apiService.postApplicationDeployCommand(this.application.shortName, this.application.version)
            .subscribe(val => {
                // TODO wait for propper return value from deploy endpoint
                // add some deployment monitoring (e.g. state)
                console.log(val);
                this.snackBar.open('Application deployment initialized.', 'Ok', {
                    duration: 5000,
                });

            });
    }

    /**
     * returns the last elements version of the allVersions list (list is sorted in ngOnInit)
     */
    getLatestVersion() {
        if (this.allVersions != null && this.allVersions.length > 0) {
            return this.allVersions[this.allVersions.length - 1].version;
        }
    }


    /**
    * call-back from the version picker
    */
    updateVersion(version) {
        if (version != null) {
            this.selectedVersion = version;
            this.router.navigate(['app-detail', this.application.shortName, version]);
        } else {
            this.router.navigate(['app-detail', this.application.shortName]);
        }
    }

    /**
     * stores changes done via the mico-forms in the ui
     * toggles back to display mode
     */
    saveApplicationChanges() {
        this.apiService.putApplication(this.shortName, this.selectedVersion, this.applicationData)
            .subscribe();
        this.edit = false;
    }

    /**
     * opens a dialog to choose the version part to be increased.
     * Triggers the creation of the next version afterwards.
     */
    promoteNextVersion() {

        // open dialog
        const dialogRef = this.dialog.open(CreateNextVersionComponent, {
            data: {
                version: this.selectedVersion,
            }
        });

        this.util.safeUnsubscribe(this.subCreateNextVersion);


        // handle dialog result
        this.subCreateNextVersion = dialogRef.afterClosed().subscribe(nextVersion => {

            if (nextVersion) {
                this.apiService.promoteApplication(this.application.shortName, this.application.version, nextVersion).subscribe(val => {
                    this.updateVersion(null);
                });
            }
        });
    }

    /**
     * removes the current application version
     */
    deleteCurrentVersion() {
        this.apiService.deleteApplication(this.application.shortName, this.selectedVersion)
            .subscribe(val => {

                // stay on the application page if there exists another version
                if (this.allVersions.length > 0) {
                    this.updateVersion(null);
                } else {
                    this.router.navigate(['../app-list']);
                }
            });
    }
}
