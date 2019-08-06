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
