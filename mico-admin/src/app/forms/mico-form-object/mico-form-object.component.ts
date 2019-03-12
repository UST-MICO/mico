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

import { Component, forwardRef, OnInit, Input, AfterViewInit, ViewChild } from '@angular/core';
import { MatFormFieldControl } from '@angular/material';
import { NG_VALUE_ACCESSOR, AsyncValidator, NG_VALIDATORS } from '@angular/forms';
import { ApiModel } from 'src/app/api/apimodel';
import { ModelsService } from 'src/app/api/models.service';
import { MicoFormComponent } from '../mico-form/mico-form.component';
import { combineLatest, Observable, BehaviorSubject, Subject, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';


@Component({
    selector: 'mico-form-object',
    templateUrl: './mico-form-object.component.html',
    styleUrls: ['./mico-form-object.component.css'],
    providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => MicoFormObjectComponent),
        multi: true
    }, { provide: NG_VALIDATORS, useExisting: forwardRef(() => MicoFormObjectComponent), multi: true }
        , { provide: MatFormFieldControl, useExisting: Boolean }],
})
export class MicoFormObjectComponent implements OnInit, AfterViewInit, AsyncValidator {

    constructor(private models: ModelsService) { }

    @ViewChild(MicoFormComponent) micoForm: MicoFormComponent;

    lastValidSub: Subscription;
    valid: Subject<boolean> = new BehaviorSubject<boolean>(false);
    currentValue: any = {};

    @Input() config: ApiModel;
    nestedModel: ApiModel;

    onChange: any = () => { };

    onTouched: any = () => { };

    validator: any = () => { };

    get value(): any {
        return this.currentValue;
    }

    set value(val: any) {
        this.currentValue = val;
        this.onChange(val);
        this.onTouched();
    }

    updateData(element) {
        this.currentValue = element;
        this.onChange(this.value);
        this.onTouched();
    }

    registerOnChange(fn) {
        this.onChange = fn;
    }

    registerOnTouched(fn) {
        this.onTouched = fn;
    }

    writeValue(value) {
        if (value) {
            this.value = value;
        }
    }

    validate() {
        return this.valid.pipe(
            map((valid) => {
                if (valid) {
                    return null;
                } else {
                    return { nestedError: 'A nested form has an error.' };
                }
            }),
        );
    }


    ngOnInit() {
        const modelUrl = this.config.$ref;
        this.models.getModel(modelUrl).subscribe(model => {
            this.nestedModel = model;
        });
    }

    ngAfterViewInit() {
        this.micoForm.valid.subscribe((valid) => this.valid.next(valid));
    }

}
