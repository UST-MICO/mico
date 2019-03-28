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
import { safeUnsubscribe, safeUnsubscribeList } from '../util/utils';
import { YesNoDialogComponent } from '../dialogs/yes-no-dialog/yes-no-dialog.component';
import { take } from 'rxjs/operators';

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
    ) { }

    subRouteParams: Subscription;
    subApplicationVersions: Subscription;
    subDeploy: Subscription;
    subPublicIps: Subscription[] = [];
    subApplication: Subscription;
    subServiceDependency: Subscription;
    subCreateNextVersion: Subscription;
    subJobStatus: Subscription;
    subApplicationStatus: Subscription;
    subApplicationStatusPolling: Subscription;


    // immutable application  object which is updated, when new data is pushed
    application: ApiObject;
    shortName: string;
    selectedVersion;
    allVersions: any[];
    deploymentStatus;
    deploymentStatusMessage: string;

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
            this.selectedVersion = params['version'];

            // get all application versions
            safeUnsubscribe(this.subApplicationVersions);
            this.subApplicationVersions = this.apiService.getApplicationVersions(this.shortName)
                .subscribe(versions => {

                    // sort by version
                    this.allVersions = JSON.parse(JSON.stringify(versions)).sort((n1, n2) => versionComparator(n1.version, n2.version));

                    if (this.allVersions.length === 0) {
                        // back to application list, if there is no version of the application left
                        this.router.navigate(['../app-detail/app-list']);
                    }

                    const latestVersion = this.getLatestVersion();

                    // adapt url path
                    if (this.selectedVersion == null || !this.allVersions.some(v => v.version === this.selectedVersion)) {
                        // check if no version is selected or an unknown version is selected -> take latest version instead
                        this.router.navigate(['app-detail', this.shortName, latestVersion]);
                        // prevent further api calls (navigate will cause a reload anyway)
                        return;
                    }

                    this.subscribeApplication();
                });
        });

    }

    /**
     * subscribe to the given shortName/version and subscribe to its interfaces
     */
    subscribeApplication() {

        safeUnsubscribe(this.subApplication);
        // get the application
        this.subApplication = this.apiService.getApplication(this.shortName, this.selectedVersion)
            .subscribe(val => {
                this.application = val;
            });

        // status polling
        safeUnsubscribe(this.subApplicationStatus);
        this.subApplicationStatus = this.apiService.getApplicationDeploymentStatus(this.shortName, this.selectedVersion)
            .subscribe(val => {

                this.deploymentStatus = val;
                let message = '';
                val.messages.forEach(element => {
                    message += element.content + '\n';
                });

                this.deploymentStatusMessage = message;

            });

        safeUnsubscribe(this.subApplicationStatusPolling);
        this.subApplicationStatusPolling = this.apiService.startApplicationStatusPolling(this.shortName, this.selectedVersion);

    }

    /**
     * unsubscribe from all subscriptions
     */
    ngOnDestroy() {
        safeUnsubscribe(this.subRouteParams);
        safeUnsubscribe(this.subApplicationVersions);
        safeUnsubscribe(this.subDeploy);
        safeUnsubscribeList(this.subPublicIps);
        safeUnsubscribe(this.subApplication);
        safeUnsubscribe(this.subServiceDependency);
        safeUnsubscribe(this.subCreateNextVersion);
        safeUnsubscribe(this.subJobStatus);
        safeUnsubscribe(this.subApplicationStatus);
        safeUnsubscribe(this.subApplicationStatusPolling);
    }


    /**
     * calls the deploy endpoint
     * uses: POST application/{shortName}/{version}/deploy
     */
    deployApplication() {
        this.subDeploy = this.apiService.postApplicationDeployCommand(this.application.shortName, this.application.version)
            .subscribe(val => {
                this.snackBar.open('Application deployment initialized.', 'Ok', {
                    duration: 5000,
                });


                safeUnsubscribe(this.subJobStatus);
                this.subJobStatus = this.apiService.pollDeploymentJobStatus(this.shortName, this.selectedVersion);

            });
    }

    /**
     * calls the undeploy endpoint
     * uses: POST application/{shortName}/{version}/undeploy
     */
    undeployApplication() {
        this.subDeploy = this.apiService.postApplicationUndeployCommand(this.application.shortName, this.application.version)
            .subscribe(val => {

                this.snackBar.open('Application undeployment initialized.', 'Ok', {
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

        safeUnsubscribe(this.subCreateNextVersion);

        // handle dialog result
        this.subCreateNextVersion = dialogRef.afterClosed().subscribe(nextVersion => {

            if (nextVersion) {
                this.apiService.promoteApplication(this.application.shortName, this.application.version, nextVersion)
                    .pipe(take(1))
                    .subscribe(val => {
                        const subVersions = this.apiService.getApplicationVersions(this.shortName)
                            .subscribe(element => {
                                // wait until the latest version is updated
                                if (element.some(v => v.version === val.version)) {
                                    safeUnsubscribe(subVersions);
                                    this.updateVersion(val.version);
                                }
                            });
                    });
            }
        });
    }

    /**
     * removes the current application version
     */
    deleteCurrentVersion() {

        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: { name: this.application.name, shortName: this.shortName, version: this.selectedVersion },
                question: 'deleteApplication'
            }
        });

        const subDeleteDependency = dialogRef.afterClosed().subscribe(result => {
            if (result) {

                // go to latest version
                this.apiService.deleteApplication(this.application.shortName, this.selectedVersion)
                    .subscribe(val => {

                        const subVersions = this.apiService.getApplicationVersions(this.shortName)
                            .subscribe(element => {
                                // wait until the versions are updated
                                if (!element.some(v => v.version === this.selectedVersion)) {
                                    safeUnsubscribe(subVersions);
                                    this.updateVersion(null);
                                }
                            });
                    });

                safeUnsubscribe(subDeleteDependency);
            }
        });
    }
}
