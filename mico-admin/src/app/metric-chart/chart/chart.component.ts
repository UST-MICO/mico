import { Component, Input, OnChanges } from '@angular/core';

import * as c3 from 'c3';

@Component({
    selector: 'mico-chart',
    templateUrl: './chart.component.html',
    styleUrls: ['./chart.component.css']
})
export class ChartComponent implements OnChanges {
    @Input() data: (string | number | boolean)[][];
    @Input() xLabels: string[];
    @Input() format: string;
    chart: c3.ChartAPI;

    constructor() {}

    ngOnChanges() {
        this.chart = c3.generate({
            data: {
                x: 'x',
                columns: [this.xLabels, ...this.data]
            },
            axis: {
                x: {
                    type: 'timeseries',
                    tick: {
                        format: this.format
                    }
                }
            }
        });
    }
    showData(label: string) {
        this.chart.show(label);
    }
    hideData(label: string) {
        this.chart.hide(label);
    }
}
