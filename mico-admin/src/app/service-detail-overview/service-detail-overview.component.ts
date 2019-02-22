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

import { Component, Input, OnInit, OnDestroy, OnChanges, SimpleChanges } from '@angular/core';
import { Subscription } from 'rxjs';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { ServicePickerComponent } from '../dialogs/service-picker/service-picker.component';
import { MatDialog } from '@angular/material';
import { YesNoDialogComponent } from '../dialogs/yes-no-dialog/yes-no-dialog.component';
import { CreateServiceInterfaceComponent } from '../dialogs/create-service-interface/create-service-interface.component';
import { Router } from '@angular/router';

export interface Dependency {
    id;
    version;
}

@Component({
    selector: 'mico-service-detail-overview',
    templateUrl: './service-detail-overview.component.html',
    styleUrls: ['./service-detail-overview.component.css']
})
export class ServiceDetailOverviewComponent implements OnChanges, OnDestroy {

    private serviceSubscription: Subscription;
    private subProvide: Subscription;
    private subDependeesDialog: Subscription;
    private subDependersDialog: Subscription;
    private subDeleteDependency: Subscription;
    private subDeleteServiceInterface: Subscription;
    private subServiceInterfaces: Subscription;
    private subVersion: Subscription;
    private subDependeesCall: Subscription;

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
        private router: Router,
    ) { }

    // dependees: services the current service depends on
    dependees: any = [];
    // dependers: services depending on the current service
    dependers: any = [];
    serviceInterfaces: any = [];

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

    handleSubscriptions() {
        this.unsubscribe(this.serviceSubscription);
        this.unsubscribe(this.subProvide);
        this.unsubscribe(this.subDependeesDialog);
        this.unsubscribe(this.subDependersDialog);
        this.unsubscribe(this.subDeleteDependency);
        this.unsubscribe(this.subDeleteServiceInterface);
        this.unsubscribe(this.subServiceInterfaces);
        this.unsubscribe(this.subVersion);
        this.unsubscribe(this.subDependeesCall);
    }

    unsubscribe(subscription: Subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    update() {

        if (this.serviceSubscription != null) {
            this.serviceSubscription.unsubscribe();
        }
        if (this.subServiceInterfaces != null) {
            this.subServiceInterfaces.unsubscribe();
        }

        this.serviceSubscription = this.apiService.getService(this.shortName, this.version)
            .subscribe(service => {
                this.serviceData = service;

                // get dependencies
                if (this.subDependeesCall != null) {
                    this.subDependeesCall.unsubscribe();
                }

                this.subDependeesCall = this.apiService.getServiceDependees(this.shortName, this.version)
                    .subscribe(val => {
                        this.dependees = val;
                    });

                // TODO insert as soon as get/service/dependers is in master
                /*
                if (this.subDependersCall != null) {
                    this.subDependersCall.unsubscribe();
                }

                this.subDependeesCall = this.apiService.getServiceDependers(this.shortName, this.version)
                    .subscribe(val => {
                        this.dependers = val;
                    });
*/


                this.subServiceInterfaces = this.apiService.getServiceInterfaces(this.shortName, this.version)
                    .subscribe(val => {
                        this.serviceInterfaces = val;
                    });

            });

    }

    save() {

        this.apiService.putService(this.shortName, this.version, this.serviceData).subscribe(val => {
            this.serviceData = val;
            this.shortName = val.shortName;
            this.version = val.version;
        });
        this.edit = false;
    }

    getServiceMetaData(service) {
        // TODO change method calls to hold a proper service
        let service_object;
        this.apiService.getService(service.id, service.version).subscribe(val => service_object = val);
        const tempObject = {
            'id': service.id,
            'version': service.version,
            'name': service_object.name,
            'shortName': service_object.shortName,
            'status': service_object.status,
        };
        return tempObject;
    }

    /**
     * action triggered in ui to create a service interface
     */
    addProvides() {
        const dialogRef = this.dialog.open(CreateServiceInterfaceComponent);
        this.subProvide = dialogRef.afterClosed().subscribe(result => {
            if (result === '') {
                return;
            }
            this.apiService.postServiceInterface(this.shortName, this.version, result).subscribe();
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

        this.subDeleteServiceInterface = dialogRef.afterClosed().subscribe(shouldDelete => {
            if (shouldDelete) {
                this.apiService.deleteServiceInterface(this.shortName, this.version, interfaceName).subscribe();
            }
        });
    }

    /**
     * action triggered in ui
     * opens an dialog to select a new service the current service depends on.
     */
    addDependee() {

        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: '',
                choice: 'multi',
                existingDependencies: this.dependees,
                serviceId: this.shortName,
            }
        });
        this.subDependeesDialog = dialogRef.afterClosed().subscribe(result => {
            console.log(result);
            // TODO use result in a useful way
        });
    }


    /**
     * action triggered in ui
     * Opens a dialog to remove the dependency from the current service to the selected service.
     */
    deleteDependency(id) {
        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: this.getServiceMetaData(id).shortName,
                question: 'deleteDependency'
            }
        });

        this.subDeleteDependency = dialogRef.afterClosed().subscribe(result => {
            if (result) {
                console.log('delete ' + id);
                // TODO really delete the dependency
            }
        });
    }

    deleteService() {

        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: { shortName: this.shortName, version: this.version },
                question: 'deleteService'
            }
        });

        this.subDeleteDependency = dialogRef.afterClosed().subscribe(result => {
            if (result) {

                this.apiService.deleteService(this.shortName, this.version).subscribe();
                this.router.navigate(['../service-detail/service-list']);
            }
        });
    }
}
