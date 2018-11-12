import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import {DashboardComponent} from './dashboard/dashboard.component';
import { AppDetailOverviewComponent } from './app-detail-overview/app-detail-overview.component';
import { AppDetailDeploystatusComponent } from './app-detail-deploystatus/app-detail-deploystatus.component'
import { AppDetailDeploysettingsComponent } from './app-detail-deploysettings/app-detail-deploysettings.component'
import { ServiceDetailOverviewComponent } from './service-detail-overview/service-detail-overview.component'
import { ServiceDetailDeploystatusComponent} from './service-detail-deploystatus/service-detail-deploystatus.component'


const routes: Routes = [
    { path: 'dashboard', component: DashboardComponent },
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
    { path: 'app-detail-overview', component: AppDetailOverviewComponent },
    { path: 'app-detail-deploystatus', component: AppDetailDeploystatusComponent },
    { path: 'app-detail-deploysettings', component: AppDetailDeploysettingsComponent },
    { path: 'service-detail-overview', component:  ServiceDetailOverviewComponent },
    { path: 'service-detail-deploystatus', component:  ServiceDetailDeploystatusComponent },
  ];

@NgModule({
  declarations: [],
  exports: [ RouterModule ],
  imports: [ RouterModule.forRoot(routes),
  ]
})
export class RoutingModule { }
