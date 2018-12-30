import { Component, Input, OnInit, OnDestroy } from '@angular/core';
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
export class ServiceDetailOverviewComponent implements OnInit, OnDestroy {

    private serviceSubscription: Subscription;
    private paramSubscription: Subscription;
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

    @Input() service: ApiObject;
    internalDependencies = [];
    externalDependencies = [];
    serviceInterfaces: any;


    // will be used by the update form
    serviceData;

    edit: Boolean = false;
    shortName: string;
    version: string;

    ngOnInit() {

        this.paramSubscription = this.route.params.subscribe(params => {

            const shortName = params['shortName'];
            console.log('service-detail-overview', this.service);

            this.update(shortName, this.service.version);
        });
    }

    ngOnDestroy() {
        this.unsubscribe(this.serviceSubscription);
        this.unsubscribe(this.paramSubscription);
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

    update(shortName, version) {

        // TODO take care of the version
        if (shortName === this.shortName) {
            return;
        } else {

            this.shortName = shortName;

            if (this.serviceSubscription != null) {
                this.serviceSubscription.unsubscribe();
            }
        }

        this.serviceSubscription = this.serviceSubscription = this.apiService.getService(shortName, version)
            .subscribe(service => this.service = service);

        // get dependencies and their status
        const internal = [];
        this.service.internalDependencies.forEach(element => {
            internal.push(this.getServiceMetaData(element));
        });
        this.internalDependencies = internal;

        const external = [];
        this.service.externalDependencies.forEach(element => {
            external.push(this.getServiceMetaData(element));
        });
        this.externalDependencies = external;

        // TODO insert dropdown to choose a service
        this.subServiceInterfaces = this.apiService.getServiceInterfaces(shortName, version)
            .subscribe(element => this.serviceInterfaces = element);


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
