import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';

import '@ustutt/grapheditor-webcomponent/lib/index';

import { AppDependencyGraphComponent } from './app-dependency-graph/app-dependency-graph.component';
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
    ],
    exports: [
        AppDependencyGraphComponent,

        CommonModule,
    ],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA,
    ]
})
export class GraphsModule { }
