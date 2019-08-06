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
import { Subscription } from 'rxjs';

import { ApiService } from '../api/api.service';
import { safeUnsubscribe } from '../util/utils';

@Component({
    selector: 'mico-service-detail-kubeconfig',
    templateUrl: './service-detail-kubeconfig.component.html',
    styleUrls: ['./service-detail-kubeconfig.component.css']
})
export class ServiceDetailKubeconfigComponent implements OnChanges, OnDestroy {

    private subYaml: Subscription;

    @Input() shortName;
    @Input() version;

    kubeConfigYaml: string;

    constructor(
        private apiService: ApiService,
    ) { }

    ngOnChanges() {
        if (this.shortName != null && this.version != null) {

            // get the yaml string

            safeUnsubscribe(this.subYaml);
            this.subYaml = this.apiService.getServiceYamlConfig(this.shortName, this.version)
                .subscribe(val => {
                    this.kubeConfigYaml = val.yaml;
                });
        }
    }

    /**
     * unsubscribe safely from all subscriptions
     */
    ngOnDestroy() {
        safeUnsubscribe(this.subYaml);
    }
}
