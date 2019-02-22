import { Component, Input } from '@angular/core';
import { ApiModel } from 'src/app/api/apimodel';

@Component({
    selector: 'mico-data-array',
    templateUrl: './mico-data-array.component.html',
    styleUrls: ['./mico-data-array.component.css']
})
export class MicoDataArrayComponent {

    constructor() { }

    @Input() config: ApiModel;
    @Input() dataValue: any;

    trackBy(index) {
        return index;
    }
}
