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

import { Component, forwardRef, Input, OnInit } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatFormFieldControl } from '@angular/material';
import { ApiModel } from 'src/app/api/apimodel';

@Component({
    selector: 'mico-form-boolean',
    templateUrl: './mico-form-boolean.component.html',
    styleUrls: ['./mico-form-boolean.component.css'],
    providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => MicoFormBooleanComponent),
        multi: true
    }, { provide: MatFormFieldControl, useExisting: Boolean }],
})

export class MicoFormBooleanComponent implements OnInit {

    constructor() { }

    checked: Boolean = false;
    @Input() config: ApiModel;

    onChange: any = () => { };

    onTouched: any = () => { };

    get value(): boolean {
        return !(!this.checked);
    }

    set value(val: boolean) {
        this.checked = val;
        this.onChange(val);
        this.onTouched();
    }

    onValueChange() {
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


    ngOnInit() {
    }

}
