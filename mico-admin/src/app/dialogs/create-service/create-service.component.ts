import { Component, OnInit, OnDestroy } from '@angular/core';
import { ApiService } from 'src/app/api/api.service';
import { Subscription } from 'rxjs';
import { ApiModel } from 'src/app/api/apimodel';

@Component({
    selector: 'mico-create-service',
    templateUrl: './create-service.component.html',
    styleUrls: ['./create-service.component.css']
})
export class CreateServiceDialogComponent implements OnInit, OnDestroy {

    serviceData;
    // TODO link with form as soon as the according endpoint exists.
    githubData;


    // manual: 0, github: 1
    selectedTab = 0;

    subModelDefinitions: Subscription;
    filterList: string[];

    constructor(private apiService: ApiService) {
        this.subModelDefinitions = this.apiService.getModelDefinitions().subscribe(val => {
            this.filterList = (val['MicoService'] as ApiModel).required.filter((val) => val !== 'serviceInterfaces');
        });
    }

    ngOnInit() {
    }

    ngOnDestroy() {
        if (this.subModelDefinitions != null) {
            this.subModelDefinitions.unsubscribe();
        }
    }

    mapTabIndexToString(index) {
        if (index === 0) {
            return 'manual';
        } else if (index === 1) {
            return 'github';
        } else {
            return 'unknown';
        }
    }

    input() {
        // return information based on selected tab
        if (this.selectedTab === 0) {
            // manual
            return { tab: this.mapTabIndexToString(this.selectedTab), data: this.serviceData };
        } else if (this.selectedTab === 1) {
            // github
            return { tab: this.mapTabIndexToString(this.selectedTab), data: this.githubData };
        } else {
            // error case
            return { tab: this.mapTabIndexToString(this.selectedTab), data: undefined };
        }
    }

    tabChange(event) {
        this.selectedTab = event.index;
    }

}
