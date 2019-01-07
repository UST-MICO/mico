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

    id: number;

    ngOnInit() {
        this.subParam = this.route.params.subscribe(params => {

            this.shortName = params['shortName'];
            this.update(this.shortName);
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

    update(shortName) {
        if (shortName === this.id) {
            return;
        }

        if (this.subService != null) {
            this.subService.unsubscribe();
        }

        // get latest version
        let version = 'a';
        this.subService = this.apiService.getServiceVersions(shortName)
            .subscribe(serviceVersions => {

                this.versions = serviceVersions;

                serviceVersions.forEach(element => {

                    // TODO implement comparison for semantic versioning
                    if (element.version > version) {
                        version = element.version;
                        this.selectedVersion = element.version;
                        this.service = element;

                    }
                });

            });
    }

}
