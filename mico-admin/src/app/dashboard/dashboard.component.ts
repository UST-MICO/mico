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

import { Component, OnInit, Input } from '@angular/core';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { MatDialog, MatSnackBar } from '@angular/material';
import { CreateServiceDialogComponent } from '../dialogs/create-service/create-service.component';
import { Router } from '@angular/router';
import { CreateApplicationComponent } from '../dialogs/create-application/create-application.component';

@Component({
    selector: 'mico-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
        private router: Router,
        private snackBar: MatSnackBar,
    ) {
        this.getApplications();
    }

    applications: Readonly<ApiObject[]>;

    displayedColumns: string[] = ['id', 'name', 'shortName'];

    ngOnInit() {

    }

    // TODO
    someAction() {
        console.log('TODO something');
    }


    /**
     * receives a list of applications from the apiService
     */
    getApplications(): void {
        this.apiService.getApplications()
            .subscribe(applications => this.applications = applications);
    }


    newService(): void {
        const dialogRef = this.dialog.open(CreateServiceDialogComponent);
        dialogRef.afterClosed().subscribe(result => {

            // filter empty results (when dialog is aborted)
            if (result !== '' && result != null && result.data != null) {

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

                // decide if the service was created manually or is to be created via github crawler
                if (result.tab === 'manual') {
                    this.apiService.postService(result.data).subscribe(val => {
                        this.router.navigate(['service-detail', val.shortName, val.version]);
                    });
                } else if (result.tab === 'github') {
                    // TODO replace result.data.vcsroot when the inpuf form field is changed
                    this.apiService.postServiceViaGithub(result.data.vcsroot).subscribe(val => {
                        this.router.navigate(['service-detail', val.shortName, val.version]);
                    });
                }

            }
        });
    }

    newApplication() {
        const dialogRef = this.dialog.open(CreateApplicationComponent);
        dialogRef.afterClosed().subscribe(result => {

            // filter empty results (when dialog is aborted)
            if (result !== '') {

                // check if returned object is complete
                for (const property in result.applicationProperties) {
                    if (result.applicationProperties[property] == null) {

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
            }
        });
    }



}
