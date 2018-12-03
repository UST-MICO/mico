import { Component, OnInit, Inject } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ApiService } from 'src/app/api/api.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';


enum FilterTypes {
    None,
    Internal,
    External,
}

@Component({
    selector: 'mico-service-picker',
    templateUrl: './service-picker.component.html',
    styleUrls: ['./service-picker.component.css']
})

export class ServicePickerComponent implements OnInit {

    service;
    serviceList;
    filter = FilterTypes.None;

    private serviceSubscription: Subscription;

    servicePickerForm = new FormControl();
    picker = new FormControl();
    options: any[] = [];

    constructor(public dialogRef: MatDialogRef<ServicePickerComponent>, @Inject(MAT_DIALOG_DATA) public data: any, private apiService: ApiService) {

        console.log(data.filter);
        if (data.filter === 'internal') {
            this.filter = FilterTypes.Internal;
        } else if (data.filter === 'external') {
            this.filter = FilterTypes.External;
        }

    }

    ngOnInit() {

        // get the list of services
        this.serviceSubscription = this.apiService.getServices()
            .subscribe(services => this.serviceList = services);

        // fill options with the service names
        const tempList: string[] = [];
        this.serviceList.forEach(element => {
            if (this.filter == FilterTypes.None) {
                tempList.push(element);
            } else if (this.filter == FilterTypes.Internal) {
                if (!element.external) {
                    tempList.push(element);
                }
            } else if (this.filter == FilterTypes.External) {
                if (element.external) {
                    tempList.push(element);
                }
            }
        });
        this.options = tempList;
    }

    input() {
        return this.service;
    }

}
