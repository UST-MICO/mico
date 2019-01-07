import { Component, OnChanges, Input } from '@angular/core';
import { ApiModel } from 'src/app/api/apimodel';

@Component({
    selector: 'mico-data-container',
    templateUrl: './mico-data-container.component.html',
    styleUrls: ['./mico-data-container.component.css']
})
export class MicoDataContainerComponent implements OnChanges {

    @Input() property: ApiModel;
    @Input() data: any;

    dataValue: any;

    constructor() { }

    ngOnChanges() {
        if (this.data != null) {
            if (this.data.hasOwnProperty(this.property['x-key'])) {
                this.dataValue = this.data[this.property['x-key']];
            }
        } else {
            this.dataValue = null;
        }

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
