import { Injectable } from '@angular/core';
import { AsyncSubject, Observable, of, from } from 'rxjs';
import { ApiModel, ApiModelAllOf, ApiModelRef } from './apimodel';
import { concatMap, reduce, first, timeout } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class ModelsService {

    private modelCache: Map<string, AsyncSubject<ApiModel>> = new Map<string, AsyncSubject<ApiModel>>();

    private localModels: {[property: string]: ApiModelAllOf|ApiModel} = {
        'servicePOST': {
            'type': 'object',
            'properties': {
                'name': {
                    'type': 'string',
                    'x-order': 1,
                },
                'description': {
                    'type': 'string',
                    'x-order': 2,
                }
            }
        },
        'servicePUT': {
            'allOf': [
                {
                    '$ref': 'local/servicePOST',
                },
            ]
        },
        'applicationPOST': {
            'allOf': [
                {
                    '$ref': 'local/servicePOST',
                },
            ]
        }
    }

    constructor() { }

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
    private resolveModel(modelUrl: string): Observable<ApiModelAllOf|ApiModel> {
        modelUrl = this.canonizeModelUri(modelUrl);
        if (modelUrl.startsWith('local/')) {
            const modelID = modelUrl.substring(6);
            const model = this.localModels[modelID];
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
    private resolveModelLinks(model: ApiModelAllOf|ApiModelRef|ApiModel): Observable<ApiModel> {
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
    private mergeModels(targetModel: ApiModel, sourceModel: ApiModel): ApiModel {
        if (targetModel == null) {
            // return next in line
            return sourceModel;
        }
        if (sourceModel != null) {
            // merge models
            for (const key in sourceModel) {
                if (key == 'required') {
                    // merge reqired attributes list
                    if (targetModel[key] != null) {
                        let required = new Set<string>(targetModel[key]);
                        sourceModel[key].forEach(required.add);
                        targetModel[key] = Array.from(required);
                    }
                } else if (key == 'properties') {
                    // merge nested models in properties
                    if (targetModel[key] != null) {
                        const targetProperties = targetModel[key];
                        const sourceProperties = sourceModel[key];
                        for (const attrKey in sourceModel[key]) {
                            if (targetProperties[attrKey] != null) {
                                const target = targetProperties[attrKey];
                                const source = sourceProperties[attrKey];
                                if (target.$ref != null && source.$ref != null) {
                                    target.$ref = source.$ref;
                                } else if (target.$ref === undefined && source.$ref === undefined) {
                                    targetProperties[attrKey] = this.mergeModels(target as ApiModel, source as ApiModel);
                                }
                            }
                        }
                    }
                }
                targetModel[key] = sourceModel[key];
            }
        }
        return targetModel;
    }

    /**
     * Fetch the cache source for the given model url.
     *
     * @param cacheUrl resource url
     */
    private getCacheSource(cacheURL: string): AsyncSubject<ApiModel> {
        cacheURL = this.canonizeModelUri(cacheURL);
        let stream = this.modelCache.get(cacheURL);
        if (stream == null) {
            stream = new AsyncSubject<ApiModel>();
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
     * @param modelUrl
     */
    getModel(modelUrl): Observable<ApiModel> {
        const stream = this.getCacheSource(modelUrl);
        if (! stream.closed) {
            this.resolveModel(modelUrl).pipe(
                concatMap(this.resolveModelLinks),
                reduce(this.mergeModels, null),
                first(),
            ).subscribe((model) => {
                if (model != null) {
                    stream.next(model);
                    stream.complete();
                }
            });
        }
        return stream.asObservable().pipe(
            timeout(2000),
            first(),
        );
    }
}
