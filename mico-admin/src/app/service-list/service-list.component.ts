import { Component, OnInit, Input } from '@angular/core';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';

@Component({
    selector: 'mico-service-list',
    templateUrl: './service-list.component.html',
    styleUrls: ['./service-list.component.css']
})
export class ServiceListComponent implements OnInit {

    constructor(
        private apiService: ApiService
    ) {
        this.getServices();
    }

    @Input() services: ApiObject[];

    displayedColumns: string[] = ['id', 'name', 'shortName', 'description'];

    ngOnInit() {
    }

    getServices(): void {
        this.apiService.getServices()
            .subscribe(val => {
                this.services = val;
                console.log(this.services);
            });
    }
}
