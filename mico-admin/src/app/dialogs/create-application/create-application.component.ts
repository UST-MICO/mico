import { Component, OnInit, OnDestroy } from '@angular/core';
import { ApiService } from 'src/app/api/api.service';
import { Subscription } from 'rxjs';
import { MatDialog } from '@angular/material';
import { ServicePickerComponent } from '../service-picker/service-picker.component';
import { ApiModel } from 'src/app/api/apimodel';

@Component({
    selector: 'mico-create-application',
    templateUrl: './create-application.component.html',
    styleUrls: ['./create-application.component.css']
})
export class CreateApplicationComponent implements OnInit, OnDestroy {

    subDependeesDialog: Subscription;
    subModelDefinitions: Subscription;
    services = [];

    applicationData;

    filterList: string[];

    constructor(private apiService: ApiService, private dialog: MatDialog) {
        this.subModelDefinitions = this.apiService.getModelDefinitions().subscribe(val => {
            this.filterList = (val['MicoApplication'] as ApiModel).required;
        });
    }

    ngOnInit() {
    }

    ngOnDestroy() {
        this.unsubscribe(this.subDependeesDialog);
        this.unsubscribe(this.subModelDefinitions);
    }

    unsubscribe(subscription: Subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    save() {
        return { applicationProperties: this.applicationData, services: this.services };
    }

    pickServices() {
        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: '',
                choice: 'multi',
                existingDependencies: this.services,
                serviceId: '',
            }
        });
        this.subDependeesDialog = dialogRef.afterClosed().subscribe(result => {
            result.forEach(element => {
                this.services.push(element);
            });
        });
    }

    deleteDependency(element) {
        const index = this.services.indexOf(element);
        if (index > -1) {
            this.services.splice(index, 1);
        }
    }

}
