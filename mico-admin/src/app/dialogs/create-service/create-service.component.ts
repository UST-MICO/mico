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
import { ApiService } from 'src/app/api/api.service';
import { Subscription } from 'rxjs';
import { ApiModel } from 'src/app/api/apimodel';
import { versionComparator } from 'src/app/api/semantic-version';
import { UtilsService } from 'src/app/util/utils.service';

@Component({
    selector: 'mico-create-service',
    templateUrl: './create-service.component.html',
    styleUrls: ['./create-service.component.css']
})
export class CreateServiceDialogComponent implements OnInit, OnDestroy {

    serviceData;

    // github crawler specific data
    githubData;
    picked = 'latest';
    possibleVersions = [];
    selectedVersion;
    latestVersion;


    // manual: 0, github: 1
    selectedTab = 0;

    subModelDefinitions: Subscription;
    filterListManual: string[];
    filterListGithub: string[];

    constructor(
        private apiService: ApiService,
        private util: UtilsService) {
    }

    ngOnInit() {
        this.subModelDefinitions = this.apiService.getModelDefinitions().subscribe(val => {
            this.filterListManual = (val['MicoService'] as ApiModel).required
                .filter((value) => value !== 'serviceInterfaces');
            this.filterListGithub = (val['CrawlingInfoDTO'] as ApiModel).required;
        });
    }

    ngOnDestroy() {
        this.util.safeUnsubscribe(this.subModelDefinitions);

    }

    mapTabIndexToString(index) {
        if (index === 0) {
            return 'manual';
        } else if (index === 1) {
            return 'github';
        } else {
            return 'unknown';
        }
    }

    /**
     * return method of the dialog
     */
    input() {
        // return information based on selected tab
        if (this.selectedTab === 0) {
            // manually filled forms
            return { tab: this.mapTabIndexToString(this.selectedTab), data: this.serviceData };

        } else if (this.selectedTab === 1) {
            // github

            if (this.githubData == null || this.githubData.url == null) {
                // dialog is not finished yet (one of the thousand calls angular performs...)
                return { tab: this.mapTabIndexToString(this.selectedTab), data: undefined };
            }

            if (this.picked === 'latest') {
                // crawl latest version

                return {
                    tab: this.mapTabIndexToString(this.selectedTab),
                    data: { url: this.githubData.url, version: this.latestVersion }
                };
            }

            if (this.picked === 'specific') {
                // crawl a specific version

                return {
                    tab: this.mapTabIndexToString(this.selectedTab),
                    data: { url: this.githubData.url, version: this.selectedVersion }
                };
            }

        }
        // error case
        return { tab: this.mapTabIndexToString(this.selectedTab), data: undefined };

    }

    tabChange(event) {
        this.selectedTab = event.index;
    }

    /**
     * Checks if the stepper went from the first to the second view (github repository -> version selection).
     * If yes, it calls the backend for the repositories versions.
     * Uses: GET services/import/github
     *
     * @param event change event from the material stepper
     */
    gitStepperChange(event) {

        if (event.selectedIndex === 1 && event.previouslySelectedIndex === 0) {

            this.apiService.getServiceVersionsViaGithub(this.githubData.url).subscribe(val => {
                this.possibleVersions = JSON.parse(JSON.stringify(val)).sort((n1, n2) => versionComparator(n1, n2));

                this.latestVersion = this.possibleVersions[this.possibleVersions.length - 1];
                this.selectedVersion = this.latestVersion;
            });

        }
    }

    /**
     * call back from the version selection drop down
     *
     * @param event newly selected version
     */
    updateSelectedVersion(event) {
        this.selectedVersion = event;
    }
}
