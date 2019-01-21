import { Component, OnInit, Input } from '@angular/core';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { MatDialog } from '@angular/material';
import { CreateServiceDialogComponent } from '../dialogs/create-service/create-service.component';
import { Router } from '@angular/router';
import { CreateApplicationComponent } from '../dialogs/create-application/create-application.component';

@Component({
    selector: 'mico-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
        private router: Router,
    ) {
        this.getApplications();
    }

    applications: ApiObject;

    displayedColumns: string[] = ['id', 'name', 'shortName'];

    ngOnInit() {

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
            // filter empty results (when dialog is aborted)
            if (result !== '') {

                // check if returned object is complete
                for (const property in result) {
                    if (result[property] == null) {
                        // TODO add some user feed back
                        return;
                    }
                }
                this.apiService.postService(result).subscribe(val => {
                    this.router.navigate(['service-detail', val.shortName, val.version]);
                });
            }
        });
    }

    newApplication() {
        const dialogRef = this.dialog.open(CreateApplicationComponent);
        dialogRef.afterClosed().subscribe(result => {

            // filter empty results (when dialog is aborted)
            if (result !== '') {

                // check if returned object is complete
                for (const property in result.applicationProperties) {
                    if (result.applicationProperties[property] == null) {
                        // TODO add some user feed back
                        return;
                    }
                }

                // check if returned object has services
                if (result.services.length <= 0) {
                    return;
                }

                const data = result.applicationProperties;
                data.dependsOn = result.services;

                this.apiService.postApplication(data).subscribe(val => {
                    this.router.navigate(['app-detail', val.shortName, val.version]);
                });
            }
        });
    }



}
