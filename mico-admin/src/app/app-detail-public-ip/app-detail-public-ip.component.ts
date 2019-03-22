import { Component, Input, OnChanges, OnInit, OnDestroy } from '@angular/core';
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'mico-app-detail-public-ip',
    templateUrl: './app-detail-public-ip.component.html',
    styleUrls: ['./app-detail-public-ip.component.css']
})
export class AppDetailPublicIpComponent implements OnInit, OnChanges, OnDestroy {

    private subPublicIps: Subscription[];
    private subServiceInterface: Subscription[];

    @Input() application;

    publicIps = new Map();

    constructor(
        private apiService: ApiService,
    ) { }

    ngOnInit() {
        this.subPublicIps = [];
        this.subServiceInterface = [];
    }

    ngOnDestroy() {
        // TODO safe unsubscribe
        // TODO safe unsubscribe for lists in util
    }


    ngOnChanges() {

        if (this.application != null) {

            // get the public ips
            this.application.services.forEach(service => {

                // assumption: one public ip per interface
                this.subServiceInterface.push(this.apiService.getServiceInterfaces(service.shortName, service.version)
                    .subscribe(serviceInterfaces => {

                        serviceInterfaces.forEach(micoInterface => {
                            this.subPublicIps.push(this.apiService
                                .getServiceInterfacePublicIp(service.shortName, service.version, micoInterface.serviceInterfaceName)
                                .subscribe(publicIpDTO => {

                                    this.publicIps.set(service.shortName + '#' + publicIpDTO.name, publicIpDTO);

                                }));
                        });
                    }));

            });
        }
    }
}
