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

import { Injectable } from '@angular/core';
import { FormGroup, FormControl, AbstractControl, Validators } from '@angular/forms';

import { ApiModel } from '../api/apimodel';


@Injectable({
    providedIn: 'root'
})
export class FormGroupService {

    constructor() { }

    /**
     * Create a new FormGroup for use with reactive forms.
     *
     * @param model the source ApiModel to create a FormGroup for
     */
    modelToFormGroup(model: ApiModel): FormGroup {
        const group: { [key: string]: AbstractControl } = {};
        if (model.properties != null) {
            for (const key in model.properties) {
                if (!model.properties.hasOwnProperty(key)) {
                    continue;
                }
                const itemModel = model.properties[key];
                let value = null;
                if (itemModel.hasOwnProperty('default')) {
                    value = (itemModel as ApiModel).default;
                }
                const validators = [];
                if (itemModel.hasOwnProperty('x-required')) {
                    validators.push(Validators.required);
                }
                if (itemModel.hasOwnProperty('minLength')) {
                    validators.push(Validators.minLength((itemModel as ApiModel).minLength));
                }
                if (itemModel.hasOwnProperty('maxLength')) {
                    validators.push(Validators.maxLength((itemModel as ApiModel).maxLength));
                }
                if (itemModel.hasOwnProperty('pattern')) {
                    validators.push(Validators.pattern((itemModel as ApiModel).pattern));
                }
                if (itemModel.hasOwnProperty('minimum')) {
                    validators.push(Validators.min((itemModel as ApiModel).minimum));
                }
                if (itemModel.hasOwnProperty('maximum')) {
                    validators.push(Validators.max((itemModel as ApiModel).maximum));
                }
                group[key] = new FormControl(value, validators);
            }
            return new FormGroup(group);
        }
    }
}
