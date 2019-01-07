import { Component, Input, OnInit, OnDestroy, OnChanges } from '@angular/core';
import { Subscription } from 'rxjs';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { ServicePickerComponent } from '../dialogs/service-picker/service-picker.component';
import { MatDialog } from '@angular/material';
import { YesNoDialogComponent } from '../dialogs/yes-no-dialog/yes-no-dialog.component';
import { CreateServiceInterfaceComponent } from '../dialogs/create-service-interface/create-service-interface.component';

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

    constructor(private apiService: ApiService, private dialog: MatDialog) { }

    dependees: any = [];
    dependers: any = [];
    serviceInterfaces: any = [];

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

                console.log(this.serviceData);


                // get dependencies
                if (this.subDependeesCall != null) {
                    this.subDependeesCall.unsubscribe();
                }

                this.subDependeesCall = this.apiService.getServiceDependees(this.shortName, this.version)
                    .subscribe(val => {
                        console.log('dependees', val);
                        this.dependees = val;
                    });

                // TODO insert as soon as get/service/dependers is in master
                /*
                if (this.subDependersCall != null) {
                    this.subDependersCall.unsubscribe();
                }

                this.subDependeesCall = this.apiService.getServiceDependers(this.shortName, this.version)
                    .subscribe(val => {
                        console.log(val);
                        this.dependers = val;
                    });
*/


                // TODO insert as soon as there are interfaces in the dummy data
                /*
                this.subServiceInterfaces = this.apiService.getServiceInterfaces(this.shortName, this.version)
                    .subscribe(val => {
                        console.log('interfaces:', val);
                        this.serviceInterfaces = val;
                    });
                */
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
    addDependee() {
        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: 'internal',
                choice: 'multi',
                exisitingDependencies: this.dependees,
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
     */
    addExternalDependency() {
        // TODO is this method still relevant? There are no internal/external dependencies in the backend.
        // So the 'External' list was changed to dependers.
        // Adding dependers is no useful operation at this point of the ui.
        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: 'external',
                choice: 'multi',
                exisitingDependencies: this.dependers,
                serviceId: this.shortName,
            }
        });
        this.subDependersDialog = dialogRef.afterClosed().subscribe(result => {
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

    /**
     * action triggered in ui
     */
    deleteServiceInterface(id) {
        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: id,
                question: 'deleteServiceInterface'
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
