import { Component, Input, OnChanges, OnDestroy } from '@angular/core';
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';

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
        this.unsubscribe(this.subApplicationStatus);
        this.subApplicationStatus = this.apiService.getApplicationStatus(this.shortName, this.version)
            .subscribe(val => {
                this.applicationStatus = val;
            });

    }

    ngOnDestroy() {
        // unsubscribe from observables
        this.unsubscribe(this.subApplicationStatus);
    }

    /**
     * generic unsubscribe routine wiht null check
     */
    unsubscribe(subscription: Subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

}
