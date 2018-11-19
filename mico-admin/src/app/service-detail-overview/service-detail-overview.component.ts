import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router'

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';

@Component({
  selector: 'app-service-detail-overview',
  templateUrl: './service-detail-overview.component.html',
  styleUrls: ['./service-detail-overview.component.css']
})
export class ServiceDetailOverviewComponent implements OnInit {

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
    ) {}

    @Input() service: ApiObject

    ngOnInit() {
        const id = +this.route.snapshot.paramMap.get('id');
        this.apiService.getServiceById(id)
        .subscribe(service => this.service = service);
    }

}
