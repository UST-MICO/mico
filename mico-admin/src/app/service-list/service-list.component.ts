import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { Subscription } from 'rxjs';
import { from } from 'rxjs';
import { groupBy, mergeMap, toArray } from 'rxjs/operators';

@Component({
    selector: 'mico-service-list',
    templateUrl: './service-list.component.html',
    styleUrls: ['./service-list.component.css']
})
export class ServiceListComponent implements OnInit, OnDestroy {

    private subServices: Subscription;

    constructor(
        private apiService: ApiService
    ) {
        this.getServices();
    }

    @Input() services: ApiObject[];

    displayedColumns: string[] = ['id', 'name', 'shortName', 'description'];

    ngOnInit() {
    }

    ngOnDestroy() {
        if (this.subServices != null) {
            this.subServices.unsubscribe();
        }
    }

    getServices(): void {

        // group services by shortName
        const tempServices: ApiObject[] = [];
        this.subServices = this.apiService.getServices()
            .subscribe(val => {
                from(val).pipe(groupBy(service => service.shortName), mergeMap(group => group.pipe(toArray())))
                    .subscribe(group => {
                        tempServices.push(group[0]);
                    });
                this.services = tempServices;
            });

    }

}
