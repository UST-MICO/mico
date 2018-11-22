import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'create-service',
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
