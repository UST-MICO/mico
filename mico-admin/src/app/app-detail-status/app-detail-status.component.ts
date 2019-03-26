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

import { Component, Input, OnChanges, OnDestroy } from '@angular/core';
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';
import { safeUnsubscribe } from '../util/utils';

@Component({
    selector: 'mico-app-detail-status',
    templateUrl: './app-detail-status.component.html',
    styleUrls: ['./app-detail-status.component.css']
})
export class AppDetailStatusComponent implements OnChanges, OnDestroy {

    subApplicationStatus: Subscription;

    constructor(
        private apiService: ApiService,
    ) { }

    @Input() shortName;
    @Input() version;

    applicationStatus;

    ngOnChanges() {
        if (this.shortName == null || this.version == null) {
            return;
        }

        // get and set applicationStatus
        safeUnsubscribe(this.subApplicationStatus);
        this.subApplicationStatus = this.apiService.getApplicationStatus(this.shortName, this.version)
            .subscribe(val => {
                this.applicationStatus = JSON.parse(JSON.stringify(val));
            });

    }

    ngOnDestroy() {
        // unsubscribe from observables
        safeUnsubscribe(this.subApplicationStatus);
    }
}
