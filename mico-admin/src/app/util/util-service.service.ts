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

import { Injectable } from '@angular/core';
import { ApiService } from '../api/api.service';
import { MatDialog, MatSnackBar } from '@angular/material';
import { Router } from '@angular/router';
import { safeUnsubscribe } from './utils';
import { CreateServiceDialogComponent } from '../dialogs/create-service/create-service.component';
import { CreateApplicationComponent } from '../dialogs/create-application/create-application.component';
import { take } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class UtilServiceService {

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
        private snackBar: MatSnackBar,
        private router: Router,
    ) { }

    /**
     * dialog to create a new service. can be done:
     * - manually
     * - via github import
     * uses: POST services or POST services/import/github
     */
    createNewService(): void {
        const dialogRef = this.dialog.open(CreateServiceDialogComponent);

        const subDialog = dialogRef.afterClosed().subscribe(result => {

            safeUnsubscribe(subDialog);

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
        });
    }

    /**
     * create a new application
     * uses: POST application
     */
    createNewApplication() {
        const dialogRef = this.dialog.open(CreateApplicationComponent);

        const subDialog = dialogRef.afterClosed().subscribe(result => {

            safeUnsubscribe(subDialog);

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

            this.apiService.postApplication(data)
                .pipe(take(1))
                .subscribe(val => {

                    result.services.forEach(service => {

                        const tempSubscription = this.apiService
                            .postApplicationServices(val.shortName, val.version, service.shortName, service.version)
                            .subscribe(element => {
                                safeUnsubscribe(tempSubscription);
                            });
                    });

                    this.router.navigate(['app-detail', val.shortName, val.version]);
                });
        });
    }

}
