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

import { Component, OnInit, Input, OnChanges, SimpleChanges, EventEmitter, Output } from '@angular/core';
import { ModelsService } from '../../api/models.service';
import { ApiModel, ApiModelRef } from '../../api/apimodel';
import { map, first } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { UtilsService } from 'src/app/util/utils.service';

@Component({
    selector: 'mico-data',
    templateUrl: './mico-data.component.html',
    styleUrls: ['./mico-data.component.css']
})
export class MicoDataComponent implements OnInit, OnChanges {

    @Input() modelUrl: string;
    @Input() filter: string[] = [];
    @Input() isBlacklist: boolean = false;
    @Input() startData: { [prop: string]: any };


    model: ApiModel;
    properties: (ApiModel | ApiModelRef)[];

    private formSubscription: Subscription;

    constructor(
        private models: ModelsService,
        private util: UtilsService,
    ) { }

    ngOnInit() { }

    ngOnChanges(changes: SimpleChanges) {
        // check for relevant changes
        if (changes.modelUrl != null || changes.filter != null || changes.isBlacklist != null) {
            this.models.getModel(this.modelUrl).pipe(
                map(this.models.filterModel(this.filter, this.isBlacklist)),
                first(),
            ).subscribe(model => {

                // handling of custom properties
                const props = [];

                if (model.properties != null) {
                    for (const key in model.properties) {
                        if (!model.properties.hasOwnProperty(key)) {
                            continue;
                        }
                        props.push(model.properties[key]);
                    }
                }
                this.model = model;

                // sort elements by their x-order
                this.properties = props.sort((a, b) => {
                    const orderA = a['x-order'] != null ? a['x-order'] : 0;
                    const orderB = b['x-order'] != null ? b['x-order'] : 0;
                    return orderA - orderB;
                });

                this.util.safeUnsubscribe(this.formSubscription);
            });
        }
    }

    trackByFn(index, item) {
        return item['x-key'];
    }

}
