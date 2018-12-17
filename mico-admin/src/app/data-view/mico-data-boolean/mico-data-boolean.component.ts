import { Component, Input, OnChanges } from '@angular/core';
import { ApiModel } from '../../api/apimodel';

@Component({
    selector: 'mico-data-boolean',
    templateUrl: './mico-data-boolean.component.html',
    styleUrls: ['./mico-data-boolean.component.css']
})
export class MicoDataBooleanComponent implements OnChanges {

    constructor() { }

    @Input() config: ApiModel;
    @Input() dataValue: any;

    convertedDataValue: boolean;

    ngOnChanges() {
        console.log('Boolean Config: ', this.config);
        this.convertedDataValue = JSON.parse(this.dataValue);
    }

}
