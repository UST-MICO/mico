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
        if (this.version != null) {

            if (this.subApplicationStatus != null) {
                this.unsubscribe(this.subApplicationStatus);
            }

            this.subApplicationStatus = this.apiService.getApplicationStatus(this.shortName, this.version)
                .subscribe(val => {
                    this.applicationStatus = val;
                    console.log(val);
                });
        }
    }

    ngOnDestroy() {
        this.unsubscribe(this.subApplicationStatus);
    }

    unsubscribe(subscription: Subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

}
