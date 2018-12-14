import { Component, OnInit, Input } from '@angular/core';
import { ApiModel } from 'src/app/api/apimodel';

@Component({
    selector: 'mico-data-container',
    templateUrl: './mico-data-container.component.html',
    styleUrls: ['./mico-data-container.component.css']
})
export class MicoDataContainerComponent implements OnInit {

    @Input() property: ApiModel;

    constructor() { }

    ngOnInit() {
    }

    /**
     * Decide the type of the property based on type and custom x- attributes.
     */
    propertyType(): string {
        if (this.property != null && this.property.type != null) {
            return this.property.type;
        } else { return 'string'; }
    }
}
