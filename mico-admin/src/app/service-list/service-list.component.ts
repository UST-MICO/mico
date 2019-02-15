import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';
import { from } from 'rxjs';
import { groupBy, mergeMap, toArray, map } from 'rxjs/operators';
import { ApiObject } from '../api/apiobject';


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

    services;

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
        this.subServices = this.apiService.getServices()
            .subscribe(val => {

                from(val as unknown as ArrayLike<ApiObject>)
                    .pipe(
                        groupBy(service => service.shortName),
                        mergeMap(group => group.pipe(toArray())),
                        map(group => group[0]),
                        toArray()
                    ).subscribe(serviceList => {
                        this.services = serviceList;
                    });

            });

    }

}
