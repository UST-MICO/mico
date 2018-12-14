import { Component, OnInit, Input } from '@angular/core';

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';

@Component({
    selector: 'mico-app-detail-overview',
    templateUrl: './app-detail-overview.component.html',
    styleUrls: ['./app-detail-overview.component.css']
})
export class AppDetailOverviewComponent implements OnInit {

    @Input() application: ApiObject;

    nodes = [{ id: 1, title: 'A', x: 1, y: 1 }, { id: 2, title: 'B', x: 10, y: 10 }];
    edges = [{ source: 1, target: 2 }];

    constructor(private apiService: ApiService) { }

    ngOnInit() {
    }

}
