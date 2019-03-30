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

import { Component, Input, OnDestroy, OnChanges } from '@angular/core';
import { Subscription } from 'rxjs';
import { ApiService } from '../api/api.service';
import { ServicePickerComponent } from '../dialogs/service-picker/service-picker.component';
import { MatDialog } from '@angular/material';
import { YesNoDialogComponent } from '../dialogs/yes-no-dialog/yes-no-dialog.component';
import { CreateServiceInterfaceComponent } from '../dialogs/create-service-interface/create-service-interface.component';
import { UpdateServiceInterfaceComponent } from '../dialogs/update-service-interface/update-service-interface.component';
import { safeUnsubscribe } from '../util/utils';


@Component({
    selector: 'mico-service-detail-overview',
    templateUrl: './service-detail-overview.component.html',
    styleUrls: ['./service-detail-overview.component.css']
})
export class ServiceDetailOverviewComponent implements OnChanges, OnDestroy {

    private serviceSubscription: Subscription;
    private subDeleteServiceInterface: Subscription;
    private subServiceInterfaces: Subscription;
    private subVersion: Subscription;
    private subDependeesCall: Subscription;
    private subDependersCall: Subscription;
    private subProvide: Subscription;

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
    ) { }

    // dependees: services the current service depends on
    dependees: any[] = [];
    // dependers: services depending on the current service
    dependers: any[] = [];
    serviceInterfaces: any[] = [];

    // will be used by the update form
    serviceData;

    edit: Boolean = false;
    @Input() shortName: string;
    @Input() version: string;

    serviceOverviewFilter = ['predecessor', 'serviceInterfaces', 'dependencies'];

    ngOnChanges() {

        if (this.shortName != null && this.version != null) {

            this.handleSubscriptions();
            this.update();
        }
    }

    ngOnDestroy() {
        this.handleSubscriptions();
    }

    /**
     * unsubscribes all obervables which are not null
     */
    handleSubscriptions() {
        safeUnsubscribe(this.serviceSubscription);
        safeUnsubscribe(this.subDeleteServiceInterface);
        safeUnsubscribe(this.subServiceInterfaces);
        safeUnsubscribe(this.subVersion);
        safeUnsubscribe(this.subDependeesCall);
        safeUnsubscribe(this.subDependersCall);
        safeUnsubscribe(this.subProvide);
    }

    update() {

        safeUnsubscribe(this.serviceSubscription);
        this.serviceSubscription = this.apiService.getService(this.shortName, this.version)
            .subscribe(service => {
                this.serviceData = service;

                // get dependencies
                safeUnsubscribe(this.subDependersCall);
                this.subDependeesCall = this.apiService.getServiceDependees(this.shortName, this.version)
                    .subscribe(val => {
                        this.dependees = JSON.parse(JSON.stringify(val));
                    });

                safeUnsubscribe(this.subDependersCall);
                this.subDependeesCall = this.apiService.getServiceDependers(this.shortName, this.version)
                    .subscribe(val => {
                        this.dependers = val;
                    });


                // get interfaces
                safeUnsubscribe(this.subServiceInterfaces);
                this.subServiceInterfaces = this.apiService.getServiceInterfaces(this.shortName, this.version)
                    .subscribe(val => {
                        this.serviceInterfaces = val;
                    });

            });

    }

    /**
     * call back from the save button. Updates the services information.
     */
    save() {

        const subPutNewServiceInformation = this.apiService.putService(this.shortName, this.version, this.serviceData)
            .subscribe(val => {
                this.serviceData = val;
                this.shortName = val.shortName;
                this.version = val.version;
                safeUnsubscribe(subPutNewServiceInformation);
            });
        this.edit = false;
    }


    /**
     * action triggered in the ui to create a service interface
     */
    addProvides() {
        const dialogRef = this.dialog.open(CreateServiceInterfaceComponent);
        const subDialog = dialogRef.afterClosed().subscribe(result => {
            if (!result) {
                return;
            }
            this.apiService.postServiceInterface(this.shortName, this.version, result).subscribe();
            safeUnsubscribe(subDialog);
        });
    }

    /**
     * Action triggered in ui to edit an existing interface.
     *
     * @param serviceInterface the interface to update
     */
    editInterface(serviceInterface) {
        const dialogRef = this.dialog.open(UpdateServiceInterfaceComponent, {
            data: {
                serviceInterface: serviceInterface,
            }
        });
        this.subProvide = dialogRef.afterClosed().subscribe(result => {

            if (result == null || result === '') {
                return;
            }
            this.apiService.putServiceInterface(this.shortName, this.version, serviceInterface.serviceInterfaceName, result)
                .subscribe();
        });

    }

    /**
     * action triggered in ui
     */
    deleteServiceInterface(interfaceName) {
        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: interfaceName,
                question: 'deleteServiceInterface'
            }
        });

        const subDialog = dialogRef.afterClosed().subscribe(result => {
            if (!result) {
                return;
            }
            this.apiService.deleteServiceInterface(this.shortName, this.version, interfaceName).subscribe();
            safeUnsubscribe(subDialog);
        });
    }

    /**
     * action triggered in the ui
     * opens an dialog to select a new service the current service depends on.
     */
    addDependee() {

        // open dialog
        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: '',
                choice: 'single',
                existingDependencies: this.dependees,
                serviceId: this.shortName,
            }
        });

        // handle result
        const subDialog = dialogRef.afterClosed().subscribe(result => {
            if (!result) {
                return;
            }
            this.apiService.postServiceDependee(this.shortName, this.version, result[0]).subscribe();
            safeUnsubscribe(subDialog);
        });
    }


    /**
     * action triggered in the ui
     * Opens a dialog to remove the dependency from the current service to the selected service.
     */
    deleteDependency(dependee) {

        // open dialog
        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: dependee.shortName,
                question: 'deleteDependency'
            }
        });

        // handle result
        const subDialog = dialogRef.afterClosed().subscribe(result => {
            if (!result) {
                return;
            }
            this.apiService.deleteServiceDependee(this.shortName, this.version, dependee.shortName, dependee.version).subscribe();
            safeUnsubscribe(subDialog);
        });
    }
}
