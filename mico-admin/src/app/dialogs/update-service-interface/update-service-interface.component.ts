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

import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';

@Component({
    selector: 'mico-update-service-interface',
    templateUrl: './update-service-interface.component.html',
    styleUrls: ['./update-service-interface.component.css']
})
export class UpdateServiceInterfaceComponent {


    constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
        this.serviceInterfaceName = data.serviceInterface.serviceInterfaceName;
        this.interfaceData = JSON.parse(JSON.stringify(data.serviceInterface));
    }

    serviceInterfaceName;

    // form elements are stored in here
    interfaceData;

    valid: boolean = false;

    /**
     * return method of the dialog
     */
    confirmButton() {
        if (!this.valid) {
            return null;
        }
        this.interfaceData.serviceInterfaceName = this.serviceInterfaceName;
        return this.interfaceData;
    }

}
