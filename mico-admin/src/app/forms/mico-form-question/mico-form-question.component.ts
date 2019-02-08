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

    get formControl() {
        if (this.form == null || this.property == null) {
            return null;
        }
        return this.form.get(this.property['x-key']);
    }

    constructor() { }

    ngOnInit() {
    }

    /**
     * Decide the type of the property based on type and custom x- attributes.
     */
    propertyType(): string {
        if (this.property != null && this.property.type != null) {
            return this.property.type;
        } else if (this.property != null && this.property.$ref != null) {
            return 'object';
        } else { return 'string'; }
    }

    getErrorMessage() {
        if (this.formControl.hasError('required')) {
            return 'You must enter a value';
        }
        if (this.formControl.hasError('minlength')) {
            return 'Input is too short.';
        }
        if (this.formControl.hasError('maxlength')) {
            return 'Input is too long.';
        }
        if (this.formControl.hasError('pattern')) {
            return 'Invalid input.';
        }
        return 'HI';
    }

}
