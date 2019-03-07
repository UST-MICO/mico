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

import { Component, OnInit, Input, OnDestroy } from '@angular/core';

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { MatDialog } from '@angular/material';
import { ServicePickerComponent } from '../dialogs/service-picker/service-picker.component';
import { Subscription } from 'rxjs';
import { YesNoDialogComponent } from '../dialogs/yes-no-dialog/yes-no-dialog.component';
import { UtilsService } from '../util/utils.service';

@Component({
    selector: 'mico-app-detail-overview',
    templateUrl: './app-detail-overview.component.html',
    styleUrls: ['./app-detail-overview.component.css']
})
export class AppDetailOverviewComponent implements OnInit, OnDestroy {

    subDependeesDialog: Subscription;
    subServiceDependency: Subscription;

    @Input() application: ApiObject;

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
        private util: UtilsService ) { }

    ngOnInit() {
    }

    ngOnDestroy() {
        this.util.safeUnsubscribe(this.subDependeesDialog);
        this.util.safeUnsubscribe(this.subServiceDependency);
    }



    addService() {

        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: '',
                choice: 'multi',
                existingDependencies: this.application.services,
                serviceId: '',
            }
        });
        this.subDependeesDialog = dialogRef.afterClosed().subscribe(result => {

            if (result === '') {
                return;
            }

            // TODO consider if null check is still neccesary as soon as endpoint to add dependencies exists
            if (this.application.services == null) {
                this.application.services = [];
            }

            result.forEach(service => {
                // this.application.services.push(element);
                // TODO Consider adding all at once.
                this.apiService.postApplicationServices(this.application.shortName, this.application.version, service)
                    .subscribe();
            });
        });

    }

    deleteService(serviceShortName: string) {

        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: serviceShortName,
                question: 'deleteDependency'
            }
        });

        this.subServiceDependency = dialogRef.afterClosed().subscribe(shouldDelete => {
            if (shouldDelete) {

                this.apiService.deleteApplicationServices(this.application.shortName, this.application.version, serviceShortName)
                    .subscribe();
            }
        });

    }

}
