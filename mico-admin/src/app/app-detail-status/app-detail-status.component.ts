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
