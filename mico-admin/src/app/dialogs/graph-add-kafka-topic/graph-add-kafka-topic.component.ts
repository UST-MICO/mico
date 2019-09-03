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

import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
    selector: 'mico-graph-add-kafka-topic',
    templateUrl: './graph-add-kafka-topic.component.html',
    styleUrls: ['./graph-add-kafka-topic.component.css']
})
export class GraphAddKafkaTopicComponent {


    constructor(public dialogRef: MatDialogRef<GraphAddKafkaTopicComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any,
    ) {
        this.serviceShortName = data.serviceShortName;
        this.possibleRoles = ['INPUT', 'OUTPUT'].filter(r => !data.existingRoles.some(existing => existing === r));
        if (this.possibleRoles.length === 2) {
            this.role = this.possibleRoles[1];
        } else if (this.possibleRoles.length === 1) {
            this.role = this.possibleRoles[0];
        }
    }

    serviceShortName;
    possibleRoles;

    topic;
    role;

    isValid() {
        if (this.topic == null || this.topic === '') {
            return false;
        }
        return /^[a-zA-Z0-9\._\-]+$/.test(this.topic);
    }

    /**
     * return method of the dialog
     */
    response() {
        if (!this.isValid()) {
            // invalid topic!
            return '';
        }

        return {
            kafkaTopicName: this.topic,
            role: this.role,
        };
    }

}
