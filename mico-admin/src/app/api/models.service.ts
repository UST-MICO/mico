import { Injectable } from '@angular/core';
import { AsyncSubject, Observable, of, from, Subscription } from 'rxjs';
import { ApiModel, ApiModelAllOf, ApiModelRef } from './apimodel';
import { concatMap, reduce, first, timeout, map } from 'rxjs/operators';
import { ApiService, freezeObject } from './api.service';

@Injectable({
    providedIn: 'root'
})
export class ModelsService {

    private remoteModels;

    constructor(private apiService: ApiService, ) {
        // TODO consider unsubscribing
        /* TODO Consider using the Observable directly instead of storing the provided value to avoid cases,
        * where remoteModels is still null but a model is already requested by the ui.
        * TBD after the UI for the minimal example is done
        */
        apiService.getModelDefinitions().subscribe(val => {
            this.remoteModels = val;
        });
    }

    private modelCache: Map<string, AsyncSubject<ApiModel>> = new Map<string, AsyncSubject<ApiModel>>();

    private localModels: { [property: string]: ApiModelAllOf | ApiModel } = {
        'serviceFromGitPOST': {
            'type': 'object',
            'properties': {
                'vcsroot': {
                    'type': 'string',
                    'x-order': 10,
                    'pattern': '(https?://)?github\.com(/[a-zA-Z0-9-]+)+/?',
                }
            },
            'required': ['name'],
        },
        'servicePOST': {
            'type': 'object',
            'properties': {
                'name': {
                    'type': 'string',
                    'title': 'Name',
                    'x-order': 10,
                    'minLength': 3,
                    'maxLength': 22,
                },
                'description': {
                    'title': 'Description',
                    'type': 'string',
                    'x-order': 20,
                }
            },
            'required': ['name'],
        },
        'servicePUT': {
            'allOf': [
                {
                    '$ref': 'local/servicePOST',
                },
                {
                    'type': 'object',
                    'properties': {
                        'name': {
                            'type': 'string',
                            'x-order': 10
                        },
                        'vcsRoot': {
                            'title': 'VCS Root',
                            'type': 'string',
                            'x-order': 20
                        },
                        'dockerfile': {
                            'title': 'Dockerfile',
                            'type': 'string',
                            'x-order': 20
                        },
                        'documentation': {
                            'title': 'Documentation',
                            'type': 'string',
                            'x-order': 30
                        },
                        'owner': {
                            'title': 'Owner',
                            'type': 'string',
                            'x-order': 40
                        },
                        'contact': {
                            'title': 'Contact',
                            'type': 'string',
                            'x-order': 50
                        },
                        'externalService': {
                            'title': 'Is external service',
                            'type': 'boolean',
                            'x-order': 60
                        },
                    }
                }
            ]
        },
        'applicationPOST': {
            'allOf': [
                {
                    '$ref': 'local/servicePOST',
                },
            ]
        },
        'editService': {
            'type': 'object',
            'properties': {
                'Name': {
                    'type': 'string',
                    'x-order': 10
                },
                'VCS Root': {
                    'type': 'string',
                    'x-order': 20
                },
                'Dockerfile': {
                    'type': 'string',
                    'x-order': 20
                },
                'Documentation': {
                    'type': 'string',
                    'x-order': 30
                },
                'Owner': {
                    'type': 'string',
                    'x-order': 40
                },
                'Contact': {
                    'type': 'string',
                    'x-order': 50
                },
                'external service': {
                    'type': 'boolean',
                    'x-order': 60
                },
            }
        },
        'serviceInterfacePOST': {
            'type': 'object',
            'properties': {
                'Name': {
                    'type': 'string',
                    'x-order': 10
                },
                'Description': {
                    'type': 'string',
                    'x-order': 20
                },
                'Port': {
                    'type': 'string',
                    'x-order': 30
                },
                'Protocol': {
                    'type': 'string',
                    'x-order': 40
                },
                'Transport-Protocol': {
                    'type': 'string',
                    'x-order': 50
                },
                'Public-DNS': {
                    'type': 'string',
                    'x-order': 60
                },

            }
        },
        'micoDataViewTEST': {
            'type': 'object',
            'properties': {
                'micoDataViewTest': {
                    'type': 'string',
                    'x-order': 10
                },
                'numberEntry': {
                    'type': 'number',
                    'x-order': 20
                },
                'booleanEntry': {
                    'type': 'boolean',
                    'x-order': 20,
                    'x-boolean': 'YesNo',
                },
            }
        }
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
            const modelID = modelUrl.substring(6);
            // deep clone model because they will be frozen later...
            const model = JSON.parse(JSON.stringify(this.localModels[modelID]));
            return of(model);
        } else if (modelUrl.startsWith('remote/')) {

            const modelID = modelUrl.substring(7);

            const model = JSON.parse(JSON.stringify(this.remoteModels[modelID]));
            return of(model);

        }
        return of(null); // TODO load models from openapi definitions
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
                            newProps[propKey] = Object.assign(new Object(), props[propKey]);
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
