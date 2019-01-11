import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';

export interface Service {
    name: string;
}

@Component({
    selector: 'mico-service-detail',
    templateUrl: './service-detail.component.html',
    styleUrls: ['./service-detail.component.css']
})
export class ServiceDetailComponent implements OnInit, OnDestroy {

    private subService: Subscription;
    private subParam: Subscription;

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
    ) { }

    service: Service;
    shortName: string;
    selectedVersion;
    versions: any = [];

    ngOnInit() {
        this.subParam = this.route.params.subscribe(params => {

            this.shortName = params['shortName'];
            const version = params['version'];
            this.update(this.shortName, version);
        });
    }

    ngOnDestroy() {
        if (this.subService != null) {
            this.subService.unsubscribe();
        }
        if (this.subParam != null) {
            this.subParam.unsubscribe();
        }
    }

    update(shortName, givenVersion) {

        if (this.selectedVersion === givenVersion && givenVersion != null) {
            return;
        }

        if (this.subService != null) {
            this.subService.unsubscribe();
        }

        // get latest version

        // TODO change url path according to the current version

        this.subService = this.apiService.getServiceVersions(shortName)
            .subscribe(serviceVersions => {

                this.versions = serviceVersions;

                if (givenVersion == null) {
                    this.setLatestVersion(serviceVersions);
                } else {
                    let found = false;
                    found = serviceVersions.forEach(element => {

                        if (element.version === givenVersion) {
                            this.selectedVersion = givenVersion;
                            this.service = element;
                            return true;
                        }
                    });

                    if (!found) {
                        // given version was not found in the versions list, take latest instead
                        this.setLatestVersion(serviceVersions);
                    }
                }

            });
    }

    /**
     * takes a list of services and sets this.service to the service with the latest version
     * this.version is set accoringly
     */
    setLatestVersion(list) {

        list.forEach(element => {

            let version = '0';

            // TODO implement comparison for semantic versioning
            if (element.version > version) {
                version = element.version;
                this.selectedVersion = element.version;
                this.service = element;

            } else {
                console.log(false);
            }
        });
    }

}
