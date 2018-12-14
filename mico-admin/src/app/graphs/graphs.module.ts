import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';

import 'mico-grapheditor/dist/bundle';

import { AppDependencyGraphComponent } from './app-dependency-graph/app-dependency-graph.component';

@NgModule({
    imports: [
        CommonModule,
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
