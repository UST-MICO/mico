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

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DashboardComponent } from './dashboard/dashboard.component';

import { AppListComponent } from './app-list/app-list.component';
import { AppDetailComponent } from './app-detail/app-detail.component';
import { AppDetailDeploystatusComponent } from './app-detail-deploystatus/app-detail-deploystatus.component';
import { AppDetailDeploysettingsComponent } from './app-detail-deploysettings/app-detail-deploysettings.component';

import { ServiceListComponent } from './service-list/service-list.component';
import { ServiceDetailComponent } from './service-detail/service-detail.component';

import { MetricChartComponent } from './metric-chart/metric-chart.component';

const routes: Routes = [
    { path: 'dashboard', component: DashboardComponent },
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' },

    { path: 'app-detail/app-list', component: AppListComponent },
    { path: 'app-detail/:shortName', component: AppDetailComponent },
    { path: 'app-detail/:shortName/:version', component: AppDetailComponent },
    { path: 'app-detail/deploystatus', component: AppDetailDeploystatusComponent },
    { path: 'app-detail/deploysettings', component: AppDetailDeploysettingsComponent },
    { path: 'service-detail/service-list', component: ServiceListComponent },
    { path: 'service-detail/:shortName', component: ServiceDetailComponent },
    { path: 'service-detail/:shortName/:version', component: ServiceDetailComponent },
    { path: 'metrics', component: MetricChartComponent }
];

@NgModule({
    declarations: [],
    exports: [RouterModule],
    imports: [RouterModule.forRoot(routes),
    ]
})
export class RoutingModule { }
