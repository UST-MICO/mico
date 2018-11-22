import { Component, OnInit, Input, OnChanges, SimpleChanges, EventEmitter, Output } from '@angular/core';
import { ModelsService } from '../../api/models.service';
import { ApiModel, ApiModelRef } from '../../api/apimodel';
import { FormGroup } from '@angular/forms';
import { FormGroupService } from '../form-group.service';
import { map, first } from 'rxjs/operators';
import { Subscription } from 'rxjs';

@Component({
  selector: 'mico-form',
  templateUrl: './mico-form.component.html',
  styleUrls: ['./mico-form.component.css']
})
export class MicoFormComponent implements OnInit, OnChanges {

    @Input() modelUrl: string;
    @Input() filter: string[] = [];
    @Input() isBlacklist: boolean = false;
    @Input() debug: boolean = false;

    @Output() valid: EventEmitter<boolean> = new EventEmitter<boolean>();
    @Output() data: EventEmitter<any> = new EventEmitter<any>();

    model: ApiModel;
    properties: (ApiModel | ApiModelRef)[];
    form: FormGroup;

    private formSubscription: Subscription;

    constructor(private models: ModelsService, private formGroup: FormGroupService) { }

    ngOnInit() {}

    ngOnChanges(changes: SimpleChanges) {
        if (changes.modelUrl != null || changes.filter != null || changes.isBlacklist != null) {
            this.models.getModel(this.modelUrl).pipe(
                map(this.models.filterModel(this.filter, this.isBlacklist)),
                first(),
            ).subscribe(model => {
                const props = [];
                if (model.properties != null) {
                    for (const key in model.properties) {
                        props.push(model.properties[key]);
                    }
                }
                this.model = model;
                this.properties = props;
                this.form = this.formGroup.modelToFormGroup(model);
                if (this.formSubscription != null) {
                    this.formSubscription.unsubscribe();
                }
                this.formSubscription = this.form.statusChanges.subscribe(status => {
                    this.valid.emit(this.form.valid);
                    this.data.emit(this.form.value);
                });
            });
        }
    }

}
