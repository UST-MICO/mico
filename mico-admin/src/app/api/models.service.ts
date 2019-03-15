/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { Injectable } from '@angular/core';
import { AsyncSubject, Observable, of, from } from 'rxjs';
import { ApiModel, ApiModelAllOf, ApiModelRef } from './apimodel';
import { concatMap, reduce, first, timeout, map, flatMap } from 'rxjs/operators';
import { ApiService, freezeObject } from './api.service';

interface PropertyRef {
    key: string;
    parent?: ApiModel;
    prop: ApiModel | ApiModelRef;
}

const numericPropertyKeys = new Set(['minLength', 'maxLength', 'minimum', 'maximum', 'minItems', 'maxItems', 'x-order']);

@Injectable({
    providedIn: 'root'
})
export class ModelsService {

    constructor(private apiService: ApiService, ) { }

    private modelCache: Map<string, AsyncSubject<ApiModel>> = new Map<string, AsyncSubject<ApiModel>>();

    private nestedModelCache: Map<string, ApiModel> = new Map<string, ApiModel>();

    // models can be hard coded here if necessary
    private localModels: { [property: string]: ApiModelAllOf | ApiModel } = {

    };



    /**
     * Canonize a resource url.
     *
     * (Remove schema, host, port, api path prefix and leading/trailing slashes.)
     *
     * @param modelURL resource url
     */
    private canonizeModelUri(modelUri: string): string {
        // TODO implement
        modelUri = modelUri.replace(/^#\/definitions/, 'remote');
        return modelUri;
    }

    /**
     * Resolve the modelUrl and return the corresponding model.
     *
     * @param modelUrl resource url
     */
    private resolveModel = (modelUrl: string): Observable<ApiModelAllOf | ApiModel> => {
        modelUrl = this.canonizeModelUri(modelUrl);

        if (modelUrl.startsWith('local/')) {
            // remove 'local/'
            const modelID = modelUrl.substring(6);
            // deep clone model because they will be frozen later...
            const model = JSON.parse(JSON.stringify(this.localModels[modelID]));
            return of(model);

        } else if (modelUrl.startsWith('remote/')) {

            // remove 'remote/'
            const modelID = modelUrl.substring(7);

            // retrieve models from swagger api
            return this.apiService.getModelDefinitions().pipe(
                map(remoteModels => {
                    return JSON.parse(JSON.stringify(remoteModels[modelID]));
                })
            );
        } else if (modelUrl.startsWith('nested/')) {

            // remove 'nested/'
            const modelID = modelUrl.substring(7);

            const model = JSON.parse(JSON.stringify(this.nestedModelCache.get(modelID)));

            return of(model);
        }
        return of(null);
    }

    /**
     * Resolve all model links and return an observable of pure ApiModels
     *
     * starting with the first model of an allOf and recursively applying itself
     *
     * @param model input model
     */
    private resolveModelLinks = (model: ApiModelAllOf | ApiModelRef | ApiModel): Observable<ApiModel> => {
        if ((model as ApiModelAllOf).allOf != null) {
            const models = (model as ApiModelAllOf).allOf;
            return from(models).pipe(concatMap(this.resolveModelLinks));
        } else if ((model as ApiModelRef).$ref != null) {
            return this.resolveModel((model as ApiModelRef).$ref).pipe(concatMap(this.resolveModelLinks));
        } else {
            return of(model as ApiModel);
        }
    }

    /**
     * Merge two ApiModels into one model.
     *
     * @param targetModel the model to be merged into
     * @param sourceModel the model to be merged
     */
    private mergeModels = (targetModel: ApiModel, sourceModel: ApiModel): ApiModel => {
        if (targetModel == null) {
            // return next in line
            return sourceModel;
        }
        if (sourceModel != null) {
            // merge models
            for (const key in sourceModel) {
                if (!sourceModel.hasOwnProperty(key)) {
                    continue;
                }
                if (key === 'required') {
                    // merge reqired attributes list
                    if (targetModel[key] != null) {
                        const required = new Set<string>(targetModel[key]);
                        sourceModel[key].forEach(required.add.bind(required));
                        targetModel[key] = Array.from(required);
                    }
                } else if (key === 'properties') {
                    // merge nested models in properties
                    if (targetModel[key] != null) {
                        const targetProperties = targetModel[key];
                        const sourceProperties = sourceModel[key];
                        for (const attrKey in sourceProperties) {
                            if (targetProperties[attrKey] != null) {
                                const target = targetProperties[attrKey];
                                const source = sourceProperties[attrKey];
                                if (target.$ref != null && source.$ref != null) {
                                    target.$ref = source.$ref;
                                } else if (target.$ref === undefined && source.$ref === undefined) {
                                    targetProperties[attrKey] = this.mergeModels(target as ApiModel, source as ApiModel);
                                }
                            } else {
                                targetProperties[attrKey] = sourceProperties[attrKey];
                            }
                        }
                    }
                } else {
                    targetModel[key] = sourceModel[key];
                }
            }
        }
        return targetModel;
    }

    /**
     * Handle object type properties.
     *
     * Replaces prop with ApiModelRef to nestedModelCache if needed
     *
     * @param property input PropertyRef
     */
    private handleObjectProperties = (property: PropertyRef): Observable<PropertyRef> => {
        if (!property.prop.hasOwnProperty('type') || (property.prop as ApiModel).type !== 'object') {
            return of(property);
        }

        let key: string;
        if (property.parent != null && property.parent.title != null) {
            key = `${property.parent.title}.${property.key}`;
        } else {
            key = `${property.key}-${Date.now()}`;
        }
        this.nestedModelCache.set(key, (property.prop as ApiModel));

        return of({
            key: property.key,
            parent: property.parent,
            prop: {
                $ref: `nested/${key}`,
            },
        });
    }


    /**
     * Handle array type properties.
     *
     * Replaces items with ApiModelRef to nestedModelCache if needed
     *
     * @param property input PropertyRef
     */
    private handleArrayProperties = (property: PropertyRef): Observable<PropertyRef> => {
        if (!property.prop.hasOwnProperty('type') || (property.prop as ApiModel).type !== 'array') {
            return of(property);
        }

        const items = (property.prop as ApiModel).items;
        if (items.$ref != null) {
            return of(property);
        }
        let key: string;
        if (property.parent != null && property.parent.title != null) {
            key = `${property.parent.title}.${property.key}`;
        } else {
            key = `${property.key}-${Date.now()}`;
        }
        this.nestedModelCache.set(key, (items as ApiModel));
        const propCopy = JSON.parse(JSON.stringify(property.prop));
        propCopy.items = {
            $ref: `nested/${key}`,
        };

        return of({
            key: property.key,
            parent: property.parent,
            prop: propCopy,
        });
    }

    /**
     * Convert all property keys that should have numeric values (like 'minimum').
     *
     * @param property input PropertyRef
     */
    private handleNumericPropertyKeys = (property: PropertyRef): Observable<PropertyRef> => {
        const propCopy = JSON.parse(JSON.stringify(property.prop));

        for (const key in propCopy) {
            if (propCopy.hasOwnProperty(key)) {
                if (numericPropertyKeys.has(key)) {
                    propCopy[key] = parseFloat(propCopy[key]);
                }
            }
        }

        return of({
            key: property.key,
            parent: property.parent,
            prop: propCopy,
        });
    }

    /**
     * Check all properties of model for complex properties like arrays or objects.
     *
     * Replaces all nested models with ApiModelRefs to nestedModelCache
     *
     * @param model input model
     */
    private handleComplexProperties = (model: ApiModel): Observable<ApiModel> => {
        const props: PropertyRef[] = [];
        for (const key in model.properties) {
            if (model.properties.hasOwnProperty(key)) {
                const prop = model.properties[key];
                props.push({ key: key, prop: prop, parent: model });
            }
        }
        return of(...props).pipe(
            flatMap(this.handleNumericPropertyKeys),
            flatMap(this.handleObjectProperties),
            flatMap(this.handleArrayProperties),
            reduce((properties, prop: PropertyRef) => {
                properties[prop.key] = prop.prop;
                return properties;
            }, {}),
            map((properties) => {
                model.properties = properties;
                return model;
            }),
        );
    }



    /**
     * Fetch the cache source for the given model url.
     *
     * @param cacheUrl resource url
     */
    private getCacheSource = (cacheURL: string): AsyncSubject<Readonly<ApiModel>> => {
        cacheURL = this.canonizeModelUri(cacheURL);
        let stream = this.modelCache.get(cacheURL);
        if (stream == null) {
            stream = new AsyncSubject<Readonly<ApiModel>>();
            this.modelCache.set(cacheURL, stream);
        }
        return stream;
    }

    /**
     * Get a model for the modelUrl.
     *
     * Observable only sends a value if the model was found.
     * Times out after 2s
     *
     * @param modelUrl modelUrl
     */
    getModel = (modelUrl): Observable<Readonly<ApiModel>> => {
        const stream = this.getCacheSource(modelUrl);
        if (!stream.closed) {
            this.resolveModel(modelUrl).pipe(
                concatMap(this.resolveModelLinks),
                reduce(this.mergeModels, null),
                flatMap(this.handleComplexProperties),
                map(model => {
                    // inject name into properties
                    if (model.properties != null) {
                        for (const key in model.properties) {
                            if (!model.properties.hasOwnProperty(key)) {
                                continue;
                            }
                            if (model.properties[key]['title'] == null) {
                                // set title from key if unset
                                model.properties[key]['title'] = key;
                            }
                            model.properties[key]['x-key'] = key;
                        }
                    }
                    // inject required information into property
                    if (model.required != null && model.properties != null) {
                        model.required.forEach(key => {
                            const prop = model.properties[key];
                            if (prop != null) {
                                prop['x-required'] = true;
                            }
                        });
                    }
                    return model;
                }),
                first(),
            ).subscribe((model) => {
                if (model != null) {
                    stream.next(freezeObject(model));
                    stream.complete();
                }
            });
        }
        return stream.asObservable().pipe(
            timeout(2000),
            first(),
        );
    }

    /**
     * Return a stream filter for api models. (use with map in observable pipe)
     *
     * @param properties the property keys to filter for (array/set or other iterable)
     *                   Use Empty iterable or null to deactivate filter
     * @param isBlacklist if true the filter ill be appliead as blacklist. (default=whitelest/false)
     */
    filterModel(properties: Iterable<string>, isBlacklist: boolean = false): (ApiModel) => Readonly<ApiModel> {
        const filterset: Set<string> = (properties !== null) ? new Set<string>(properties) : new Set<string>();
        return (model) => {
            if (filterset.size === 0) { return model; }
            const newModel: ApiModel = { type: model.type };
            for (const key in model) {
                if (key === 'type') {
                    continue;
                } else if (key === 'properties') {
                    const props = model[key];
                    const newProps: any = {};
                    for (const propKey in props) {
                        if ((isBlacklist && !filterset.has(propKey)) ||
                            (!isBlacklist && filterset.has(propKey))) {
                            // create new object because direct assignement fails (except for the debugger...)
                            newProps[propKey] = JSON.parse(JSON.stringify(props[propKey]));
                        }
                    }
                    newModel[key] = newProps;
                    continue;
                } else if (key === 'required') {
                    if (isBlacklist) {
                        newModel[key] = model[key].filter((propKey) => !filterset.has(propKey));
                    } else {
                        newModel[key] = model[key].filter((propKey) => filterset.has(propKey));
                    }
                    continue;
                }
                newModel[key] = model[key];
            }
            return freezeObject(newModel);
        };
    }
}
