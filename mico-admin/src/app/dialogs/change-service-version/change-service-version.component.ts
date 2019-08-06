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

import { SelectionModel } from '@angular/cdk/collections';
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatTableDataSource } from '@angular/material';
import { selection } from 'd3';
import { take } from 'rxjs/operators';
import { ApiService } from 'src/app/api/api.service';
import { ApiObject } from 'src/app/api/apiobject';
import { incrementVersion, versionComparator, versionComponents } from 'src/app/api/semantic-version';

@Component({
  selector: 'mico-change-service-version',
  templateUrl: './change-service-version.component.html',
  styleUrls: ['./change-service-version.component.css']
})
export class ChangeServiceVersionComponent implements OnInit {

    service: ApiObject;
    versions: ApiObject[];
    dataSource: MatTableDataSource<ApiObject>;
    selection: SelectionModel<ApiObject>;
    selected: ApiObject;

    constructor(
        public dialogRef: MatDialogRef<ChangeServiceVersionComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any,
        private apiService: ApiService,
    ) {
        this.service = data.service;
    }

    ngOnInit() {
        this.apiService.getServiceVersions(this.service.shortName).pipe(take(2)).subscribe(versions => {
            this.versions = JSON.parse(JSON.stringify(versions)).sort((n1, n2) => versionComparator(n1.version, n2.version));
            this.dataSource = new MatTableDataSource(this.versions);
            this.selection = new SelectionModel<ApiObject>(false, []);
            this.selection.changed.subscribe(change => {
                const selected = this.selection.selected;
                if (selected.length > 0) {
                    this.selected = selected[0];
                } else {
                    this.selected = null;
                }
            });
            if (this.selected == null) {
                this.selection.select(this.service);
            }
        });
    }

    confirm() {
        return this.selected;
    }

}
