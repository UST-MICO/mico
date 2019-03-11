import { Component, Input, OnChanges, OnDestroy } from '@angular/core';
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';
import { UtilsService } from '../util/utils.service';

@Component({
    selector: 'mico-app-detail-status',
    templateUrl: './app-detail-status.component.html',
    styleUrls: ['./app-detail-status.component.css']
})
export class AppDetailStatusComponent implements OnChanges, OnDestroy {

    subApplicationStatus: Subscription;

    constructor(
        private apiService: ApiService,
        private util: UtilsService,
    ) { }

    @Input() shortName;
    @Input() version;

    applicationStatus;

    ngOnChanges() {
        if (this.shortName == null || this.version == null) {
            return;
        }

        // get and set applicationStatus
        this.util.safeUnsubscribe(this.subApplicationStatus);
        this.subApplicationStatus = this.apiService.getApplicationStatus(this.shortName, this.version)
            .subscribe(val => {
                this.applicationStatus = JSON.parse(JSON.stringify(val));
            });

    }

    ngOnDestroy() {
        // unsubscribe from observables
        this.util.safeUnsubscribe(this.subApplicationStatus);
    }
}
