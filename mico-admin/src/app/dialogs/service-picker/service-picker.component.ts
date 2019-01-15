import { Component, OnInit, OnDestroy, Inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { ApiService } from 'src/app/api/api.service';
import { MatDialogRef, MAT_DIALOG_DATA, MatTableDataSource } from '@angular/material';
import { SelectionModel } from '@angular/cdk/collections';


enum FilterTypes {
    None,
    Internal,
    External,
}

enum ChoiceTypes {
    single,
    multi,
}


export interface Service {
    name: string;
    shortName: string;
    description: string;
    id: number;
    version: string;
}

@Component({
    selector: 'mico-service-picker',
    templateUrl: './service-picker.component.html',
    styleUrls: ['./service-picker.component.css']
})

export class ServicePickerComponent implements OnInit, OnDestroy {

    serviceList;
    filter = FilterTypes.None;
    choiceModel = ChoiceTypes.multi;
    existingDependencies: number[] = [];

    private serviceSubscription: Subscription;

    displayedColumns: string[] = ['name', 'shortName', 'version', 'description'];
    dataSource;
    selection;

    // used for highlighting
    selectedRowIndex: number = -1;

    constructor(public dialogRef: MatDialogRef<ServicePickerComponent>, @Inject(MAT_DIALOG_DATA) public data: any,
        private apiService: ApiService) {

        if (data.filter === 'internal') {
            this.filter = FilterTypes.Internal;
        } else if (data.filter === 'external') {
            this.filter = FilterTypes.External;
        }

        if (data.choice === 'single') {
            this.choiceModel = ChoiceTypes.single;
            this.selection = new SelectionModel<Service>(false, []);
        } else if (data.choice === 'multi') {
            this.choiceModel = ChoiceTypes.multi;
            this.displayedColumns = ['select', 'name', 'shortName', 'version', 'description'];
            this.selection = new SelectionModel<Service>(true, []);
        }

        data.existingDependencies.forEach(element => {
            this.existingDependencies.push(parseInt(element.id, 10));
        });
        if (data.serviceId != null && data.serviceId !== '') {
            this.existingDependencies.push(data.serviceId);
        }
    }

    ngOnInit() {

        // get the list of services
        this.serviceSubscription = this.apiService.getServices()
            .subscribe(services => {
                this.serviceList = services;

                // fill options with the service names
                const tempList: Service[] = [];
                this.serviceList.forEach(element => {
                    if (this.filterElement(element)) {
                        tempList.push({
                            name: element.name,
                            shortName: element.shortName,
                            description: element.description,
                            id: element.id,
                            version: element.version,
                        });
                    }
                });

                this.dataSource = new MatTableDataSource(tempList);
            });
    }

    ngOnDestroy() {
        if (this.serviceSubscription != null) {
            this.serviceSubscription.unsubscribe();
        }
    }

    getSelectedService() {
        return this.selection.selected;
    }

    private filterElement = (element): boolean => {

        let val = false;

        if (!this.existingDependencies.includes(parseInt(element.id, 10))) {
            if (this.filter === FilterTypes.None) {
                val = true;
            } else if (this.filter === FilterTypes.Internal) {
                if (!element.external) {
                    val = true;
                }
            } else if (this.filter === FilterTypes.External) {
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

    highlight(row) {
        this.selectedRowIndex = row.id;
    }
}
