import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
    selector: 'mico-deployment-information-dialog',
    templateUrl: './deployment-information-dialog.component.html',
    styleUrls: ['./deployment-information-dialog.component.css']
})
export class DeploymentInformationDialogComponent implements OnInit {

    deploymentInformation;
    changedDeploymentInformation;

    serviceName;

    constructor(
        public dialogRef: MatDialogRef<DeploymentInformationDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any
    ) {
        this.deploymentInformation = data.deploymentInformation;
        this.serviceName = data.serviceName;
    }

    confirm() {
        return this.changedDeploymentInformation;

    }

    ngOnInit() {
    }

}
