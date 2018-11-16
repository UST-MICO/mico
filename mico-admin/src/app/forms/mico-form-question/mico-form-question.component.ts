import { Component, OnInit, Input } from '@angular/core';
import { ApiModel } from 'src/app/api/apimodel';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'mico-form-question',
  templateUrl: './mico-form-question.component.html',
  styleUrls: ['./mico-form-question.component.css']
})
export class MicoFormQuestionComponent implements OnInit {

    @Input() property: ApiModel;
    @Input() form: FormGroup;

    constructor() { }

    ngOnInit() {
    }

    /**
     * Decide the type of the property based on type and custom x- attributes.
     */
    propertyType(): string {
        if (this.property != null && this.property.type != null) {
            return this.property.type;
        }
        else return 'string';
    }

}
