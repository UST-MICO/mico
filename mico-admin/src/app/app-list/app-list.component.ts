import { Component, OnInit, Input } from '@angular/core';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { Subscription, from } from 'rxjs';
import { groupBy, mergeMap, toArray, map } from 'rxjs/operators';

@Component({
    selector: 'mico-app-list',
    templateUrl: './app-list.component.html',
    styleUrls: ['./app-list.component.css']
})
export class AppListComponent implements OnInit {

    subApplication: Subscription;

    constructor(
        private apiService: ApiService
    ) {
        this.getApplications();
    }

    applications: Readonly<ApiObject[]>;

    displayedColumns: string[] = ['id', 'name', 'shortName', 'description', 'controls'];

    ngOnInit() {
    }

    getApplications(): void {

        // group applications by shortName
        this.subApplication = this.apiService.getApplications()
            .subscribe(val => {
                from(val as unknown as ArrayLike<ApiObject>)
                    .pipe(
                        groupBy(service => service.shortName),
                        mergeMap(group => group.pipe(toArray())),
                        map(group => group[0]),
                        toArray()
                    ).subscribe(applicationList => {
                        this.applications = applicationList;
                    });

            });
    }

    deleteApplication(application) {
        // TODO delete whole application (all versions and add a dialog before)
        this.apiService.deleteApplication(application.shortName, application.version).subscribe();
    }
}
