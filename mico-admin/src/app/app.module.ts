import { BrowserModule } from '@angular/platform-browser';
import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import 'mico-grapheditor/dist/bundle';

// modules

import {
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
    MatSlideToggleModule
} from '@angular/material';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatExpansionModule } from '@angular/material/expansion';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { RoutingModule } from './routing.module';

// components
import { AppComponent } from './app.component';

// page components
import { DashboardComponent } from './dashboard/dashboard.component';
import { AppDetailOverviewComponent } from './app-detail-overview/app-detail-overview.component';
import { AppDetailDeploystatusComponent } from './app-detail-deploystatus/app-detail-deploystatus.component';
import { AppDetailDeploysettingsComponent } from './app-detail-deploysettings/app-detail-deploysettings.component';
import { ServiceDetailDeploystatusComponent } from './service-detail-deploystatus/service-detail-deploystatus.component';
import { ServiceDetailOverviewComponent } from './service-detail-overview/service-detail-overview.component';
import { ToolbarComponent } from './toolbar/toolbar.component';

// non page components
import { MicoFormComponent } from './forms/mico-form/mico-form.component';
import { MicoFormQuestionComponent } from './forms/mico-form-question/mico-form-question.component';
import { AppListComponent } from './app-list/app-list.component';
import { AppDetailComponent } from './app-detail/app-detail.component';
import { ServiceListComponent } from './service-list/service-list.component';
import { ServiceDetailComponent } from './service-detail/service-detail.component';
import { CreateServiceDialogComponent } from './dialogs/create-service/create-service.component';
import { MicoFormBooleanComponent } from './forms/mico-form-boolean/mico-form-boolean.component';
import { MicoFormNumberComponent } from './forms/mico-form-number/mico-form-number.component';
import { ServicePickerComponent } from './dialogs/service-picker/service-picker.component';
import { YesNoDialogComponent } from './dialogs/yes-no-dialog/yes-no-dialog.component';
import { CreateServiceInterfaceComponent } from './dialogs/create-service-interface/create-service-interface.component';
import { MetricChartComponent } from './metric-chart/metric-chart.component';

import { NgxChartsModule } from '@swimlane/ngx-charts';
@NgModule({
    declarations: [
        AppComponent,
        DashboardComponent,
        AppDetailOverviewComponent,
        AppDetailDeploystatusComponent,
        AppDetailDeploysettingsComponent,
        ServiceDetailDeploystatusComponent,
        ServiceDetailOverviewComponent,
        ToolbarComponent,
        MicoFormComponent,
        MicoFormQuestionComponent,
        AppListComponent,
        AppDetailComponent,
        ServiceListComponent,
        ServiceDetailComponent,
        CreateServiceDialogComponent,
        MicoFormBooleanComponent,
        MicoFormNumberComponent,
        ServicePickerComponent,
        YesNoDialogComponent,
        CreateServiceInterfaceComponent,
        MetricChartComponent
    ],
    entryComponents: [
        // dialogs
        CreateServiceDialogComponent,
        ServicePickerComponent,
        YesNoDialogComponent,
        CreateServiceInterfaceComponent
    ],
    imports: [
        BrowserModule,

        FormsModule,
        ReactiveFormsModule,

        RoutingModule,

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
        MatExpansionModule,
        NgxChartsModule
    ],
    providers: [],
    bootstrap: [AppComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class AppModule {}
