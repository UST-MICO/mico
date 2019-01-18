import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { Subscription } from 'rxjs';

@Component({
    selector: 'mico-app-detail',
    templateUrl: './app-detail.component.html',
    styleUrls: ['./app-detail.component.css']
})
export class AppDetailComponent implements OnInit, OnDestroy {

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
    ) { }

    subRouteParams: Subscription;
    subApplicationVersions: Subscription;

    application: ApiObject;
    selectedVersion;

    ngOnInit() {

        this.subRouteParams = this.route.params.subscribe(params => {
            const shortName = params['shortName'];
            const givenVersion = params['version'];

            // getServiceVersions works also for applications
            this.subApplicationVersions = this.apiService.getServiceVersions(shortName)
                .subscribe(versions => {
                    if (givenVersion == null) {
                        this.setLatestVersion(versions);
                    } else {
                        let found = false;
                        found = versions.forEach(element => {

                            if (element.version === givenVersion) {
                                this.selectedVersion = givenVersion;
                                this.apiService = element;
                                return true;
                            }
                        });
                        if (!found) {
                            // given version was not found in the versions list, take latest instead
                            this.setLatestVersion(versions);
                        }
                    }
                });
        });

    }

    ngOnDestroy() {
        this.unsubscribe(this.subRouteParams);
        this.unsubscribe(this.subApplicationVersions);
    }

    unsubscribe(subscription: Subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    /**
     * takes a list of applications and sets this.application to the application with the latest version
     * this.version is set accoringly
     */
    setLatestVersion(list) {
        list.forEach(element => {

            let version = '0';

            // TODO implement comparison for semantic versioning
            if (element.version > version) {
                version = element.version;
                this.selectedVersion = element.version;
                this.application = element;

            }
        });
    }
}
