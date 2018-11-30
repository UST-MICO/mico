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

    nodes = [{id: 1, title: 'A', x: 1, y: 1}, {id: 2, title: 'B', x: 10, y:10}];
    edges = [{source: 1, target: 2}];

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
