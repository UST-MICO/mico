import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { ServicePickerComponent } from '../dialogs/service-picker/service-picker.component';
import { MatDialog } from '@angular/material';
import { CreateServiceDialogComponent } from '../dialogs/create-service/create-service.component';


@Component({
    selector: 'mico-service-detail-overview',
    templateUrl: './service-detail-overview.component.html',
    styleUrls: ['./service-detail-overview.component.css']
})
export class ServiceDetailOverviewComponent implements OnInit {

    private serviceSubscription: Subscription;
    private paramSubscription: Subscription;

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
        private dialog: MatDialog,
    ) { }

    @Input() service: ApiObject;
    @Input() internalDependencies = [];
    @Input() externalDependencies = [];

    // will be used by the update form
    serviceData;

    edit: Boolean = false;
    id: number;

    ngOnInit() {

        this.paramSubscription = this.route.params.subscribe(params => {
            this.update(parseInt(params['id'], 10));
        });

    }

    update(id) {
        if (id === this.id) {
            return;
        } else {

            this.id = id;

            if (this.serviceSubscription != null) {
                this.serviceSubscription.unsubscribe();
            }
        }

        this.serviceSubscription = this.serviceSubscription = this.apiService.getServiceById(id)
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
    }

    editOrSave() {
        console.log('edit or save');
        if (this.edit) {
            // TODO save content
        }
        this.edit = !this.edit;
    }

    getServiceMetaData(id) {
        let service_object;
        this.apiService.getServiceById(id).subscribe(val => service_object = val);
        const return_object = {
            'id': id,
            'name': service_object.name,
            'status': service_object.status,
        };
        return return_object;
    }

    addInternalDependencie() {
        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: "internal",
                exisitingDependencies: this.internalDependencies,
                serviceId: this.id,
            }
        });
        dialogRef.afterClosed().subscribe(result => {
            console.log(result);
        });
        // TODO use result in a useful way
    }

    addExternalDependencie() {
        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: "external",
                exisitingDependencies: this.externalDependencies,
                serviceId: this.id,
            }
        });
        dialogRef.afterClosed().subscribe(result => {
            console.log(result);
        });
        // TODO use result in a useful way
    }

}
