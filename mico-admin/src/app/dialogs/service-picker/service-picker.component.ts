import { Component, OnInit, Inject } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { ApiService } from 'src/app/api/api.service';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';


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
    options: string[] = [];
    filteredOptions: Observable<string[]>;

    constructor(public dialogRef: MatDialogRef<ServicePickerComponent>, @Inject(MAT_DIALOG_DATA) public data: any, private apiService: ApiService) {

        console.log(data.filter);
        if (data.filter === 'internal') {
            this.filter = FilterTypes.Internal;
        } else if (data.filter === 'external') {
            this.filter = FilterTypes.External;
        }

    }

    ngOnInit() {

        this.filteredOptions = this.picker.valueChanges
            .pipe(
                startWith(''),
                map(value => this._filter(value))
            );

        // get the list of services
        this.serviceSubscription = this.apiService.getServices()
            .subscribe(services => this.serviceList = services);


        // fill options with the service names
        const tempList: string[] = [];
        this.serviceList.forEach(element => {
            if (this.filter == FilterTypes.None) {
                tempList.push(element.name);
            } else if (this.filter == FilterTypes.Internal) {
                if (!element.external) {
                    tempList.push(element.name);
                }
            } else if (this.filter == FilterTypes.External) {
                if (element.external) {
                    tempList.push(element.name);
                }
            }
        });
        this.options = tempList;
    }

    private _filter(value: string): string[] {
        const filterValue = value.toLowerCase();

        return this.options.filter(option => option.toLowerCase().includes(filterValue));
    }

    input() {
        return this.service;
    }

}
