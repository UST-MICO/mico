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
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';
import { from } from 'rxjs';
import { groupBy, mergeMap, toArray, map } from 'rxjs/operators';
import { ApiObject } from '../api/apiobject';
import { MatDialog } from '@angular/material';
import { YesNoDialogComponent } from '../dialogs/yes-no-dialog/yes-no-dialog.component';
import { UtilsService } from '../util/utils.service';


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
        private util: UtilsService,
    ) {
        this.getServices();
    }

    services;

    displayedColumns: string[] = ['id', 'name', 'shortName', 'description', 'controls'];

    ngOnInit() {
    }

    ngOnDestroy() {
        // unsubscribe observables
        this.util.safeUnsubscribe(this.subServices);
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
                        map(group => group[0]),
                        toArray()
                    ).subscribe(serviceList => {
                        this.services = serviceList;
                    });
            });

    }

    /**
     * deletes all versions of a service, if the user confirms a dialog and the service is not deployed.
     * uses: DELETE services/{shortName}
     * @param service shortName of the services to be deleted
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
                this.util.safeUnsubscribe(subDeleteServiceVersions);
            }
        });
    }

}
