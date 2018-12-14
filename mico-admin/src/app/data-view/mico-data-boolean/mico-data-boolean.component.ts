import { Component, OnInit, Input } from '@angular/core';
import { ApiModel } from '../../api/apimodel';

@Component({
    selector: 'mico-mico-data-boolean',
    templateUrl: './mico-data-boolean.component.html',
    styleUrls: ['./mico-data-boolean.component.css']
})
export class MicoDataBooleanComponent implements OnInit {

    constructor() { }

    @Input() config: ApiModel;
    @Input() dataValue: any;

    convertedDataValue: boolean;

    ngOnInit() {
        this.convertedDataValue = JSON.parse(this.dataValue);
    }

}
