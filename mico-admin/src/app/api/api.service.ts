import { Injectable } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { ApiObject } from './apiobject';


/**
 * Service to interact with the mico api.
 */
@Injectable({
  providedIn: 'root'
})
export class ApiService {
    private streams: Map<string, Subject<ApiObject | ApiObject[]>> = new Map();

    constructor() { }

    /**
     * Canonize a resource url.
     *
     * (Remove schema, host, port, api path prefix and leading/trailing slashes.)
     *
     * @param streamURL resource url
     */
    private canonizeStreamUrl(streamURL: string): string {
        // TODO implement
        return streamURL;
    }

    /**
     * Fetch the stream source for the given resource url.
     *
     * @param streamURL resource url
     * @param defaultSubject function to create a new streamSource if needed (default: BehaviourSubject)
     */
    private getStreamSource(streamURL: string, defaultSubject: () => Subject<ApiObject | ApiObject[]> =
                            () => new BehaviorSubject<ApiObject | ApiObject[]>(undefined)
                           ): Subject<ApiObject | ApiObject[]> {
        streamURL = this.canonizeStreamUrl(streamURL);
        let stream = this.streams.get(streamURL);
        if (stream == null) {
            stream = defaultSubject();
            if (stream != null) {
                this.streams.set(streamURL, stream);
            }
        }
        return stream;
    }

    /**
     * Get service list
     */
    getServices(): Observable<ApiObject[]> {
        let resource = 'services';
        const stream = this.getStreamSource(resource);

        // TODO
        const mockData: ApiObject[] = [
            {
                'id': '1',
                'name': 'Mock Service',
                'shortName': 'test.mock-service',
                'description': 'A generic dummy service',
            },
            {
                'id': '2',
                'name': 'Hello World Service',
                'shortName': 'test.hello-world-service',
                'description': 'A generic hello world service',
            },
        ];

        stream.next(mockData);

        return (stream.asObservable() as Observable<ApiObject[]>).pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * Get application list
     */
    getApplications(): Observable<ApiObject[]> {
        let resource = 'applications';
        const stream = this.getStreamSource(resource);

        // TODO
        const mockData: ApiObject[] = [
            {
                'id': '2',
                'name': 'Hello World Service',
                'shortName': 'test.hello-world-service',
                'description': 'A generic hello world service',
            },
            {
                'id': '4',
                'name': 'Bye  Service',
                'shortName': 'test.bye-service',
                'description': 'A generic service',
            },
        ];

        stream.next(mockData);

        return (stream.asObservable() as Observable<ApiObject[]>).pipe(
            filter(data => data !== undefined)
        );
    }

    getApplicationById(id): Observable<ApiObject> {
        // TODO check if there is a resource for single applications
        let resource = 'applications';
        const stream = this.getStreamSource(resource);

        // TODO
        const mockData: ApiObject =
            {
                'id': id,
                'name': 'Hello World Service',
                'shortName': 'test.' + id + 'service',
                'description': 'A generic service',
            }

        stream.next(mockData);

        return (stream.asObservable() as Observable<ApiObject[]>).pipe(
            filter(data => data !== undefined)
        );
    }
}
