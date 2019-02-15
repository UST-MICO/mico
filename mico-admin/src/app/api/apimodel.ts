
export interface ApiModelRef {
    $ref: string;
}

export interface ApiModel {
    type: string;
    properties?: {[propName: string]: ApiModel | ApiModelRef};
    required?: string[];
    title?: string;
    description?: string;
    items?: ApiModel | ApiModelRef
    [propName: string]: any;
}

export interface ApiModelAllOf {
    allOf: (ApiModel|ApiModelRef)[];
}
