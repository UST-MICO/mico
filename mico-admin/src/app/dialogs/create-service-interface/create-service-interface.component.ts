import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'mico-create-service-interface',
    templateUrl: './create-service-interface.component.html',
    styleUrls: ['./create-service-interface.component.css']
})
export class CreateServiceInterfaceComponent implements OnInit {

    constructor() { }

    // form elements are stored in here
    serviceData;
    portData;

    ngOnInit() {
    }

    confirmButton() {
        const tempReturn = this.serviceData;
        if (tempReturn != null) {
            tempReturn.ports = this.portData;
        }
        return tempReturn;
    }

}
