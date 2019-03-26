import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
    selector: 'mico-graph-add-environment-variable',
    templateUrl: './graph-add-environment-variable.component.html',
    styleUrls: ['./graph-add-environment-variable.component.css']
})
export class GraphAddEnvironmentVariableComponent {


    constructor(public dialogRef: MatDialogRef<GraphAddEnvironmentVariableComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any,
    ) {

        this.serviceShortName = data.serviceShortName;
        this.targetServiceShortName = data.targetServiceShortName;
        this.interfaceName = data.interfaceName;
    }

    serviceShortName;
    targetServiceShortName;
    interfaceName;

    chosenEnvVar;

    /**
     * return method of the dialog
     */
    response() {

        return {
            micoServiceInterfaceName: this.interfaceName,
            micoServiceShortName: this.targetServiceShortName,
            environmentVariableName: this.chosenEnvVar
        };
    }

}
