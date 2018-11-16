import { Injectable } from '@angular/core';
import { FormGroup, FormControl, AbstractControl } from '@angular/forms';

import { Observable } from 'rxjs';

import { ApiModel } from '../api/apimodel';
import { stringify } from '@angular/compiler/src/util';


@Injectable({
    providedIn: 'root'
})
export class FormGroupService {

    constructor() { }

    modelToFormGroup(model: ApiModel): FormGroup {
        const group: {[key: string]: AbstractControl} = {};
        if (model.properties != null) {
            for (const key in model.properties) {
                let value = null;
                if (model.properties.hasOwnProperty('default')) {
                    value = model.properties.default;
                }
                const validators = [];
                group[key] = new FormControl( value, validators);
            }
            return new FormGroup(group);
        }
    }
}
