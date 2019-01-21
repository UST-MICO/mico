import { Component, OnInit, Input } from '@angular/core';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';

@Component({
    selector: 'mico-app-list',
    templateUrl: './app-list.component.html',
    styleUrls: ['./app-list.component.css']
})
export class AppListComponent implements OnInit {

    constructor(
        private apiService: ApiService
    ) {
        this.getApplications();
    }

    applications: Readonly<ApiObject>;

    displayedColumns: string[] = ['id', 'name', 'shortName', 'description'];

    ngOnInit() {
    }

    getApplications(): void {
        this.apiService.getApplications()
            .subscribe(applications => this.applications = applications);
    }
}
