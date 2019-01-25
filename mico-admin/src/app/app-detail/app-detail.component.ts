import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { Subscription } from 'rxjs';
import { MatDialog } from '@angular/material';
import { ServicePickerComponent } from '../dialogs/service-picker/service-picker.component';

@Component({
    selector: 'mico-app-detail',
    templateUrl: './app-detail.component.html',
    styleUrls: ['./app-detail.component.css']
})
export class AppDetailComponent implements OnInit, OnDestroy {

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
        private dialog: MatDialog,
        private router: Router,
    ) { }

    subRouteParams: Subscription;
    subApplicationVersions: Subscription;
    subDeploy: Subscription;
    subDependeesDialog: Subscription;

    application: ApiObject;
    selectedVersion;
    allVersions;

    ngOnInit() {

        this.subRouteParams = this.route.params.subscribe(params => {
            const shortName = params['shortName'];
            const givenVersion = params['version'];

            this.subApplicationVersions = this.apiService.getApplicationVersions(shortName)
                .subscribe(versions => {

                    this.allVersions = versions;

                    if (givenVersion == null) {
                        this.setLatestVersion(versions);
                    } else {
                        let found = false;
                        found = versions.some(element => {

                            if (element.version === givenVersion) {
                                this.selectedVersion = givenVersion;
                                this.application = element;
                                return true;
                            }
                        });
                        if (!found) {
                            // given version was not found in the versions list, take latest instead
                            this.setLatestVersion(versions);
                        }
                    }
                });
        });

    }

    ngOnDestroy() {
        this.unsubscribe(this.subRouteParams);
        this.unsubscribe(this.subApplicationVersions);
        this.unsubscribe(this.subDeploy);
        this.unsubscribe(this.subDependeesDialog);
    }

    unsubscribe(subscription: Subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    deployApplication() {
        this.subDeploy = this.apiService.postApplicationDeployCommand(this.application.shortName, this.application.version)
            .subscribe(val => {
                console.log(val);
            });
    }

    /**
     * takes a list of applications and sets this.application to the application with the latest version
     * this.version is set accoringly
     */
    setLatestVersion(list) {
        list.forEach(element => {

            let version = '0';

            // TODO implement comparison for semantic versioning
            if (element.version > version) {
                version = element.version;
                this.selectedVersion = element.version;
                this.application = element;

            }
        });
    }

    addService() {

        console.log(this.application);
        // TODO fix implementation

        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: '',
                choice: 'multi',
                existingDependencies: this.application.dependsOn,
                serviceId: '',
            }
        });
        this.subDependeesDialog = dialogRef.afterClosed().subscribe(result => {
            console.log(result);
            result.forEach(element => {
                // this.services.push(element);
            });
        });

    }

    deleteService(shortName: string, version: string) {
        // TODO implement as soon as there are working dummy values and application endpoints
    }

    /**
    * call-back from the version picker
    */
    updateVersion(version) {
        this.selectedVersion = version;
        this.router.navigate(['app-detail', this.application.shortName, version]);
    }
}
