import { Component, OnInit, Input } from '@angular/core';
import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { MatDialog } from '@angular/material';
import { CreateServiceDialogComponent } from '../dialogs/create-service/create-service.component';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
    ) {
        this.getApplications();
    }

    @Input() applications: ApiObject[]

    displayedColumns: string[] = ['id', 'name', 'shortName'];

    ngOnInit() {    }

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
            console.log(result);
        });
    }



}
