import { Injectable } from '@angular/core';
import { AsyncSubject, Observable } from 'rxjs';
import { ApiModel, ApiModelAllOf } from './apimodel';

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

    private canonizeModelUri(modelUri: string): string {
        // TODO implement
        return modelUri;
    }

    /**
     * Fetch the cache source for the given model url.
     *
     * @param modelUrl resource url
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

    getModel(modelUrl): Observable<ApiModel> {
        const stream = this.getCacheSource(modelUrl);
        if (! stream.closed) {
            // TODO fetch and sqash model
        }
        return stream.asObservable();
    }
}
