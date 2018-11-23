import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'mico-create-service',
    templateUrl: './create-service.component.html',
    styleUrls: ['./create-service.component.css']
})
export class CreateServiceDialogComponent implements OnInit {

    serviceData;

    constructor() { }

    ngOnInit() {
    }

    input() {
        return this.serviceData;
    }

}
