import { Component, OnInit, Input } from '@angular/core';
import { ApiModel } from '../../api/apimodel';

@Component({
    selector: 'mico-data-string',
    templateUrl: './mico-data-string.component.html',
    styleUrls: ['./mico-data-string.component.css']
})
export class MicoDataStringComponent implements OnInit {

    constructor() { }

    @Input() config: ApiModel;
    @Input() dataValue: any;

    ngOnInit() {
    }

}
