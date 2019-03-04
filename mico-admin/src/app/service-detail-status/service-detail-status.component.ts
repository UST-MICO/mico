import { Component, Input, OnDestroy, OnChanges } from '@angular/core';
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';

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

    constructor(
        private apiService: ApiService
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
        if (this.subServiceStatus != null) {
            this.subServiceStatus.unsubscribe();
        }
    }

}
