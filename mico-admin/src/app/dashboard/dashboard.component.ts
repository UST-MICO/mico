import { Component, OnInit, Input } from '@angular/core';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { MatDialog } from '@angular/material';
import { CreateServiceDialogComponent } from '../dialogs/create-service/create-service.component';

@Component({
    selector: 'mico-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
    ) {
        this.getApplications();
    }

    applications: ApiObject;

    displayedColumns: string[] = ['id', 'name', 'shortName'];

    ngOnInit() {


        // test calls
        this.apiService.getServices().subscribe(val => {
            console.log('getServices', val);
        });

        this.apiService.getServiceVersions('test.service').subscribe(val => {
            console.log('getServiceVersions', val);
        });

        this.apiService.getService('test.service', 'v1').subscribe(val => {
            console.log('getService', val);
        });

        /*
        // Throws exceptions in the backend
        this.apiService.getServiceDependees('test.service', 'v1').subscribe(val => {
            console.log('getServiceDependees', val);
        });
        */

        this.apiService.getServiceDependers('test.service', 'v1').subscribe(val => {
            console.log('getServiceDependers', val);
        });
    }


    /**
     * receives a list of applications from the apiService
     */
    getApplications(): void {
        this.apiService.getApplications()
            .subscribe(applications => this.applications = applications);
    }


    newService(): void {
        const dialogRef = this.dialog.open(CreateServiceDialogComponent);
        dialogRef.afterClosed().subscribe(result => {
            console.log(result);
        });
    }



}
