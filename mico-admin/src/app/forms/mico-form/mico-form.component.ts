import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ModelsService } from 'src/app/api/models.service';
import { ApiModel, ApiModelRef } from 'src/app/api/apimodel';
import { FormGroup } from '@angular/forms';
import { FormGroupService } from '../form-group.service';
import { map, first } from 'rxjs/operators';

@Component({
  selector: 'mico-form',
  templateUrl: './mico-form.component.html',
  styleUrls: ['./mico-form.component.css']
})
export class MicoFormComponent implements OnInit, OnChanges {

    @Input() modelUrl: string;
    @Input() filter: string[] = [];
    @Input() isBlacklist: boolean = false;

    model: ApiModel;
    properties: (ApiModel | ApiModelRef)[];
    form: FormGroup;

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
            });
        }
    }

}
