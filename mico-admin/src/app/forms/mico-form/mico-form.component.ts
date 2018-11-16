import { Component, OnInit, Input } from '@angular/core';
import { ModelsService } from 'src/app/api/models.service';
import { ApiModel, ApiModelRef } from 'src/app/api/apimodel';
import { Observable } from 'rxjs';
import { FormGroup } from '@angular/forms';
import { FormGroupService } from '../form-group.service';
import { map, first } from 'rxjs/operators';

@Component({
  selector: 'mico-form',
  templateUrl: './mico-form.component.html',
  styleUrls: ['./mico-form.component.css']
})
export class MicoFormComponent implements OnInit {

    @Input() modelUrl: string;

    model: Observable<ApiModel>;
    properties: (ApiModel | ApiModelRef)[];
    form: FormGroup;

    constructor(private models: ModelsService, private formGroup: FormGroupService) { }

    ngOnInit() {
        this.model = this.models.getModel(this.modelUrl);
        this.model.subscribe(model => {
            const props = [];
            if (model.properties != null) {
                for (const key in model.properties) {
                    props.push(model.properties[key]);
                }
            }
            this.properties = props;
        });
        this.model.pipe(
            map(this.formGroup.modelToFormGroup),
            first(),
        ).subscribe(group => this.form = group);
    }

}
