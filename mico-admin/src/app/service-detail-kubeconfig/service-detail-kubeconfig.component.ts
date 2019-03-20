import { Component, OnChanges, Input, OnDestroy } from '@angular/core';
import { ApiService } from '../api/api.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'mico-service-detail-kubeconfig',
    templateUrl: './service-detail-kubeconfig.component.html',
    styleUrls: ['./service-detail-kubeconfig.component.css']
})
export class ServiceDetailKubeconfigComponent implements OnChanges, OnDestroy {

    private subYaml: Subscription;

    @Input() shortName;
    @Input() version;

    kubeConfigYaml: string;

    constructor(
        private apiService: ApiService,
    ) { }

    ngOnChanges() {
        if (this.shortName != null && this.version != null) {

            // TODO safeUnsubscribe subYaml

            this.subYaml = this.apiService.getServiceYamlConfig(this.shortName, this.version)
                .subscribe(val => {
                    console.log(val);
                    this.kubeConfigYaml = val.yaml;
                });
        }
    }

    ngOnDestroy() {
        // TODO unsubscribe as soon as ui-maintenance-3 branch is merged
    }
}
