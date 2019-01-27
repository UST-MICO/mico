import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';
import { ApiObject } from '../api/apiobject';

export interface Service extends ApiObject {
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
        private router: Router,
    ) { }

    service: Service;
    shortName: string;
    selectedVersion;
    versions: any = [];

    ngOnInit() {
        this.subParam = this.route.params.subscribe(params => {

            this.shortName = params['shortName'];
            const version = params['version'];
            console.log(this.shortName, version);
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
            // no version change
            return;
        }

        if (this.subService != null) {
            this.subService.unsubscribe();
        }

        // get latest version
        this.subService = this.apiService.getServiceVersions(shortName)
            .subscribe(serviceVersions => {

                this.versions = serviceVersions;

                if (givenVersion == null) {
                    this.setLatestVersion(serviceVersions);
                } else {
                    let found = false;
                    found = serviceVersions.some(element => {

                        if (element.version === givenVersion) {
                            this.selectedVersion = givenVersion;
                            this.service = element as Service;
                            this.updateVersion(givenVersion);
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
                this.service = element;

            } else {
                console.log(false);
            }
            this.updateVersion(version);
        });
    }


    /**
     * call-back from the version picker
     */
    updateVersion(version) {
        this.selectedVersion = version;
        this.router.navigate(['service-detail', this.shortName, version]);
    }

}
