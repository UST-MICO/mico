import { Component, forwardRef, OnInit, Input, ViewChildren, AfterViewInit } from '@angular/core';
import { MatFormFieldControl } from '@angular/material';
import { NG_VALUE_ACCESSOR,  AsyncValidator, NG_VALIDATORS } from '@angular/forms';
import { ApiModel } from 'src/app/api/apimodel';
import { ModelsService } from 'src/app/api/models.service';
import { MicoFormComponent } from '../mico-form/mico-form.component';
import { combineLatest, Observable, BehaviorSubject, Subject, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';


@Component({
    selector: 'mico-form-array',
    templateUrl: './mico-form-array.component.html',
    styleUrls: ['./mico-form-array.component.css'],
    providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => MicoFormArrayComponent),
        multi: true
    }, {provide: NG_VALIDATORS, useExisting: forwardRef(() => MicoFormArrayComponent), multi: true}
    , { provide: MatFormFieldControl, useExisting: Boolean }],
})
export class MicoFormArrayComponent implements OnInit, AfterViewInit, AsyncValidator {


    constructor(private models: ModelsService) { }

    @ViewChildren(MicoFormComponent) forms;

    lastValidSub: Subscription;
    valid: Subject<boolean> = new BehaviorSubject<boolean>(false);
    currentValue: any[] = [];

    @Input() config: ApiModel;
    nestedModel: ApiModel;

    onChange: any = () => { };

    onTouched: any = () => { };

    validator: any = () => { };

    get value(): any[] {
        return this.currentValue;
    }

    set value(val: any[]) {
        this.currentValue = val;
        this.onChange(val);
        this.onTouched();
    }

    trackBy(index) {
        return index
    }

    updateElement(index, element) {
        if (index >= this.currentValue.length || index < 0) {
            return;
        }
        this.currentValue[index] = element;
        this.onChange(this.value);
        this.onTouched();
    }

    removeElement(index) {
        if (index >= this.currentValue.length || index < 0) {
            return;
        }
        this.currentValue.splice(index, 1);
        this.onChange(this.value);
        this.onTouched();
    }

    addElement() {
        this.currentValue.push({});
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

    validate() {
        return this.valid.pipe(
            map((valid) => {
                if (valid) {
                    return null;
                } else {
                    return {nestedError: 'A nested form has an error.'};
                }
            }),
        );
    }


    ngOnInit() {
        console.log(this.config)
        const modelUrl = this.config.items.$ref;
        this.models.getModel(modelUrl).subscribe(model => {
            this.nestedModel = model;
        });
    }

    ngAfterViewInit() {
        this.forms.changes.subscribe((forms) => {
            const micoForms: MicoFormComponent[] = forms._results;
            const validObservables: Observable<boolean>[] = []
            micoForms.forEach((form) => {
                validObservables.push(form.valid.asObservable());
            });
            if (this.lastValidSub != null) {
                this.lastValidSub.unsubscribe();
            }
            this.lastValidSub = combineLatest(...validObservables).pipe(
                map((values) => {
                    return !values.some(value => !value);
                }),
            ).subscribe((valid) => this.valid.next(valid));
        })
    }

}
