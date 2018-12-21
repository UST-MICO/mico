import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../api/api.service';

import { ChartComponent } from './chart/chart.component';

@Component({
    selector: 'mico-metric-chart',
    templateUrl: './metric-chart.component.html',
    styleUrls: ['./metric-chart.component.css']
})
export class MetricChartComponent implements OnInit {
    application: any;
    @ViewChild('chart') chart: ChartComponent;
    min: Date;
    max: Date;
    label: string;
    x = [
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
    @Input() data: any;
    @Input() dataLabels: string[];
    constructor(private apiService: ApiService, private route: ActivatedRoute) {}

    ngOnInit() {
        const id = +this.route.snapshot.paramMap.get('id');
        this.apiService.getApplicationById(id).subscribe(app => (this.application = app));
    }
    changeLabelCheckbox(event: { checked: any }, label: any) {
        console.log(event, label);
        if (event.checked) {
            this.chart.showData(label);
        } else {
            this.chart.hideData(label);
        }
    }
    // [TODO] call rest api to get required data
    dataChange() {
        console.log(this.min, this.max);
        console.log('generating data');
        const nrOfDataLines = this.randomIntFromInterval(2, 5);
        const dataList = [];

        for (let i = 0; i < nrOfDataLines; i++) {
            const dataPoints = [];
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
