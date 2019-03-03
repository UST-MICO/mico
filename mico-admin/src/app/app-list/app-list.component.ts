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

import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { Subscription, from } from 'rxjs';
import { groupBy, mergeMap, toArray, map } from 'rxjs/operators';
import { MatDialog } from '@angular/material';
import { YesNoDialogComponent } from '../dialogs/yes-no-dialog/yes-no-dialog.component';

@Component({
    selector: 'mico-app-list',
    templateUrl: './app-list.component.html',
    styleUrls: ['./app-list.component.css']
})
export class AppListComponent implements OnInit {

    subApplication: Subscription;

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog
    ) { }

    applications: Readonly<ApiObject[]>;

    displayedColumns: string[] = ['id', 'name', 'shortName', 'description', 'controls'];

    ngOnInit() {
        this.getApplications();
    }

    /**
     * retreives all applications and groups them by their shortName
     * uses: GET applications
     */
    getApplications(): void {

        // group applications by shortName
        this.subApplication = this.apiService.getApplications()
            .subscribe(val => {
                from(val as unknown as ArrayLike<ApiObject>)
                    .pipe(
                        groupBy(application => application.shortName),
                        mergeMap(group => group.pipe(toArray())),
                        map(group => group[0]),
                        toArray()
                    ).subscribe(applicationList => {
                        this.applications = applicationList;
                    });

            });
    }

    /**
     * deletes all versions of an application, if the user confirms a dialog and the application is not deployed.
     * uses: DELETE applications/{shortName}
     * @param application shortName of the applications to be deleted
     */
    deleteApplication(application) {

        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: application,
                question: 'deleteAllServiceVersions'
            }
        });

        const subDeleteServiceVersions = dialogRef.afterClosed().subscribe(shouldDelete => {
            if (shouldDelete) {
                this.apiService.deleteAllApplicationVersions(application.shortName).subscribe();
                subDeleteServiceVersions.unsubscribe();
            }
        });
    }
}
