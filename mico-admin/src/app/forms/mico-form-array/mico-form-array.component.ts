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

import { Component, forwardRef, OnInit, Input, ViewChildren, AfterViewInit } from '@angular/core';
import { MatFormFieldControl } from '@angular/material';
import { NG_VALUE_ACCESSOR, AsyncValidator, NG_ASYNC_VALIDATORS } from '@angular/forms';
import { ApiModel } from 'src/app/api/apimodel';
import { ModelsService } from 'src/app/api/models.service';
import { MicoFormComponent } from '../mico-form/mico-form.component';
import { combineLatest, Observable, BehaviorSubject, Subject, Subscription } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { safeUnsubscribe } from 'src/app/util/utils';


@Component({
    selector: 'mico-form-array',
    templateUrl: './mico-form-array.component.html',
    styleUrls: ['./mico-form-array.component.css'],
    providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => MicoFormArrayComponent),
        multi: true
    }, { provide: NG_ASYNC_VALIDATORS, useExisting: forwardRef(() => MicoFormArrayComponent), multi: true }
        , { provide: MatFormFieldControl, useExisting: Boolean }],
})
export class MicoFormArrayComponent implements OnInit, AfterViewInit, AsyncValidator {


    constructor(private models: ModelsService) { }

    @ViewChildren(MicoFormComponent) forms;

    lastValidSub: Subscription;
    valid: Subject<boolean> = new BehaviorSubject<boolean>(false);
    currentValue: any[] = [];
    minLimitText;
    maxLimitText;

    @Input() config: ApiModel;

    onChange: any = () => { };

    onTouched: any = () => { };

    validator: any = () => { };

    get value(): any[] {
        return this.currentValue;
    }

    set value(val: any[]) {
        this.currentValue = val;
        this.onChange(val);
        this.onTouched();
    }

    trackBy(index) {
        return index;
    }

    updateElement(index, element) {
        if (index >= this.currentValue.length || index < 0) {
            return;
        }
        this.currentValue[index] = element;
        this.onChange(this.value);
        this.onTouched();
    }

    removeElement(index) {
        if (index >= this.currentValue.length || index < 0) {
            return;
        }
        this.currentValue.splice(index, 1);
        if (this.currentValue.length === 0) {
            // ensure validation happens if last element was removed
            this.valid.next(true);
        }
        this.onChange(this.value);
        this.onTouched();
    }

    addElement() {
        this.currentValue.push({});
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
                const error: any = {};
                if (this.config != null && this.config.minItems && this.value.length < this.config.minItems) {
                    error.length = 'Too few items in array!';
                }
                if (this.config != null && this.config.maxItems && this.value.length > this.config.maxItems) {
                    error.length = 'Too many items in array!';
                }
                if (!valid) {
                    error.nestedError = 'A nested form has an error.';
                }
                if (Object.keys(error).length === 0) {
                    return null;
                }
                return error;
            }),
            take(1),
        );
    }


    ngOnInit() {

        // build strings for min/max limit
        if (this.config.hasOwnProperty('minItems')) {
            this.minLimitText = this.config.minItems;
        } else {
            this.minLimitText = '0';
        }

        if (this.config.hasOwnProperty('maxItems')) {
            this.maxLimitText = this.config.maxItems;
        } else {
            this.maxLimitText = '\u221E';
        }
    }

    ngAfterViewInit() {
        if (this.forms._results != null && this.forms._results.length > 0) {
            this.updateFormValidators(this.forms);
        }
        this.forms.changes.subscribe(this.updateFormValidators);
    }

    updateFormValidators = (forms) => {
        const micoForms: MicoFormComponent[] = forms._results;
        const validObservables: Observable<boolean>[] = [];
        micoForms.forEach((form) => {
            validObservables.push(form.valid.asObservable());
        });
        if (this.lastValidSub != null) {
            safeUnsubscribe(this.lastValidSub);
        }
        if (validObservables.length > 0) {
            this.lastValidSub = combineLatest(...validObservables).pipe(
                map((values) => {
                    return !values.some(value => !value);
                }),
            ).subscribe((valid) => {
                this.valid.next(valid);
            });
        } else {
            // also emit valid values for empty arrays
            this.valid.next(true);
        }
    }

}
