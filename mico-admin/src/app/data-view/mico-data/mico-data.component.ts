import { Component, OnInit, Input, OnChanges, SimpleChanges, EventEmitter, Output } from '@angular/core';
import { ModelsService } from '../../api/models.service';
import { ApiModel, ApiModelRef } from '../../api/apimodel';
import { map, first } from 'rxjs/operators';
import { Subscription } from 'rxjs';

@Component({
    selector: 'mico-data',
    templateUrl: './mico-data.component.html',
    styleUrls: ['./mico-data.component.css']
})
export class MicoDataComponent implements OnInit, OnChanges {

    @Input() modelUrl: string;
    @Input() filter: string[] = [];
    @Input() isBlacklist: boolean = false;
    @Input() debug: boolean = false;
    @Input() set startData(data: { [prop: string]: any }) {
        this._startData = data;
    }

    @Output() valid: EventEmitter<boolean> = new EventEmitter<boolean>();
    @Output() data: EventEmitter<any> = new EventEmitter<any>();

    model: ApiModel;
    properties: (ApiModel | ApiModelRef)[];

    _startData: { [prop: string]: any };

    private formSubscription: Subscription;

    constructor(private models: ModelsService) { }

    ngOnInit() { }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.modelUrl != null || changes.filter != null || changes.isBlacklist != null) {
            this.models.getModel(this.modelUrl).pipe(
                map(this.models.filterModel(this.filter, this.isBlacklist)),
                first(),
            ).subscribe(model => {
                const props = [];
                if (model.properties != null) {
                    for (const key in model.properties) {
                        if (!model.properties.hasOwnProperty(key)) {
                            continue;
                        }
                        props.push(model.properties[key]);
                    }
                }
                this.model = model;
                this.properties = props.sort((a, b) => {
                    const orderA = a['x-order'] != null ? a['x-order'] : 0;
                    const orderB = b['x-order'] != null ? b['x-order'] : 0;
                    return orderA - orderB;
                });

                if (this.formSubscription != null) {
                    this.formSubscription.unsubscribe();
                }
            });
        }
    }

    trackByFn(index, item) {
        return item['x-key'];
    }

}
