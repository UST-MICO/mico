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

import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
// modules
import {
    MatButtonModule, MatButtonToggleModule, MatCardModule, MatChipsModule, MatDialogModule, MatIconModule,
    MatInputModule, MatListModule, MatMenuModule, MatSidenavModule, MatSlideToggleModule, MatTableModule, MatTabsModule,
    MatToolbarModule, MatTooltipModule
} from '@angular/material';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { OwlDateTimeModule, OwlNativeDateTimeModule } from 'ng-pick-datetime';

import {
    AppDetailOverviewDeploymentInformationComponent
} from './app-detail-overview-deployment-information/app-detail-overview-deployment-information.component';
import { AppDetailOverviewComponent } from './app-detail-overview/app-detail-overview.component';
import { AppDetailPublicIpComponent } from './app-detail-public-ip/app-detail-public-ip.component';
import { AppDetailStatusComponent } from './app-detail-status/app-detail-status.component';
import { AppDetailComponent } from './app-detail/app-detail.component';
import { AppListComponent } from './app-list/app-list.component';
// components
import { AppComponent } from './app.component';
// page components
import { DashboardComponent } from './dashboard/dashboard.component';
import { MicoDataArrayComponent } from './data-view/mico-data-array/mico-data-array.component';
import { MicoDataBooleanComponent } from './data-view/mico-data-boolean/mico-data-boolean.component';
import { MicoDataContainerComponent } from './data-view/mico-data-container/mico-data-container.component';
import { MicoDataObjectComponent } from './data-view/mico-data-object/mico-data-object.component';
import { MicoDataStringComponent } from './data-view/mico-data-string/mico-data-string.component';
import { MicoDataComponent } from './data-view/mico-data/mico-data.component';
import { ChangeServiceVersionComponent } from './dialogs/change-service-version/change-service-version.component';
import { CreateApplicationComponent } from './dialogs/create-application/create-application.component';
import { CreateNextVersionComponent } from './dialogs/create-next-version/create-next-version.component';
import { CreateServiceInterfaceComponent } from './dialogs/create-service-interface/create-service-interface.component';
import { CreateServiceDialogComponent } from './dialogs/create-service/create-service.component';
import {
    DeploymentInformationDialogComponent
} from './dialogs/deployment-information-dialog/deployment-information-dialog.component';
import {
    GraphAddEnvironmentVariableComponent
} from './dialogs/graph-add-environment-variable/graph-add-environment-variable.component';
import { ServicePickerComponent } from './dialogs/service-picker/service-picker.component';
import { UpdateServiceInterfaceComponent } from './dialogs/update-service-interface/update-service-interface.component';
import { YesNoDialogComponent } from './dialogs/yes-no-dialog/yes-no-dialog.component';
import { MicoFormArrayComponent } from './forms/mico-form-array/mico-form-array.component';
import { MicoFormBooleanComponent } from './forms/mico-form-boolean/mico-form-boolean.component';
import { MicoFormNumberComponent } from './forms/mico-form-number/mico-form-number.component';
import { MicoFormObjectComponent } from './forms/mico-form-object/mico-form-object.component';
import { MicoFormQuestionComponent } from './forms/mico-form-question/mico-form-question.component';
// non page components
import { MicoFormComponent } from './forms/mico-form/mico-form.component';
import { GraphsModule } from './graphs/graphs.module';
import { ChartComponent } from './metric-chart/chart/chart.component';
import { MetricChartComponent } from './metric-chart/metric-chart.component';
import { RoutingModule } from './routing.module';
import {
    ServiceDetailDeploystatusComponent
} from './service-detail-deploystatus/service-detail-deploystatus.component';
import { ServiceDetailKubeconfigComponent } from './service-detail-kubeconfig/service-detail-kubeconfig.component';
import { ServiceDetailOverviewComponent } from './service-detail-overview/service-detail-overview.component';
import { ServiceDetailStatusComponent } from './service-detail-status/service-detail-status.component';
import { ServiceDetailComponent } from './service-detail/service-detail.component';
import { ServiceListComponent } from './service-list/service-list.component';
import { ToolbarComponent } from './toolbar/toolbar.component';

@NgModule({
    declarations: [
        AppComponent,
        DashboardComponent,
        AppDetailOverviewComponent,
        ServiceDetailDeploystatusComponent,
        ServiceDetailOverviewComponent,
        ToolbarComponent,
        MicoFormComponent,
        MicoFormQuestionComponent,
        MicoFormBooleanComponent,
        MicoFormNumberComponent,
        MicoFormArrayComponent,
        MicoFormObjectComponent,
        AppListComponent,
        AppDetailComponent,
        ServiceListComponent,
        ServiceDetailComponent,
        CreateServiceDialogComponent,
        ChangeServiceVersionComponent,
        ServicePickerComponent,
        YesNoDialogComponent,
        CreateServiceInterfaceComponent,
        UpdateServiceInterfaceComponent,
        MetricChartComponent,
        ChartComponent,
        MicoDataComponent,
        MicoDataStringComponent,
        MicoDataContainerComponent,
        MicoDataBooleanComponent,
        CreateApplicationComponent,
        CreateNextVersionComponent,
        MicoDataArrayComponent,
        MicoDataObjectComponent,
        AppDetailStatusComponent,
        ServiceDetailStatusComponent,
        AppDetailOverviewDeploymentInformationComponent,
        DeploymentInformationDialogComponent,
        AppDetailPublicIpComponent,
        ServiceDetailKubeconfigComponent,
        GraphAddEnvironmentVariableComponent
    ],
    entryComponents: [
        // dialogs
        CreateServiceDialogComponent,
        ChangeServiceVersionComponent,
        ServicePickerComponent,
        YesNoDialogComponent,
        CreateServiceInterfaceComponent,
        UpdateServiceInterfaceComponent,
        CreateApplicationComponent,
        CreateNextVersionComponent,
        DeploymentInformationDialogComponent,
        GraphAddEnvironmentVariableComponent,
    ],
    imports: [
        BrowserModule,

        FormsModule,
        ReactiveFormsModule,

        RoutingModule,

        GraphsModule,

        HttpModule,


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
        MatSelectModule,
        MatExpansionModule,
        OwlDateTimeModule,
        OwlNativeDateTimeModule,
        MatSnackBarModule,
        MatRadioModule,
        MatStepperModule
    ],
    providers: [],
    bootstrap: [AppComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class AppModule { }
