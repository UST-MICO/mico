import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

//modules
import { MatButtonModule, MatCardModule, MatMenuModule, MatIconModule, MatListModule, MatSidenavModule, MatDialogModule, MatToolbarModule, MatTooltipModule, } from '@angular/material';

import { RoutingModule } from './routing.module';
import { AppRoutingModule } from './app-routing.module';

//components
import { AppComponent } from './app.component';

import { DashboardComponent } from './dashboard/dashboard.component';
import { AppDetailOverviewComponent } from './app-detail-overview/app-detail-overview.component';
import { AppDetailDeploystatusComponent } from './app-detail-deploystatus/app-detail-deploystatus.component';
import { AppDetailDeploysettingsComponent } from './app-detail-deploysettings/app-detail-deploysettings.component';
import { ServiceDetailDeploystatusComponent } from './service-detail-deploystatus/service-detail-deploystatus.component';
import { ServiceDetailOverviewComponent } from './service-detail-overview/service-detail-overview.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';


@NgModule({
    declarations: [
        AppComponent,
        DashboardComponent,
        AppDetailOverviewComponent,
        AppDetailDeploystatusComponent,
        AppDetailDeploysettingsComponent,
        ServiceDetailDeploystatusComponent,
        ServiceDetailOverviewComponent,
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
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }
