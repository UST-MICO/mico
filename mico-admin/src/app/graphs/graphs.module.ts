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

import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';

import '@ustutt/grapheditor-webcomponent/lib/index';

import { AppDependencyGraphComponent } from './app-dependency-graph/app-dependency-graph.component';
import { ServiceDependencyGraphComponent } from './service-dependency-graph/service-dependency-graph.component';
import {
    MatButtonModule, MatButtonToggleModule, MatCardModule, MatMenuModule,
    MatIconModule, MatListModule, MatSidenavModule, MatDialogModule, MatToolbarModule,
    MatTooltipModule, MatTableModule, MatInputModule, MatTabsModule, MatChipsModule, MatSlideToggleModule,
} from '@angular/material';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

@NgModule({
    imports: [
        CommonModule,

        // material
        BrowserAnimationsModule,
        MatButtonModule,
        MatButtonToggleModule,
        MatCardModule,
        MatMenuModule,
        MatIconModule,
        MatListModule,
        MatSidenavModule,
        MatDialogModule,
        MatToolbarModule,
        MatTooltipModule,
        MatTableModule,
        MatInputModule,
        MatTabsModule,
        MatChipsModule,
        MatSlideToggleModule,
        MatAutocompleteModule,
        MatCheckboxModule,
    ],
    declarations: [
        AppDependencyGraphComponent,
        ServiceDependencyGraphComponent,
    ],
    exports: [
        AppDependencyGraphComponent,
        ServiceDependencyGraphComponent,

        CommonModule,
    ],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA,
    ]
})
export class GraphsModule { }
