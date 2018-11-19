import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DashboardComponent } from './dashboard/dashboard.component';

import { AppListComponent } from './app-list/app-list.component';
import { AppDetailComponent } from './app-detail/app-detail.component';
import { AppDetailOverviewComponent } from './app-detail-overview/app-detail-overview.component';
import { AppDetailDeploystatusComponent } from './app-detail-deploystatus/app-detail-deploystatus.component';
import { AppDetailDeploysettingsComponent } from './app-detail-deploysettings/app-detail-deploysettings.component';

import { ServiceListComponent } from './service-list/service-list.component';
import { ServiceDetailComponent } from './service-detail/service-detail.component';


const routes: Routes = [
    { path: 'dashboard', component: DashboardComponent },
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' },

    { path: 'app-detail/app-app-list', component: AppListComponent },
    { path: 'app-detail/overview', component: AppDetailOverviewComponent },
    { path: 'app-detail/:id', component:  AppDetailComponent},
    { path: 'app-detail/deploystatus', component: AppDetailDeploystatusComponent },
    { path: 'app-detail/deploysettings', component: AppDetailDeploysettingsComponent },

    { path: 'service-detail/app-service-list', component: ServiceListComponent },
    { path: 'service-detail/:id', component:  ServiceDetailComponent},
  ];

@NgModule({
  declarations: [],
  exports: [ RouterModule ],
  imports: [ RouterModule.forRoot(routes),
  ]
})
export class RoutingModule { }
