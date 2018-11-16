import { Component, OnInit, Input } from '@angular/core';
import { ModelsService } from 'src/app/api/models.service';
import { ApiModel, ApiModelRef } from 'src/app/api/apimodel';
import { Observable } from 'rxjs';

@Component({
  selector: 'mico-form',
  templateUrl: './mico-form.component.html',
  styleUrls: ['./mico-form.component.css']
})
export class MicoFormComponent implements OnInit {

    @Input() modelUrl: string;

    model: Observable<ApiModel>;
    properties: (ApiModel | ApiModelRef)[];

    constructor(private models: ModelsService) { }

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
    }

}
