import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { Subscription } from 'rxjs';

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

    @Input() service: ApiObject;

    id: number;

    ngOnInit() {
        this.subParam = this.route.params.subscribe(params => {

            // TODO consider moving the code for the latest version from detail-overview to here and passing the data into detail-overview.
            this.update(params['shortName']);
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

        this.subService = this.apiService.getService(shortName)
            .subscribe(service => this.service = service);
    }

}
