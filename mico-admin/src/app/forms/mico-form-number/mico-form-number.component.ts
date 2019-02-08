import { Component, forwardRef, OnInit, Input } from '@angular/core';
import { MatFormFieldControl } from '@angular/material';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { ApiModel } from 'src/app/api/apimodel';

@Component({
    selector: 'mico-form-number',
    templateUrl: './mico-form-number.component.html',
    styleUrls: ['./mico-form-number.component.css'],
    providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => MicoFormNumberComponent),
        multi: true
    }, { provide: MatFormFieldControl, useExisting: Number }],
})

export class MicoFormNumberComponent implements OnInit {

    constructor() { }

    content: number;
    @Input() config: ApiModel;

    ngOnInit() {
    }

    onChange: any = () => { };

    onTouched: any = () => { };

    get value(): number {
        return this.content;
    }

    set value(val: number) {
        if (!Number.isNaN(val)) {
            this.content = val;
            this.onChange(val);
            this.onTouched();
        }
    }

    onInputChange(input: number) {
        if (input == null) {
            return;
        }
        this.content = input;
        this.onChange(input);
        this.onTouched();
    }

    registerOnChange(fn) {
        this.onChange = fn;
    }

    registerOnTouched(fn) {
        this.onTouched = fn;
    }

    writeValue(value) {
        const temp: number = Number(value);
        if (!Number.isNaN(temp)) {
            this.value = temp;
        }
    }
}
