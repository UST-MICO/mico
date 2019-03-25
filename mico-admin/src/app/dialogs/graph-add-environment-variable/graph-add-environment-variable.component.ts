import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { ApiService } from 'src/app/api/api.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'mico-graph-add-environment-variable',
    templateUrl: './graph-add-environment-variable.component.html',
    styleUrls: ['./graph-add-environment-variable.component.css']
})
export class GraphAddEnvironmentVariableComponent implements OnInit {

    constructor(public dialogRef: MatDialogRef<GraphAddEnvironmentVariableComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any,
        private apiService: ApiService,
    ) {

        this.applicationShortName = data.applicationShortName;
        this.applicationVersion = data.applicationVersion;
        this.serviceShortName = data.serviceShortName;
        this.interfaceName = data.interfaceName;
    }

    applicationShortName;
    applicationVersion;
    serviceShortName;
    interfaceName;

    deploymentInformation;

    chosenEnvVar;

    // TODO unsubsribe
    subDeploymentInformation: Subscription;

    ngOnInit() {
        this.subDeploymentInformation = this.apiService.getServiceDeploymentInformation(this.applicationShortName,
            this.applicationVersion, this.serviceShortName)
            .subscribe(val => {
                this.deploymentInformation = val;
            });
    }

}
