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

import { Component, Input, OnChanges, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';
import { safeUnsubscribeList, safeUnsubscribe } from '../util/utils';
import { take } from 'rxjs/operators';

@Component({
    selector: 'mico-app-detail-public-ip',
    templateUrl: './app-detail-public-ip.component.html',
    styleUrls: ['./app-detail-public-ip.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppDetailPublicIpComponent implements OnInit, OnChanges, OnDestroy {

    private subApplication: Subscription;
    private subPublicIps: Subscription[];
    private subServiceInterfaces: Subscription[];
    private subPolling: Subscription[];

    @Input() applicationShortName;
    @Input() applicationVersion;

    publicIps = new Map();

    constructor(
        private apiService: ApiService,
        private changeDedection: ChangeDetectorRef,
    ) { }

    ngOnInit() {
        this.subPublicIps = [];
        this.subServiceInterfaces = [];
        this.subPolling = [];
    }

    ngOnDestroy() {
        safeUnsubscribe(this.subApplication);
        safeUnsubscribeList(this.subPublicIps);
        safeUnsubscribeList(this.subServiceInterfaces);
        safeUnsubscribeList(this.subPolling);
    }


    ngOnChanges(changes) {

        if ((changes.hasOwnProperty('applicationShortName') && changes.applicationShortName.currentValue != null) ||
            (changes.hasOwnProperty('applicationVersion') && changes.applicationVersion.currentValue != null)) {

            this.publicIps.clear();
        }

        if (this.applicationShortName != null && this.applicationVersion != null) {

            safeUnsubscribeList(this.subPublicIps);
            safeUnsubscribeList(this.subServiceInterfaces);
            safeUnsubscribeList(this.subPolling);

            // get the public ips
            safeUnsubscribe(this.subApplication);
            this.subApplication = this.apiService.getApplication(this.applicationShortName, this.applicationVersion)
                .pipe(take(1))
                .subscribe(application => {

                    safeUnsubscribeList(this.subServiceInterfaces);
                    this.subServiceInterfaces = [];
                    safeUnsubscribeList(this.subPublicIps);
                    this.subPublicIps = [];
                    safeUnsubscribeList(this.subPolling);
                    this.subPolling = [];

                    application.services.forEach(service => {

                        // assumption: one public ip per interface
                        this.subServiceInterfaces.push(this.apiService.getServiceInterfaces(service.shortName, service.version)
                            .pipe(take(1))
                            .subscribe(serviceInterfaces => {

                                serviceInterfaces.forEach(micoInterface => {
                                    this.subPublicIps.push(this.apiService
                                        .getServiceInterfacePublicIp(service.shortName, service.version, micoInterface.serviceInterfaceName)
                                        .subscribe(publicIpDTO => {

                                            if (publicIpDTO.externalIpIsAvailable) {
                                                // public ip is already present

                                                this.publicIps.set(service.shortName + '#' + publicIpDTO.name, publicIpDTO);
                                                this.changeDedection.markForCheck();

                                                // end polling;
                                                safeUnsubscribe(tempSubPolling);
                                            }

                                        }));

                                    // start polling
                                    const tempSubPolling = this.apiService.pollServiceInterfacePublicIp(service.shortName,
                                        service.version, micoInterface.serviceInterfaceName);
                                    this.subPolling.push(tempSubPolling);
                                });
                            }));

                    });
                });
        }
    }
}
