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

import { Component, OnInit, Input } from '@angular/core';
import { ApiModel } from 'src/app/api/apimodel';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'mico-form-question',
  templateUrl: './mico-form-question.component.html',
  styleUrls: ['./mico-form-question.component.css']
})
export class MicoFormQuestionComponent implements OnInit {

    @Input() property: ApiModel;
    @Input() form: FormGroup;

    get formControl() {
        if (this.form == null || this.property == null) {
            return null;
        }
        return this.form.get(this.property['x-key']);
    }

    constructor() { }

    ngOnInit() {
    }

    /**
     * Decide the type of the property based on type and custom x- attributes.
     */
    propertyType(): string {
        if (this.property != null && this.property.type != null) {
            return this.property.type;
        } else if (this.property != null && this.property.$ref != null) {
            return 'object';
        } else { return 'string'; }
    }

    getErrorMessage() {
        if (this.formControl.hasError('required')) {
            return 'You must enter a value';
        }
        if (this.formControl.hasError('minlength')) {
            return 'Input is too short.';
        }
        if (this.formControl.hasError('maxlength')) {
            return 'Input is too long.';
        }
        if (this.formControl.hasError('pattern')) {
            return 'Invalid input.';
        }
        return 'HI';
    }

}
