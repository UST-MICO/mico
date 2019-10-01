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

import { Component, OnChanges, Input, OnDestroy } from '@angular/core';
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';
import { safeUnsubscribe } from '../util/utils';
import { ApiObject } from '../api/apiobject';

@Component({
    selector: 'mico-service-detail-status',
    templateUrl: './service-detail-status.component.html',
    styleUrls: ['./service-detail-status.component.css']
})
export class ServiceDetailStatusComponent implements OnChanges, OnDestroy {

    @Input() shortName;
    @Input() version;

    subServiceStatus: Subscription;

    serviceStatusList: ApiObject[];
    blackList = ['shortName', 'version', 'name'];

    constructor(
        private apiService: ApiService,
    ) { }

    ngOnChanges() {
        if (this.shortName == null || this.version == null) {
            return;
        }

        // get and set serviceStatus
        this.apiService.getServiceStatus(this.shortName, this.version).subscribe(val => {
            this.serviceStatusList = JSON.parse(JSON.stringify(val));
        });
    }

    ngOnDestroy() {
        // unsubscribe from observables
        safeUnsubscribe(this.subServiceStatus);
    }

}
