import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Http, ResponseType } from '@angular/http';

import { ApiService } from '../api/api.service';
import { ApiObject } from '../api/apiobject';
import { Subscription } from 'rxjs';
import { MatDialog } from '@angular/material';
import { ServicePickerComponent } from '../dialogs/service-picker/service-picker.component';
import { versionComparator } from '../api/semantic-version';
import { YesNoDialogComponent } from '../dialogs/yes-no-dialog/yes-no-dialog.component';

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
        private http: Http
    ) { }

    subRouteParams: Subscription;
    subApplicationVersions: Subscription;
    subDeploy: Subscription;
    subDependeesDialog: Subscription;
    subDeployInformation: Subscription;
    subPublicIps: Subscription[] = [];
    subApplication: Subscription;
    subServiceDependency: Subscription;

    // immutable application  object which is updated, when new data is pushed
    application: ApiObject;
    shortName: string;
    selectedVersion;
    allVersions;
    publicIps: string[] = [];

    // modifiable application object
    applicationData;
    edit: Boolean = false;
    deployment: string;

    ngOnInit() {

        this.subRouteParams = this.route.params.subscribe(params => {
            this.shortName = params['shortName'];
            const givenVersion = params['version'];

            this.subApplicationVersions = this.apiService.getApplicationVersions(this.shortName)
                .subscribe(versions => {

                    this.allVersions = versions;

                    if (givenVersion == null) {
                        this.setLatestVersion(versions);
                    } else {
                        let found = false;
                        found = versions.some(element => {

                            if (element.version === givenVersion) {
                                this.selectedVersion = givenVersion;
                                this.subscribeApplication(element.version);
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

        this.getTextFile('http://localhost:8080/services/testyaml/1.4/yaml').subscribe(
            res => (this.deployment = this.removeStartingDashes(res['_body']))
        );
    }

    /**
     * subscribe to the given shortName/version and subscribe to its interfaces
     * @param shortName shortName of the application to be displayed
     * @param version version of the application to be displayed
     */
    subscribeApplication(version: string) {

        if (this.subApplication != null) {
            this.subApplication.unsubscribe();
        }

        this.subApplication = this.apiService.getApplication(this.shortName, version).subscribe(val => {
            this.application = val;

            // application is found now, so try to get some more information
            // Deployment information

            this.subDeployInformation = this.apiService
                .getApplicationDeploymentInformation(this.application.shortName, this.application.version)
                .subscribe(deploymentInformation => {
                    console.log(deploymentInformation);
                });


            // public ip
            const tempPublicIps = [];


            this.application.services.forEach(service => {

                if (service.serviceInterfaces != null) {

                    service.serviceInterfaces.forEach(micoInterface => {
                        this.subPublicIps.push(this.apiService
                            .getServiceInterfacePublicIp(service.shortName, service.version, micoInterface.serviceInterfaceName)
                            .subscribe(listOfPublicIps => {
                                listOfPublicIps.forEach(publicIp => {
                                    tempPublicIps.push(publicIp);
                                });
                            }));
                    });
                }
            });
            this.publicIps = tempPublicIps;
        });
    }

    ngOnDestroy() {
        this.unsubscribe(this.subRouteParams);
        this.unsubscribe(this.subApplicationVersions);
        this.unsubscribe(this.subDeploy);
        this.unsubscribe(this.subDependeesDialog);
        this.unsubscribe(this.subDeployInformation);
        this.subPublicIps.forEach(subscription => {
            this.unsubscribe(subscription);
        });
        this.unsubscribe(this.subApplication);
        this.unsubscribe(this.subServiceDependency);
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
        let version = '0.0.0';

        list.forEach(element => {

            if (versionComparator(element.version, version) > 0) {
                version = element.version;
            }
        });
        this.selectedVersion = version;
        this.subscribeApplication(version);
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
                    .subscribe();
            });
        });

    }

    deleteService(serviceShortName: string) {

        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: serviceShortName,
                question: 'deleteDependency'
            }
        });

        this.subServiceDependency = dialogRef.afterClosed().subscribe(shouldDelete => {
            if (shouldDelete) {

                this.apiService.deleteApplicationServices(this.application.shortName, this.application.version, serviceShortName)
                    .subscribe(val => {
                        // TODO add some user output (as soon as the endpoint actually exists)

                    });
            }
        });

    }

    /**
    * call-back from the version picker
    */
    updateVersion(version) {
        this.selectedVersion = version;
        this.router.navigate(['app-detail', this.application.shortName, version]);
    }

    saveApplicationChanges() {
        console.log(this.applicationData);
        this.apiService.putApplication(this.shortName, this.selectedVersion, this.applicationData)
            .subscribe(val => {
                console.log(val);
            });
        this.edit = false;
    }
    getTextFile(filename: string) {
        return this.http.get(filename).pipe();
    }
    removeStartingDashes(yaml: String) {
        return yaml.substring(3).trim();
    }
}
