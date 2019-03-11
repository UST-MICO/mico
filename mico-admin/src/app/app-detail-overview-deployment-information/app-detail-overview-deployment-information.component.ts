import { Component, Input, OnChanges } from '@angular/core';
import { ApiService } from '../api/api.service';
import { DeploymentInformationDialogComponent } from '../dialogs/deployment-information-dialog/deployment-information-dialog.component';
import { MatDialog } from '@angular/material';

@Component({
    selector: 'mico-app-detail-overview-deployment-information',
    templateUrl: './app-detail-overview-deployment-information.component.html',
    styleUrls: ['./app-detail-overview-deployment-information.component.css']
})
export class AppDetailOverviewDeploymentInformationComponent implements OnChanges {

    constructor(private apiService: ApiService,
        private dialog: MatDialog) { }

    @Input() service;
    @Input() applicationShortName: string;
    @Input() applicationVersion: string;

    deploymentInformation;

    ngOnChanges() {
        if (this.service != null && this.applicationShortName != null && this.applicationVersion != null) {

            this.apiService.getServiceDeploymentInformation(this.applicationShortName, this.applicationVersion, this.service.shortName)
                .subscribe(val => {
                    this.deploymentInformation = JSON.parse(JSON.stringify(val));
                });
        }
    }

    openSettings() {
        const dialogRef = this.dialog.open(DeploymentInformationDialogComponent, {
            data: {
                deploymentInformation: this.deploymentInformation,
            }
        });

        const subDialog = dialogRef.afterClosed().subscribe(val => {

            if (val) {

                this.apiService.putServiceDeploymentInformation(this.applicationShortName, this.applicationVersion,
                    this.service.shortName, val)
                    .subscribe();
                subDialog.unsubscribe();
            }
        });
    }

}
