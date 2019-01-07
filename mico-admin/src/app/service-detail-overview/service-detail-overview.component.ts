import { Component, Input, OnInit, OnDestroy, OnChanges } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { ServicePickerComponent } from '../dialogs/service-picker/service-picker.component';
import { MatDialog } from '@angular/material';
import { YesNoDialogComponent } from '../dialogs/yes-no-dialog/yes-no-dialog.component';
import { CreateServiceInterfaceComponent } from '../dialogs/create-service-interface/create-service-interface.component'

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
    private subInternalDependency: Subscription;
    private subExternalDependency: Subscription;
    private subDeleteDependency: Subscription;
    private subDeleteServiceInterface: Subscription;
    private subServiceInterfaces: Subscription;
    private subVersion: Subscription;

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
        private dialog: MatDialog,
    ) { }

    internalDependencies = [];
    externalDependencies = [];
    serviceInterfaces: any;


    // will be used by the update form
    serviceData;

    edit: Boolean = false;
    @Input() shortName: string;
    @Input() version: string;
    oldShortName: string;
    oldVersion: string;


    ngOnChanges() {

        console.log('onChanges()', this.shortName, this.version);

        if (this.shortName != null && this.version != null) {
            console.log('if successful');
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
        this.unsubscribe(this.subInternalDependency);
        this.unsubscribe(this.subExternalDependency);
        this.unsubscribe(this.subDeleteDependency);
        this.unsubscribe(this.subDeleteServiceInterface);
        this.unsubscribe(this.subServiceInterfaces);
        this.unsubscribe(this.subVersion);
    }

    unsubscribe(subscription: Subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    update() {

        // TODO take care of the version
        if (this.oldShortName === this.shortName) {
            if (this.oldVersion === this.version) {
                console.log('return', this.oldShortName, this.shortName, this.oldVersion, this.version);
                return;
            }
        }

        this.oldShortName = this.shortName;
        this.oldVersion = this.version;



        if (this.serviceSubscription != null) {
            this.serviceSubscription.unsubscribe();
        }
        if (this.subServiceInterfaces != null) {
            this.subServiceInterfaces.unsubscribe();
        }

        this.serviceSubscription = this.apiService.getService(this.shortName, this.version)
            .subscribe(service => {
                this.serviceData = service;


                // get dependencies and their status
                const internal = [];
                if (this.serviceData != null && this.serviceData.internalDependencies != null) {
                    this.serviceData.internalDependencies.forEach(element => {
                        internal.push(this.getServiceMetaData(element));
                    });

                    this.internalDependencies = internal;
                }

                const external = [];
                if (this.serviceData != null && this.serviceData.externalDependencies != null) {
                    this.serviceData.externalDependencies.forEach(element => {
                        external.push(this.getServiceMetaData(element));
                    });

                    this.externalDependencies = external;
                }

                this.subServiceInterfaces = this.apiService.getServiceInterfaces(this.shortName, this.version)
                    .subscribe(element => {
                        this.serviceInterfaces = element;
                    });
            });

    }

    editOrSave() {
        if (this.edit) {
            // TODO save content
        }
        this.edit = !this.edit;
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
     * action triggered in ui
     */
    addProvides() {
        const dialogRef = this.dialog.open(CreateServiceInterfaceComponent);
        this.subProvide = dialogRef.afterClosed().subscribe(result => {
            console.log(result);
            // TODO use result in a useful way
        });

    }

    /**
     * action triggered in ui
     */
    addInternalDependency() {
        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: 'internal',
                choice: 'multi',
                exisitingDependencies: this.internalDependencies,
                serviceId: this.shortName,
            }
        });
        this.subInternalDependency = dialogRef.afterClosed().subscribe(result => {
            console.log(result);
            // TODO use result in a useful way
        });
    }

    /**
     * action triggered in ui
     */
    addExternalDependency() {
        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: 'external',
                choice: 'multi',
                exisitingDependencies: this.externalDependencies,
                serviceId: this.shortName,
            }
        });
        this.subExternalDependency = dialogRef.afterClosed().subscribe(result => {
            console.log(result);
            // TODO use result in a useful way
        });
    }

    /**
     * action triggered in ui
     */
    deleteDependency(id) {

        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: this.getServiceMetaData(id).shortName,
                question: 'deleteDependency',
            }
        });

        this.subDeleteDependency = dialogRef.afterClosed().subscribe(result => {
            if (result) {
                console.log('delete ' + id);
                // TODO really delete the dependency
            }
        });

    }

    /**
     * action triggered in ui
     */
    deleteServiceInterface(id) {
        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: id,
                question: 'deleteServiceInterface',
            }
        });

        this.subDeleteServiceInterface = dialogRef.afterClosed().subscribe(result => {
            if (result) {
                console.log('delete ' + id);
                // TODO really delete the dependency
            }
        });
    }
}
