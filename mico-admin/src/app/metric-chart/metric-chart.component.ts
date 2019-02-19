/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { Component, Input, ViewChild } from '@angular/core';

import { ChartComponent } from './chart/chart.component';

@Component({
    selector: 'mico-metric-chart',
    templateUrl: './metric-chart.component.html',
    styleUrls: ['./metric-chart.component.css']
})
export class MetricChartComponent {
    @Input() data: any;
    @Input() dataLabels: string[];
    @Input() xLabels: string[] = [
        'x',
        '2018-11-29',
        '2018-11-30',
        '2018-12-01',
        '2018-12-02',
        '2018-12-03',
        '2018-12-04',
        '2018-12-05',
        '2018-12-06',
        '2018-12-07',
        '2018-12-08',
        '2018-12-09',
        '2018-12-10',
        '2018-12-11',
        '2018-12-12',
        '2018-12-13',
        '2018-12-14',
        '2018-12-15',
        '2018-12-16',
        '2018-12-17'
    ];
    @ViewChild('chart') chart: ChartComponent;
    min: Date;
    max: Date;

    constructor() {}

    changeLabelCheckbox(event: { checked: any }, label: any) {
        if (event.checked) {
            this.chart.showData(label);
        } else {
            this.chart.hideData(label);
        }
    }

    // [TODO] call rest api to get required data
    dataChange() {
        const nrOfDataLines = this.randomIntFromInterval(2, 5);
        const dataList: any[][] = [];

        for (let i = 0; i < nrOfDataLines; i++) {
            const dataPoints: any[] = [];
            dataPoints.push('data' + i);
            const nrOfDataPoints = this.randomIntFromInterval(10, 20);
            for (let j = 0; j < nrOfDataPoints; j++) {
                dataPoints.push(this.randomIntFromInterval(0, 100));
            }
            dataList.push(dataPoints);
        }

        this.data = dataList;
        this.dataLabels = this.getDataLablesFromDataList(dataList);
    }
    getDataLablesFromDataList(data: any[]) {
        return data.map(d => d[0]);
    }
    /*
     *  min and max included
     */
    randomIntFromInterval(min: number, max: number) {
        return Math.floor(Math.random() * (max - min + 1) + min);
    }
}
