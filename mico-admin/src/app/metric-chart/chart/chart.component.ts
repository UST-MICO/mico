import { Component, OnInit, Input, OnChanges } from '@angular/core';

import * as c3 from 'c3';

@Component({
    selector: 'mico-chart',
    templateUrl: './chart.component.html',
    styleUrls: ['./chart.component.css']
})
export class ChartComponent implements OnInit, OnChanges {
    chart: any;
    @Input() data: any;
    @Input() x: any;
    constructor() {}

    ngOnInit() {
        console.log('loaded');
    }
    ngOnChanges() {
        this.chart = c3.generate({
            data: {
                x: 'x',
                columns: [this.x, ...this.data]
            },
            axis: {
                x: {
                    type: 'timeseries',
                    tick: {
                        format: '%y-%m-%d'
                    }
                }
            }
        });
    }
    showData(label: any) {
        this.chart.show(label);
    }
    hideData(label: any) {
        this.chart.hide(label);
    }
}
