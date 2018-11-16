import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

//modules
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule, MatCardModule, MatMenuModule, MatIconModule, MatListModule, MatSidenavModule, MatDialogModule, MatToolbarModule, MatTooltipModule, MatInputModule } from '@angular/material';

import { RoutingModule } from './routing.module';
import { AppRoutingModule } from './app-routing.module';

//components
import { AppComponent } from './app.component';

//page components
import { DashboardComponent } from './dashboard/dashboard.component';
import { AppDetailOverviewComponent } from './app-detail-overview/app-detail-overview.component';
import { AppDetailDeploystatusComponent } from './app-detail-deploystatus/app-detail-deploystatus.component';
import { AppDetailDeploysettingsComponent } from './app-detail-deploysettings/app-detail-deploysettings.component';
import { ServiceDetailDeploystatusComponent } from './service-detail-deploystatus/service-detail-deploystatus.component';
import { ServiceDetailOverviewComponent } from './service-detail-overview/service-detail-overview.component';

//non page components
import { MicoFormComponent } from './forms/mico-form/mico-form.component';
import { MicoFormQuestionComponent } from './forms/mico-form-question/mico-form-question.component';


@NgModule({
    declarations: [
        AppComponent,
        DashboardComponent,
        AppDetailOverviewComponent,
        AppDetailDeploystatusComponent,
        AppDetailDeploysettingsComponent,
        ServiceDetailDeploystatusComponent,
        ServiceDetailOverviewComponent,
        MicoFormComponent,
        MicoFormQuestionComponent,
    ],
    imports: [
        BrowserModule,

        AppRoutingModule,
        RoutingModule,

        //material
        BrowserAnimationsModule,
        MatButtonModule,
        MatCardModule,
        MatMenuModule,
        MatIconModule,
        MatListModule,
        MatSidenavModule,
        MatDialogModule,
        MatToolbarModule,
        MatTooltipModule,
        MatInputModule,
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }
