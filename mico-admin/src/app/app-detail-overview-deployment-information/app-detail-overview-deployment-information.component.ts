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
                serviceName: this.service.name,
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
