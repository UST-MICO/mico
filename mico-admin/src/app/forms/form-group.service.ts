import { Injectable } from '@angular/core';
import { FormGroup, FormControl, AbstractControl, Validators } from '@angular/forms';

import { ApiModel } from '../api/apimodel';


@Injectable({
    providedIn: 'root'
})
export class FormGroupService {

    constructor() { }

    modelToFormGroup(model: ApiModel): FormGroup {
        const group: {[key: string]: AbstractControl} = {};
        if (model.properties != null) {
            for (const key in model.properties) {
                const itemModel = model.properties[key];
                let value = null;
                if (itemModel.hasOwnProperty('default')) {
                    value = (itemModel as ApiModel).default;
                }
                const validators = [];
                if (itemModel.hasOwnProperty('x-required')) {
                    validators.push(Validators.required);
                }
                if (itemModel.hasOwnProperty('minLength')) {
                    validators.push(Validators.minLength((itemModel as ApiModel).minLength));
                }
                if (itemModel.hasOwnProperty('maxLength')) {
                    validators.push(Validators.maxLength((itemModel as ApiModel).maxLength));
                }
                if (itemModel.hasOwnProperty('pattern')) {
                    validators.push(Validators.pattern((itemModel as ApiModel).pattern));
                }
                group[key] = new FormControl( value, validators);
            }
            return new FormGroup(group);
        }
    }
}
