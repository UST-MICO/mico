import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { Subscription } from 'rxjs';

@Component({
    selector: 'mico-service-detail-overview',
    templateUrl: './service-detail-overview.component.html',
    styleUrls: ['./service-detail-overview.component.css']
})
export class ServiceDetailOverviewComponent implements OnInit {

    private serviceSubscription: Subscription;
    private paramSubscription: Subscription;

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
    ) { }

    @Input() service: ApiObject;
    @Input() internalDependencies = [];
    @Input() externalDependencies = [];

    // will be used by the update form
    serviceData;

    edit: Boolean = false;
    id: number;

    ngOnInit() {

        this.paramSubscription = this.route.params.subscribe(params => {
            this.update(parseInt(params['id'], 10));
        });

    }

    update(id) {
        if (id === this.id) {
            return;
        } else {
            if (this.serviceSubscription != null) {
                this.serviceSubscription.unsubscribe();
            }
        }

        this.serviceSubscription = this.serviceSubscription = this.apiService.getServiceById(id)
            .subscribe(service => this.service = service);

        // get dependencies and their status
        // TODO service is changed in the following lines... why??
        const internal = [];
        this.service.internalDependencies.forEach(element => {
            internal.push(this.getServiceMetaData(element));
        });
        this.internalDependencies = internal;

        const external = [];
        this.service.externalDependencies.forEach(element => {
            external.push(this.getServiceMetaData(element));
        });
        this.externalDependencies = external;
    }

    editOrSave() {
        console.log('edit or save');
        if (this.edit) {
            // save content
        }
        this.edit = !this.edit;
    }

    getServiceMetaData(id) {
        let service_object;
        this.apiService.getServiceById(id).subscribe(val => service_object = val);
        const return_object = {
            'id': id,
            'name': service_object.name,
            'status': service_object.status,
        };
        return return_object;
    }

}
