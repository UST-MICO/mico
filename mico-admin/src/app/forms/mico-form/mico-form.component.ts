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
import { FormGroup } from '@angular/forms';
import { FormGroupService } from '../form-group.service';
import { map, first } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { safeUnsubscribe } from 'src/app/util/utils';

/**
 * Dynamic form component that renders a form from the given modelUrl.
 */
@Component({
    selector: 'mico-form',
    templateUrl: './mico-form.component.html',
    styleUrls: ['./mico-form.component.css']
})
export class MicoFormComponent implements OnInit, OnChanges {

    @Input() modelUrl: string;
    @Input() filter: string[] = [];
    @Input() isBlacklist: boolean = false;
    @Input() debug: boolean = false;
    @Input() set startData(data: { [prop: string]: any }) {
        this._startData = data;
        if (this.form != null) {
            this.form.patchValue(data);
        }
    }

    @Output() valid: EventEmitter<boolean> = new EventEmitter<boolean>();
    @Output() data: EventEmitter<any> = new EventEmitter<any>();

    model: ApiModel;
    properties: (ApiModel | ApiModelRef)[];
    form: FormGroup;

    private _startData: { [prop: string]: any };

    private formSubscription: Subscription;

    constructor(private models: ModelsService,
        private formGroup: FormGroupService,
    ) { }

    ngOnInit() { }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.modelUrl != null || changes.filter != null || changes.isBlacklist != null) {

            this.models.getModel(this.modelUrl).pipe(
                map(this.models.filterModel(this.filter, this.isBlacklist)),
                first(),
            ).subscribe(model => {
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
                this.properties = props.sort((a, b) => {
                    const orderA = a['x-order'] != null ? a['x-order'] : 0;
                    const orderB = b['x-order'] != null ? b['x-order'] : 0;
                    return orderA - orderB;
                });
                this.form = this.formGroup.modelToFormGroup(model);

                safeUnsubscribe(this.formSubscription);

                if (this._startData != null) {
                    this.form.patchValue(this._startData);
                }
                this.formSubscription = this.form.statusChanges.subscribe(status => {
                    this.valid.emit(this.form.valid);
                    this.data.emit(this.form.value);
                });
                this.valid.emit(this.form.valid);
                this.data.emit(this.form.value);
            });
        }
    }

    trackByFn(index, item) {
        return item['x-key'];
    }

}
