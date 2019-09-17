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
    selector: 'mico-graph-update-faas-function',
    templateUrl: './graph-update-faas-function.component.html',
    styleUrls: ['./graph-update-faas-function.component.css']
})
export class GraphUpdateFaasFunctionComponent {


    constructor(public dialogRef: MatDialogRef<GraphUpdateFaasFunctionComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any,
    ) {
        this.kfConnectorInfo = data.kfConnectorInfo;
        this.faasFunction = this.kfConnectorInfo.openFaaSFunctionName;
    }

    kfConnectorInfo;

    faasFunction;

    isValid() {
        if (this.faasFunction == null || this.faasFunction === '') {
            return true;
        }
        if (this.faasFunction.length > 63) {
            return false;
        }
        return /^[a-z0-9\-]+$/.test(this.faasFunction);
    }

    /**
     * return method of the dialog
     */
    response() {
        if (!this.isValid()) {
            // invalid topic!
            return '';
        }

        let faasFunction = this.faasFunction;

        if (faasFunction === '') {
            faasFunction = null;
        }

        return {
            openFaaSFunctionName: faasFunction,
        };
    }

}
