import { Component, OnInit, Input, OnDestroy } from '@angular/core';
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'mico-service-detail-status',
    templateUrl: './service-detail-status.component.html',
    styleUrls: ['./service-detail-status.component.css']
})
export class ServiceDetailStatusComponent implements OnInit, OnDestroy {

    @Input() shortName;
    @Input() version;

    subServiceStatus: Subscription;

    serviceStatus;

    constructor(
        private apiService: ApiService
    ) { }

    ngOnInit() {
        if (this.shortName == null || this.version == null) {
            return;
        }

        this.apiService.getServiceStatus(this.shortName, this.version).subscribe(val => {
            this.serviceStatus = JSON.parse(JSON.stringify(val));
        });
    }

    ngOnDestroy() {
        if (this.subServiceStatus != null) {
            this.subServiceStatus.unsubscribe();
        }
    }

}
