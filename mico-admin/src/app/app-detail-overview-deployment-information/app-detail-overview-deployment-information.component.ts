import { Component, Input, OnChanges } from '@angular/core';
import { ApiService } from '../api/api.service';

@Component({
    selector: 'mico-app-detail-overview-deployment-information',
    templateUrl: './app-detail-overview-deployment-information.component.html',
    styleUrls: ['./app-detail-overview-deployment-information.component.css']
})
export class AppDetailOverviewDeploymentInformationComponent implements OnChanges {

    constructor(private apiService: ApiService) { }

    @Input() service;
    @Input() applicationShortName: string;
    @Input() applicationVersion: string;

    ngOnChanges() {
        if (this.service != null && this.applicationShortName != null && this.applicationVersion != null) {

            this.apiService.getServiceDeploymentInformation(this.applicationShortName, this.applicationVersion, this.service.shortName)
                .subscribe(val => {
                    console.log(val);
                });
        }
    }

}
