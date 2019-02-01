import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { Subscription } from 'rxjs';
import { MatDialog } from '@angular/material';
import { ServicePickerComponent } from '../dialogs/service-picker/service-picker.component';
import { versionComparator } from '../api/semantic-version';

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
                                this.application = JSON.parse(JSON.stringify(element));
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
                // TODO wait for propper return value from deploy endpoint
                // add some deployment monitoring (e.g. state)
                console.log(val);
            });
    }

    /**
     * takes a list of applications and sets this.application to the application with the latest version
     * this.version is set accoringly
     */
    setLatestVersion(list) {
        list.forEach(element => {

            let version = '0.0.0';

            if (versionComparator(element.version, version) > 0) {
                version = element.version;
                this.selectedVersion = element.version;
                this.application = JSON.parse(JSON.stringify(element));

            }
        });
    }

    addService() {

        const dialogRef = this.dialog.open(ServicePickerComponent, {
            data: {
                filter: '',
                choice: 'multi',
                existingDependencies: this.application.services,
                serviceId: '',
            }
        });
        this.subDependeesDialog = dialogRef.afterClosed().subscribe(result => {

            if (result === '') {
                return;
            }

            // TODO consider if null check is still neccesary as soon as endpoint to add dependencies exists
            if (this.application.services == null) {
                this.application.services = [];
            }

            result.forEach(service => {
                // this.application.services.push(element);
                // TODO Consider adding all at once.
                this.apiService.postApplicationServices(this.application.shortName, this.application.version, service)
                    .subscribe(val => {
                        console.log(val);
                    });
            });
        });

    }

    deleteService(serviceShortName: string) {

        // Local handling, TODO remove as soon as the delete endpoint actually exists
        /*
        let searchForId = -1;

        let counter = 0;
        this.application.services.forEach(element => {
            if (element.shortName === shortName && element.version === version) {
                searchForId = counter;
            }
            counter++;
        });

        if (searchForId >= 0) {
            this.application.services.splice(searchForId, 1);
        }
        */

        this.apiService.deleteApplicationServices(this.application.shortName, this.application.version, serviceShortName)
            .subscribe(val => {
                // TODO add some user output (as soon as the endpoint actually exists)
                console.log(val);
            });
    }

    /**
    * call-back from the version picker
    */
    updateVersion(version) {
        this.selectedVersion = version;
        this.router.navigate(['app-detail', this.application.shortName, version]);
    }
}
