import { Component, OnInit, Inject } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Subscription, Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { ApiService } from 'src/app/api/api.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';


enum FilterTypes {
    None,
    Internal,
    External,
}


export interface Service {
    name: string;
    shortName: string;
    description: string;
    id: number;
    filterName: string;
}

@Component({
    selector: 'mico-service-picker',
    templateUrl: './service-picker.component.html',
    styleUrls: ['./service-picker.component.css']
})

export class ServicePickerComponent implements OnInit {

    choosenService: Service;
    serviceList;
    filter = FilterTypes.None;
    exisitingDependencies: number[] = [];

    private serviceSubscription: Subscription;

    picker = new FormControl();

    options: Service[];
    filteredOptions: Observable<Service[]>;

    constructor(public dialogRef: MatDialogRef<ServicePickerComponent>, @Inject(MAT_DIALOG_DATA) public data: any, private apiService: ApiService) {

        if (data.filter === 'internal') {
            this.filter = FilterTypes.Internal;
        } else if (data.filter === 'external') {
            this.filter = FilterTypes.External;
        }

        data.exisitingDependencies.forEach(element => {
            this.exisitingDependencies.push(parseInt(element.id, 10));
        });
        this.exisitingDependencies.push(data.serviceId);
    }

    ngOnInit() {

        // get the list of services
        this.serviceSubscription = this.apiService.getServices()
            .subscribe(services => this.serviceList = services);

        // fill options with the service names
        const tempList: Service[] = [];
        this.serviceList.forEach(element => {
            if (this.filterElement(element)) {
                tempList.push({
                    name: element.name,
                    shortName: element.shortName,
                    description: element.description,
                    id: element.id,
                    filterName: element.shortName + ' ' + element.name,
                });
            }

        });
        this.options = tempList;

        this.picker.valueChanges.subscribe(element => {

            // TODO check if a service with shortName 'element' exists. If yes, store in this.chosenService
            console.log(element);
            this.choosenService = element

        })

        this.filteredOptions = this.picker.valueChanges
            .pipe(
                startWith<string | Service>(''),
                map(value => typeof value === 'string' ? value : value.name),
                map(name => name ? this._filter(name) : this.options.slice())
            );

    }

    getSelectedService() {
        return this.choosenService;
    }

    private filterElement = (element): boolean => {

        var val = false;

        if (!this.exisitingDependencies.includes(parseInt(element.id, 10))) {
            if (this.filter == FilterTypes.None) {
                val = true;
            } else if (this.filter == FilterTypes.Internal) {
                if (!element.external) {
                    val = true;
                }
            } else if (this.filter == FilterTypes.External) {
                if (element.external) {
                    val = true;
                }
            } else {
                val = false;
            }
        }
        return val;
    }

    displayFn(service?: Service): string | undefined {
        return service ? service.shortName : undefined;
    }

    private _filter(value: string): Service[] {
        const filterValue = value.toLowerCase();

        return this.options.filter(option => option.filterName.toLowerCase().indexOf(filterValue) >= 0);
    }

}
