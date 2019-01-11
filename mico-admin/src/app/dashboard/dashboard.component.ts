import { Component, OnInit, Input } from '@angular/core';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { MatDialog } from '@angular/material';
import { CreateServiceDialogComponent } from '../dialogs/create-service/create-service.component';
import { Router } from '@angular/router';

@Component({
    selector: 'mico-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
        private router: Router,
    ) {
        this.getApplications();
    }

    applications: ApiObject;

    displayedColumns: string[] = ['id', 'name', 'shortName'];

    ngOnInit() {

    }


    /**
     * receives a list of applications from the apiService
     */
    getApplications(): void {
        this.apiService.getApplications()
            .subscribe(applications => this.applications = applications);
    }


    newService(): void {
        const dialogRef = this.dialog.open(CreateServiceDialogComponent);
        dialogRef.afterClosed().subscribe(result => {
            this.apiService.postService(result).subscribe(val => {
                console.log(val);
                this.router.navigate(['service-detail', val.shortName, val.version]);
            });
        });
    }



}
