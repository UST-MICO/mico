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
import { ApiModel } from 'src/app/api/apimodel';

@Component({
    selector: 'mico-data-container',
    templateUrl: './mico-data-container.component.html',
    styleUrls: ['./mico-data-container.component.css']
})
export class MicoDataContainerComponent implements OnChanges {

    @Input() property: ApiModel;
    @Input() data: any;

    dataValue: any;

    constructor() { }

    ngOnChanges() {
        if (this.data != null) {
            if (this.data.hasOwnProperty(this.property['x-key'])) {
                this.dataValue = this.data[this.property['x-key']];
            }
        } else {
            this.dataValue = null;
        }

    }

    /**
     * Decide the type of the property based on type and custom x- attributes.
     */
    propertyType(): string {
        if (this.property != null && this.property.type != null) {
            return this.property.type;
        } else if (this.property != null && this.property.$ref != null) {
            return 'object';
        } else {
            return 'json-default';
        }
    }
}
