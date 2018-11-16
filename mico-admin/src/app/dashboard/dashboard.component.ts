import { Component, OnInit, Input } from '@angular/core';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

    constructor(
        private apiService: ApiService
    ) {
        this.getApplications();
    }

    @Input() applications: ApiObject[]

    displayedColumns: string[] = ['id', 'name', 'shortName'];

    ngOnInit() {    }

    getApplications(): void {
        this.apiService.getApplications()
        .subscribe(applications => this.applications = applications);
    }



}
