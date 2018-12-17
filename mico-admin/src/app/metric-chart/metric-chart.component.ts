import { Component, OnInit, Input, AfterViewInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../api/api.service';

import * as c3 from 'c3';

@Component({
    selector: 'mico-metric-chart',
    templateUrl: './metric-chart.component.html',
    styleUrls: ['./metric-chart.component.css']
})
export class MetricChartComponent implements OnInit, AfterViewInit {
    application: any;
    chart: any;
    min;
    max;
    lable: string;
    @Input() data: any;
    dataLabels: string[];
    constructor(private apiService: ApiService, private route: ActivatedRoute) {}

    ngOnInit() {
        const id = +this.route.snapshot.paramMap.get('id');
        this.apiService.getApplicationById(id).subscribe(app => (this.application = app));
    }
    ngAfterViewInit() {
        this.chart = c3.generate({
            bindto: '#chart',
            data: {
                columns: [['data1', 30, 200, 100, 400, 150, 250], ['data2', 50, 20, 10, 40, 15, 25]]
            }
        });
    }
    changeLabelCheckbox(event, label) {
        console.log(event, label);
        if (event.checked) {
            this.showData(label);
        } else {
            this.hideData(label);
        }
    }
    showData(label) {
        this.chart.show(label);
    }
    hideData(label) {
        this.chart.hide(label);
    }
    minmax() {
        const data = [1, 2, 3, 4, 5, 6, 7, 8, 9];
        this.prepareData(this.min, this.max, data);
    }
    dataChange() {
        // this.chart.unload({ ids: this.dataLabels });
        const nrOfDataLines = this.randomIntFromInterval(2, 5);
        const dataList = [];
        const x = [
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
        for (let i = 0; i < nrOfDataLines; i++) {
            const dataPoints = [];
            dataPoints.push('data' + i);
            const nrOfDataPoints = this.randomIntFromInterval(10, 20);
            for (let j = 0; j < nrOfDataPoints; j++) {
                dataPoints.push(this.randomIntFromInterval(0, 100));
            }
            dataList.push(dataPoints);
        }
        this.chart = c3.generate({
            data: {
                x: 'x',
                columns: [x, ...dataList]
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
        /*this.chart.load({
            x: 'x',
            columns: [x, ...dataList],
            axis: {
                x: {
                    type: 'timeseries',
                    tick: {
                        format: '%Y-%m-%d'
                    }
                }
            },
            unload: this.dataLabels
        });*/
        this.dataLabels = this.getDataLablesFromDataList(dataList);
    }
    selectedLabels(names: string[]) {
        this.chart.hide({ ids: names });
    }
    getDataLablesFromDataList(data: any[]) {
        return data.map(d => d[0]);
    }
    prepareData(min, max, data: any[]) {
        console.log(typeof min);
        const slice = data.slice(min, max);
        console.log(slice);
        this.setData([this.lable, ...slice]);
    }
    setData(data) {
        this.chart.load({ columns: [data] });
    }
    /*
     *  min and max included
     */
    randomIntFromInterval(min: number, max: number) {
        return Math.floor(Math.random() * (max - min + 1) + min);
    }
}
