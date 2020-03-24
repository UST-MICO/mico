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

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ApiService } from 'src/app/api/api.service';
import { MAT_DIALOG_DATA, MatDialogRef, MatTableDataSource } from '@angular/material';
import { SelectionModel } from '@angular/cdk/collections';
import { safeUnsubscribe } from 'src/app/util/utils';

enum FilterTypes {
    None,
    Internal,
    External,
}

enum ChoiceTypes {
    single,
    multi,
}

export interface Pattern {
    name: string;
    description: string;
    id: number;
}

@Component({
    selector: 'mico-pattern-picker',
    templateUrl: './pattern-picker.component.html',
    styleUrls: ['./pattern-picker.component.css']
})

export class PatternPickerComponent implements OnInit, OnDestroy {

    filter = FilterTypes.None;
    choiceModel = ChoiceTypes.multi;
    existingDependencies: Set<string> = new Set();

    private serviceSubscription: Subscription;

    displayedColumns: string[] = ['icon', 'name', 'description'];
    dataSource;
    selection;

    // used for highlighting
    selectedRowIndex: number = -1;

    constructor(
        public dialogRef: MatDialogRef<PatternPickerComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any,
        private apiService: ApiService,
    ) {

        if (data.choice === 'single') {
            this.choiceModel = ChoiceTypes.single;
            this.selection = new SelectionModel<Pattern>(false, []);
        } else if (data.choice === 'multi') {
            this.choiceModel = ChoiceTypes.multi;
            this.displayedColumns = ['select', 'icon', 'name', 'description'];
            this.selection = new SelectionModel<Pattern>(true, []);
        }
    }

    ngOnInit() {
        const tempPatternGroups: any[] = [{
            id: 1,
            name: 'Message Filter',
            description: 'A Message Filter',
            openFaaSFunctionName: 'message-filter'
        }, {
            id: 2,
            name: "Content Based Router",
            description: " A Content-Based Router",
            openFaaSFunctionName: 'content-based-router'
        }, {
            id: 3,
            name: 'Publish-Subscribe Channel',
            description: 'A Pub-Sub Channel',
            openFaaSFunctionName: 'publish-subscribe-channel'
        }];
        this.dataSource = new MatTableDataSource(tempPatternGroups);
    }

    ngOnDestroy() {
        safeUnsubscribe(this.serviceSubscription);
    }

    getSelectedPattern() {

        return this.selection.selected;
    }


    applyFilter(filterValue: string) {
        this.dataSource.filter = filterValue.trim().toLowerCase();
    }

    isAllSelected() {
        const numSelected = this.selection.selected.length;
        const numRows = this.dataSource.data.length;
        return numSelected === numRows;
    }

    /** Selects all rows if they are not all selected; otherwise clear selection. */
    masterToggle() {
        this.isAllSelected() ?
            this.selection.clear() :
            this.dataSource.data.forEach(row => this.selection.select(row));
    }

    highlight(row) {
        this.selectedRowIndex = row.id;
    }

}
