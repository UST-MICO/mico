import { Component, forwardRef, OnInit, Input } from '@angular/core';
import { MatFormFieldControl } from '@angular/material';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { ApiModel } from 'src/app/api/apimodel';

@Component({
    selector: 'mico-form-boolean',
    templateUrl: './mico-form-boolean.component.html',
    styleUrls: ['./mico-form-boolean.component.css'],
    providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => MicoFormBooleanComponent),
        multi: true
    }, { provide: MatFormFieldControl, useExisting: Boolean }],
})

export class MicoFormBooleanComponent implements OnInit {

    constructor() { }

    checked: Boolean = false;
    @Input() config: ApiModel;

    onChange: any = () => { };

    onTouched: any = () => { };

    get value(): boolean {
        return !(!this.checked);
    }

    set value(val: boolean) {
        this.checked = val;
        this.onChange(val);
        this.onTouched();
    }

    onClick() {
        this.checked = !this.checked;
        this.onChange(this.value);
        this.onTouched();
    }

    registerOnChange(fn) {
        this.onChange = fn;
    }

    registerOnTouched(fn) {
        this.onTouched = fn;
    }

    writeValue(value) {
        if (value) {
            this.value = value;
        }
    }


    ngOnInit() {
    }

}
