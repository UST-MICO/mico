import { BrowserModule } from '@angular/platform-browser';
import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import 'mico-grapheditor/dist/bundle';

// modules

import { MatButtonModule, MatButtonToggleModule, MatCardModule, MatMenuModule,
    MatIconModule, MatListModule, MatSidenavModule, MatDialogModule, MatToolbarModule,
    MatTooltipModule, MatTableModule, MatInputModule, MatTabsModule, MatChipsModule} from '@angular/material';
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
    ],
    entryComponents: [
        CreateServiceDialogComponent,
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
    ],
    providers: [],
    bootstrap: [AppComponent],
    schemas: [
      CUSTOM_ELEMENTS_SCHEMA
    ]
})
export class AppModule { }
