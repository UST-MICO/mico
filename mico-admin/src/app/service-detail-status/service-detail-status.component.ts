import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'mico-service-detail-status',
    templateUrl: './service-detail-status.component.html',
    styleUrls: ['./service-detail-status.component.css']
})
export class ServiceDetailStatusComponent implements OnInit {

    @Input() serviceStatus;


    constructor() { }

    ngOnInit() {
    }

}
