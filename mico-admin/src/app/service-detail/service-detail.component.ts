import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router'

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';

@Component({
  selector: 'app-service-detail',
  templateUrl: './service-detail.component.html',
  styleUrls: ['./service-detail.component.css']
})
export class ServiceDetailComponent implements OnInit {

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
