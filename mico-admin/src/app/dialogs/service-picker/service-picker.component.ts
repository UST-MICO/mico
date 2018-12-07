import { Component, OnInit, Inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { ApiService } from 'src/app/api/api.service';
import { MatDialogRef, MAT_DIALOG_DATA, MatTableDataSource } from '@angular/material';
import { SelectionModel } from '@angular/cdk/collections';


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
}

@Component({
    selector: 'mico-service-picker',
    templateUrl: './service-picker.component.html',
    styleUrls: ['./service-picker.component.css']
})

export class ServicePickerComponent implements OnInit {

    serviceList;
    filter = FilterTypes.None;
    exisitingDependencies: number[] = [];

    private serviceSubscription: Subscription;

    displayedColumns: string[] = ['select', 'id', 'name', 'shortName', 'description'];
    dataSource;
    selection = new SelectionModel<Service>(true, []);

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
                });
            }

        });
        this.dataSource = new MatTableDataSource(tempList);
    }

    getSelectedService() {
        return this.selection.selected;
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

    applyFilter(filterValue: string) {
        this.dataSource.filter = filterValue.trim().toLowerCase();
    }

    isAllSelected() {
        const numSelected = this.selection.selected.length;
        const numRows = this.dataSource.data.length;
        return numSelected === numRows;
    }

    /** Selects all rows if they are not all selected; otherwise clear selection. */
    masterToggle() {
        this.isAllSelected() ?
            this.selection.clear() :
            this.dataSource.data.forEach(row => this.selection.select(row));
    }
}
