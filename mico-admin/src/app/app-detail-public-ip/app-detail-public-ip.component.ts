import { Component, Input, OnChanges, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';
import { safeUnsubscribeList, safeUnsubscribe } from '../util/utils';

@Component({
    selector: 'mico-app-detail-public-ip',
    templateUrl: './app-detail-public-ip.component.html',
    styleUrls: ['./app-detail-public-ip.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppDetailPublicIpComponent implements OnInit, OnChanges, OnDestroy {

    private subApplication: Subscription;
    private subPublicIps: Subscription[];
    private subServiceInterfaces: Subscription[];

    @Input() applicationShortName;
    @Input() applicationVersion;

    publicIps = new Map();

    constructor(
        private apiService: ApiService,
        private changeDedection: ChangeDetectorRef,
    ) { }

    ngOnInit() {
        this.subPublicIps = [];
        this.subServiceInterfaces = [];
    }

    ngOnDestroy() {
        safeUnsubscribe(this.subApplication);
        safeUnsubscribeList(this.subPublicIps);
        safeUnsubscribeList(this.subServiceInterfaces);
    }


    ngOnChanges() {

        if (this.applicationShortName != null && this.applicationVersion != null) {

            safeUnsubscribeList(this.subPublicIps);
            safeUnsubscribeList(this.subServiceInterfaces);

            // get the public ips
            safeUnsubscribe(this.subApplication);
            this.subApplication = this.apiService.getApplication(this.applicationShortName, this.applicationVersion)
                .subscribe(application => {

                    application.services.forEach(service => {

                        safeUnsubscribeList(this.subServiceInterfaces);
                        // assumption: one public ip per interface
                        this.subServiceInterfaces.push(this.apiService.getServiceInterfaces(service.shortName, service.version)
                            .subscribe(serviceInterfaces => {

                                safeUnsubscribeList(this.subPublicIps);

                                serviceInterfaces.forEach(micoInterface => {
                                    this.subPublicIps.push(this.apiService
                                        .getServiceInterfacePublicIp(service.shortName, service.version, micoInterface.serviceInterfaceName)
                                        .subscribe(publicIpDTO => {

                                            this.publicIps.set(service.shortName + '#' + publicIpDTO.name, publicIpDTO);
                                            this.changeDedection.markForCheck();

                                        }));
                                });
                            }));

                    });
                });
        }
    }
}
