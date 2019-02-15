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
    portData: any[];

    ngOnInit() {
    }

    confirmButton() {
        if (this.serviceData == null) {
            return null;
        }
        console.log(this.portData);
        if (this.portData == null || this.portData.length <= 0) {
            return null;
        }
        const tempReturn = this.serviceData;
        tempReturn.ports = this.portData;
        return tempReturn;
    }

}
