import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router'

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-service-detail',
    templateUrl: './service-detail.component.html',
    styleUrls: ['./service-detail.component.css']
})
export class ServiceDetailComponent implements OnInit {

    private serviceSubscription: Subscription;
    private paramSubscription: Subscription;

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
    ) { }

    @Input() service: ApiObject

    id: number;

    ngOnInit() {
        this.paramSubscription = this.route.params.subscribe(params => {
            this.update(parseInt(params['id'], 10));
        })
    }

    update(id) {
        if (id == this.id) {
            return
        }

        if (this.serviceSubscription != null){
            this.serviceSubscription.unsubscribe();
        }

        this.serviceSubscription = this.apiService.getServiceById(id)
        .subscribe(service => this.service = service);
    }

}
