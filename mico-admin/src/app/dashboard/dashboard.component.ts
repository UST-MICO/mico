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
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { MatDialog, MatSnackBar } from '@angular/material';
import { CreateServiceDialogComponent } from '../dialogs/create-service/create-service.component';
import { Router } from '@angular/router';
import { CreateApplicationComponent } from '../dialogs/create-application/create-application.component';
import { groupBy, mergeMap, toArray, map } from 'rxjs/operators';
import { Subscription, from } from 'rxjs';
import { safeUnsubscribe } from '../util/utils';

@Component({
    selector: 'mico-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
        private router: Router,
        private snackBar: MatSnackBar,
    ) { }

    subApplications: Subscription;
    applications: Readonly<ApiObject[]>;
    displayedColumns: string[] = ['name', 'shortName'];

    ngOnInit() {
        this.getApplications();
    }

    ngOnDestroy() {
        // unsubscribe from observables
        safeUnsubscribe(this.subApplications);
    }


    /**
     * receives a list of applications from the apiService and group them by the shortName
     */
    getApplications(): void {


        // group applications by shortName
        this.subApplications = this.apiService.getApplications()
            .subscribe(val => {
                from(val as unknown as ArrayLike<ApiObject>)
                    .pipe(
                        // group
                        groupBy(application => application.shortName),
                        // cast groups to arrays
                        mergeMap(group => group.pipe(toArray())),
                        // take first element from each group
                        map(group => group[0]),
                        // create an array from those first elements
                        toArray()
                    ).subscribe(applicationList => {
                        this.applications = applicationList;
                    });

            });
    }


    /**
     * dialog to create a new service. can be done:
     * - manually
     * - via github import
     * uses: POST services or POST services/import/github
     */
    newService(): void {
        const dialogRef = this.dialog.open(CreateServiceDialogComponent);

        const subDialog = dialogRef.afterClosed().subscribe(result => {

            // filter empty results (when dialog is aborted)
            if (!result) {
                return;
            }

            // check if returned object is complete
            for (const property in result.data) {
                if (result.data[property] == null) {

                    if (property !== 'serviceInterfaces') {
                        this.snackBar.open('Missing property: ' + property, 'Ok', {
                            duration: 8000,
                        });
                        return;
                    }
                }
            }

            // decide if the service was created manually or is to be created via github crawler and create service
            if (result.tab === 'manual') {
                this.apiService.postService(result.data).subscribe(val => {
                    this.router.navigate(['service-detail', val.shortName, val.version]);
                });
            } else if (result.tab === 'github') {

                this.apiService.postServiceViaGithub(result.data.url, result.data.version).subscribe(val => {
                    this.router.navigate(['service-detail', val.shortName, val.version]);
                });
            }

            safeUnsubscribe(subDialog);
        });
    }

    /**
     * create a new application
     * uses: POST application
     */
    newApplication() {
        const dialogRef = this.dialog.open(CreateApplicationComponent);

        const subDialog = dialogRef.afterClosed().subscribe(result => {

            // filter empty results (when dialog is aborted)
            if (!result) {
                return;
            }

            // check if returned object is complete
            for (const property in result.applicationProperties) {
                if (result.applicationProperties[property] == null) {

                    // show an error message containg the missing field
                    this.snackBar.open('Missing property: ' + property, 'Ok', {
                        duration: 8000,
                    });
                    return;
                }
            }

            const data = result.applicationProperties;
            data.services = result.services;

            this.apiService.postApplication(data).subscribe(val => {
                this.router.navigate(['app-detail', val.shortName, val.version]);
            });

            safeUnsubscribe(subDialog);
        });
    }

    // links to our related pages
    openUserGuide() {
        window.open('https://mico-docs.readthedocs.io/en/latest/');
    }
    openDevGuide() {
        window.open('https://mico-dev.readthedocs.io/en/latest/');
    }
    suggestFeature() {
        window.open('https://github.com/UST-MICO/mico/issues/new');
    }

}
