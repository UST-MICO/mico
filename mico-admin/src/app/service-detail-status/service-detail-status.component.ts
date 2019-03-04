import { Component, OnChanges, Input, OnDestroy } from '@angular/core';
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';
import { UtilsService } from '../util/utils.service';

@Component({
    selector: 'mico-service-detail-status',
    templateUrl: './service-detail-status.component.html',
    styleUrls: ['./service-detail-status.component.css']
})
export class ServiceDetailStatusComponent implements OnChanges, OnDestroy {

    @Input() shortName;
    @Input() version;

    subServiceStatus: Subscription;

    serviceStatus;
    blackList = ['shortName', 'version', 'name'];

    constructor(
        private apiService: ApiService,
        private util: UtilsService,
    ) { }

    ngOnChanges() {
        if (this.shortName == null || this.version == null) {
            return;
        }

        // get and set serviceStatus
        this.apiService.getServiceStatus(this.shortName, this.version).subscribe(val => {
            this.serviceStatus = JSON.parse(JSON.stringify(val));
        });
    }

    ngOnDestroy() {
        // unsubscribe from observables
        this.util.safeUnsubscribe(this.subServiceStatus);
    }

}
