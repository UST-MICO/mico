import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'mico-app-detail-overview-deployment-information',
    templateUrl: './app-detail-overview-deployment-information.component.html',
    styleUrls: ['./app-detail-overview-deployment-information.component.css']
})
export class AppDetailOverviewDeploymentInformationComponent implements OnInit {

    constructor() { }

    @Input() service;
    @Input() applicationShortName: string;
    @Input() applicationVersion: string;

    ngOnInit() {
    }

}
