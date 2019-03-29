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
import { Subscription } from 'rxjs';
import { from } from 'rxjs';
import { groupBy, mergeMap, toArray, map } from 'rxjs/operators';
import { ApiObject } from '../api/apiobject';
import { MatDialog } from '@angular/material';
import { YesNoDialogComponent } from '../dialogs/yes-no-dialog/yes-no-dialog.component';
import { safeUnsubscribe } from '../util/utils';
import { UtilServiceService } from '../util/util-service.service';
import { versionComparator } from '../api/semantic-version';


@Component({
    selector: 'mico-service-list',
    templateUrl: './service-list.component.html',
    styleUrls: ['./service-list.component.css']
})
export class ServiceListComponent implements OnInit, OnDestroy {

    private subServices: Subscription;

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
        private utilService: UtilServiceService,
    ) {
        this.getServices();
    }

    services;

    displayedColumns: string[] = ['name', 'shortName', 'version', 'description', 'controls'];

    ngOnInit() {
    }

    ngOnDestroy() {
        // unsubscribe observables
        safeUnsubscribe(this.subServices);
    }

    /**
     * retrieves all services and versions
     * uses: GET services
     */
    getServices(): void {

        // group services by shortName
        this.subServices = this.apiService.getServices()
            .subscribe(val => {

                from(val as unknown as ArrayLike<ApiObject>)
                    .pipe(
                        groupBy(service => service.shortName),
                        mergeMap(group => group.pipe(toArray())),
                        map(group => group.sort((a, b) => versionComparator(a.version, b.version))),
                        map(group => group[group.length - 1]),
                        toArray()
                    ).subscribe(serviceList => {
                        this.services = serviceList;
                    });
            });

    }

    /**
     * deletes all versions of a service, if the user confirms a dialog and the service is not deployed.
     * uses: DELETE services/{shortName}
     * @param service shortName object to be deleted
     */
    deleteService(service) {
        // open dialog
        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: service,
                question: 'deleteAllServiceVersions'
            }
        });

        // handle dialog result
        const subDeleteServiceVersions = dialogRef.afterClosed().subscribe(shouldDelete => {
            if (shouldDelete) {
                this.apiService.deleteAllServiceVersions(service.shortName).subscribe();
                safeUnsubscribe(subDeleteServiceVersions);
            }
        });
    }


    /**
     * opens a dialog to create a new service.
     */
    newService(): void {
        this.utilService.createNewService();
    }
}
