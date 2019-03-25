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

import { Component, OnInit, OnDestroy } from '@angular/core';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { groupBy, mergeMap, toArray, map } from 'rxjs/operators';
import { Subscription, from } from 'rxjs';
import { safeUnsubscribe } from '../util/utils';
import { UtilServiceService } from '../util/util-service.service';

@Component({
    selector: 'mico-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {

    constructor(
        private apiService: ApiService,
        private utilService: UtilServiceService,
    ) { }

    subApplications: Subscription;
    applications: Readonly<ApiObject[]>;
    displayedColumns: string[] = ['name', 'shortName'];

    ngOnInit() {
        this.getApplications();
    }

    ngOnDestroy() {
        // unsubscribe from observables
        safeUnsubscribe(this.subApplications);
    }


    /**
     * receives a list of applications from the apiService and group them by the shortName
     */
    getApplications(): void {


        // group applications by shortName
        this.subApplications = this.apiService.getApplications()
            .subscribe(val => {
                from(val as unknown as ArrayLike<ApiObject>)
                    .pipe(
                        // group
                        groupBy(application => application.shortName),
                        // cast groups to arrays
                        mergeMap(group => group.pipe(toArray())),
                        // take first element from each group
                        map(group => group[0]),
                        // create an array from those first elements
                        toArray()
                    ).subscribe(applicationList => {
                        this.applications = applicationList;
                    });

            });
    }


    /**
     * opens a dialog to create a new service.
     */
    newService(): void {
        this.utilService.createNewService();
    }

    /**
     * create a new application
     * uses: POST application
     */
    newApplication() {
        this.utilService.createNewApplication();
    }

    // links to our related pages
    openUserGuide() {
        window.open('https://mico-docs.readthedocs.io/en/latest/');
    }
    openDevGuide() {
        window.open('https://mico-dev.readthedocs.io/en/latest/');
    }
    suggestFeature() {
        window.open('https://github.com/UST-MICO/mico/issues/new');
    }

}
