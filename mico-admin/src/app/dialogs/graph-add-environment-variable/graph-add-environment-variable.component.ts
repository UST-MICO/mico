/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
