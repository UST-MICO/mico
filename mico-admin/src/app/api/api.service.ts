import { Injectable } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { ApiObject } from './apiobject';
import { ApiBaseFunctionService } from './api-base-function.service';


/**
 * Recursively freeze object.
 *
 * ONLY use this if you are sure what objects this will freeze!
 *
 * @param obj the object to freeze
 */
export function freezeObject<T>(obj: T): Readonly<T> {
    if (Object.isFrozen(obj)) { return; }
    const propNames = Object.getOwnPropertyNames(obj);
    // Freeze properties before freezing self
    for (const key of propNames) {
        const value = obj[key];
        if (value && typeof value === 'object') {
            obj[key] = freezeObject(value);
        } else {
            obj[key] = value;
        }
    }
    return Object.freeze(obj);
}


/**
 * Service to interact with the mico api.
 */
@Injectable({
    providedIn: 'root'
})
export class ApiService {
    private streams: Map<string, Subject<Readonly<ApiObject> | Readonly<ApiObject[]>>> = new Map();

    constructor(private rest: ApiBaseFunctionService, ) { }

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
    private getStreamSource(streamURL: string, defaultSubject: () => Subject<Readonly<ApiObject> | Readonly<ApiObject[]>> =
        () => new BehaviorSubject<Readonly<ApiObject> | Readonly<ApiObject[]>>(undefined)
    ): Subject<Readonly<ApiObject> | Readonly<ApiObject[]>> {
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
    getServices(): Observable<Readonly<ApiObject[]>> {
        const resource = 'services';
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            // return actual service list
            stream.next(freezeObject((val as ApiObject)._embedded.serviceList));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject[]>>).pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * Get application list
     */
    getApplications(): Observable<Readonly<ApiObject[]>> {
        const resource = 'applications';
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

        stream.next(freezeObject(mockData));

        return (stream.asObservable() as Observable<Readonly<ApiObject[]>>).pipe(
            filter(data => data !== undefined)
        );
    }

    getApplicationById(id): Observable<Readonly<ApiObject>> {
        // TODO check if there is a resource for single applications
        const resource = 'applications';
        const stream = this.getStreamSource(resource);

        // TODO
        const mockData: ApiObject = {
            'id': id,
            'name': 'Hello World Application id ' + id,
            'shortName': 'test.' + id + 'application',
            'description': 'A generic application',
        };

        stream.next(mockData);

        return (stream.asObservable() as Observable<ApiObject[]>).pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * Get all versions of a service based on its shortName
     */
    getService(shortName): Observable<ApiObject> {

        const resource = 'services/' + shortName + '/';
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            stream.next(freezeObject((val as ApiObject)._embedded.serviceList));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject[]>>).pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * Get a specific version of a service
     *
     * @param shortName unique short name of the service
     * @param version service version to be returned
     */
    getService(shortName, version): Observable<ApiObject> {

        const resource = 'services/' + shortName + '/' + version;
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            stream.next(freezeObject((val as ApiObject)));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject[]>>).pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * Get all services a specific service depends on.
     *
     * @param shortName unique short name of the service
     * @param version service version to be returned
     */
    getServiceDependees(shortName, version): Observable<ApiObject> {

        const resource = 'services/' + shortName + '/' + version + '/dependees';
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            stream.next(freezeObject((val as ApiObject)));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject[]>>).pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * Get all services depending on a specific service
     *
     * @param shortName unique short name of the service
     * @param version service version to be returned
     */
    getServiceDependers(shortName, version): Observable<ApiObject> {

        const resource = 'services/' + shortName + '/' + version + '/dependers';
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            stream.next(freezeObject((val as ApiObject)));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject[]>>).pipe(
            filter(data => data !== undefined)
        );
    }

    getServiceInterfaces(shortName, version): Observable<ApiObject[]> {
        const resource = 'services/' + shortName + '/' + version + '/interfaces';
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            stream.next(freezeObject((val as ApiObject)));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject[]>>).pipe(
            filter(data => data !== undefined)
        );
    }
}
