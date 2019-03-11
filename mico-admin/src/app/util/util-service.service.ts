import { Injectable } from '@angular/core';
import { ApiService } from '../api/api.service';
import { MatDialog, MatSnackBar } from '@angular/material';
import { Router } from '@angular/router';
import { safeUnsubscribe } from './utils';
import { CreateServiceDialogComponent } from '../dialogs/create-service/create-service.component';
import { CreateApplicationComponent } from '../dialogs/create-application/create-application.component';

@Injectable({
    providedIn: 'root'
})
export class UtilServiceService {

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
        private snackBar: MatSnackBar,
        private router: Router,
    ) { }

    /**
     * dialog to create a new service. can be done:
     * - manually
     * - via github import
     * uses: POST services or POST services/import/github
     */
    createNewService(): void {
        const dialogRef = this.dialog.open(CreateServiceDialogComponent);

        const subDialog = dialogRef.afterClosed().subscribe(result => {

            // filter empty results (when dialog is aborted)
            if (!result) {
                return;
            }

            // check if returned object is complete
            for (const property in result.data) {
                if (result.data[property] == null) {

                    if (property !== 'serviceInterfaces') {
                        this.snackBar.open('Missing property: ' + property, 'Ok', {
                            duration: 8000,
                        });
                        return;
                    }
                }
            }

            // decide if the service was created manually or is to be created via github crawler and create service
            if (result.tab === 'manual') {
                this.apiService.postService(result.data).subscribe(val => {
                    this.router.navigate(['service-detail', val.shortName, val.version]);
                });
            } else if (result.tab === 'github') {

                this.apiService.postServiceViaGithub(result.data.url, result.data.version).subscribe(val => {
                    this.router.navigate(['service-detail', val.shortName, val.version]);
                });
            }

            safeUnsubscribe(subDialog);
        });
    }

    /**
     * create a new application
     * uses: POST application
     */
    createNewApplication() {
        const dialogRef = this.dialog.open(CreateApplicationComponent);

        const subDialog = dialogRef.afterClosed().subscribe(result => {

            // filter empty results (when dialog is aborted)
            if (!result) {
                return;
            }

            // check if returned object is complete
            for (const property in result.applicationProperties) {
                if (result.applicationProperties[property] == null) {

                    // show an error message containg the missing field
                    this.snackBar.open('Missing property: ' + property, 'Ok', {
                        duration: 8000,
                    });
                    return;
                }
            }

            const data = result.applicationProperties;
            data.services = result.services;

            this.apiService.postApplication(data).subscribe(val => {
                result.services.forEach(service => {
                    const tempSubscription = this.apiService
                        .postApplicationServices(val.shortName, val.version, service.shortName, service.version)
                        .subscribe(element => {
                            safeUnsubscribe(tempSubscription);
                        });
                });
                this.router.navigate(['app-detail', val.shortName, val.version]);
            });

            safeUnsubscribe(subDialog);
        });
    }

}
