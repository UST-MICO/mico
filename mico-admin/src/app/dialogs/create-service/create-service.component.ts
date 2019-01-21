import { Component, OnInit, OnDestroy } from '@angular/core';
import { ApiService } from 'src/app/api/api.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'mico-create-service',
    templateUrl: './create-service.component.html',
    styleUrls: ['./create-service.component.css']
})
export class CreateServiceDialogComponent implements OnInit, OnDestroy {

    serviceData;

    subModelDefinitions: Subscription;
    filterList: [string];

    constructor(private apiService: ApiService) {
        this.subModelDefinitions = this.apiService.getModelDefinitions().subscribe(val => {
            this.filterList = val['Service'].required;
        });
    }

    ngOnInit() {
    }

    ngOnDestroy() {
        if (this.subModelDefinitions != null) {
            this.subModelDefinitions.unsubscribe();
        }
    }

    input() {
        return this.serviceData;
    }

}
