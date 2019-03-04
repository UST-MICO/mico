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
import { ApiService } from 'src/app/api/api.service';
import { Subscription } from 'rxjs';
import { MatDialog } from '@angular/material';
import { ServicePickerComponent } from '../service-picker/service-picker.component';
import { ApiModel } from 'src/app/api/apimodel';
import { UtilsService } from 'src/app/util/utils.service';

@Component({
    selector: 'mico-create-application',
    templateUrl: './create-application.component.html',
    styleUrls: ['./create-application.component.css']
})
export class CreateApplicationComponent implements OnInit, OnDestroy {

    subDependeesDialog: Subscription;
    subModelDefinitions: Subscription;

    applicationData;
    services = [];

    filterList: string[];

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
        private util: UtilsService,
    ) {
    }

    ngOnInit() {
        // only show required fields
        this.subModelDefinitions = this.apiService.getModelDefinitions().subscribe(val => {
            this.filterList = (val['MicoApplicationDTO'] as ApiModel).required;
        });
    }

    ngOnDestroy() {
        // unsubscribe obervables
        this.util.safeUnsubscribe(this.subDependeesDialog);
        this.util.safeUnsubscribe(this.subModelDefinitions);
    }


    /**
     * return function of the dialog
     */
    save() {
        return { applicationProperties: this.applicationData, services: this.services };
    }

    /**
     * add service picker
     */
    pickServices() {
        // open dialog
        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: '',
                choice: 'multi',
                existingDependencies: this.services,
                serviceId: '',
            }
        });

        // handle result
        this.subDependeesDialog = dialogRef.afterClosed().subscribe(result => {
            if (result) {
                result.forEach(element => {
                    this.services.push(element);
                });
            }
        });
    }

    /**
     * delete a service dependencie
     * @param element service where the dependencie shall be deleted
     */
    deleteDependency(element) {
        const index = this.services.indexOf(element);
        if (index > -1) {
            this.services.splice(index, 1);
        }
    }
}
