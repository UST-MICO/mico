import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';

@Component({
  selector: 'mico-app-detail',
  templateUrl: './app-detail.component.html',
  styleUrls: ['./app-detail.component.css']
})
export class AppDetailComponent implements OnInit {

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
    ) {}

    @Input() application: ApiObject;

  ngOnInit() {
    const id = +this.route.snapshot.paramMap.get('id');
    this.apiService.getApplicationById(id)
      .subscribe(app => this.application = app);
  }

}
