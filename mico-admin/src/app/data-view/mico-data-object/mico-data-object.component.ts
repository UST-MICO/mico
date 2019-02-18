import { Component, Input } from '@angular/core';
import { ApiModel } from 'src/app/api/apimodel';

@Component({
    selector: 'mico-data-object',
    templateUrl: './mico-data-object.component.html',
    styleUrls: ['./mico-data-object.component.css']
})
export class MicoDataObjectComponent {

    constructor() { }

    @Input() config: ApiModel;
    @Input() dataValue: any;

}
