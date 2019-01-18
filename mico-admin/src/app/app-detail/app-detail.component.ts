import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';

@Component({
    selector: 'mico-app-detail',
    templateUrl: './app-detail.component.html',
    styleUrls: ['./app-detail.component.css']
})
export class AppDetailComponent implements OnInit {

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
    ) { }

    application: ApiObject;
    selectedVersion;

    ngOnInit() {
        const id = +this.route.snapshot.paramMap.get('id');
        // getServiceVersions works also for applications
        this.apiService.getServiceVersions(id)
            .subscribe(val => {
                this.setLatestVersion(val);
            });
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
