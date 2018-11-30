import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { ApiService } from 'src/app/api/api.service';

@Component({
    selector: 'mico-service-picker',
    templateUrl: './service-picker.component.html',
    styleUrls: ['./service-picker.component.css']
})
export class ServicePickerComponent implements OnInit {

    service;
    serviceList;

    private serviceSubscription: Subscription;

    constructor(private apiService: ApiService) { }

    servicePickerForm = new FormControl();
    picker = new FormControl();
    options: string[] = [];
    filteredOptions: Observable<string[]>;

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
            // TODO is there a better way to get a boolean from the JSON?
            if ("false" === element.external) {
                tempList.push(element.name);
            }
        });
        this.options = tempList;
    }

    private _filter(value: string): string[] {
        const filterValue = value.toLowerCase();

        return this.options.filter(option => option.toLowerCase().includes(filterValue));
    }

    input() {
        //TODO add the service to the dependencies
        return this.service;
    }

}
