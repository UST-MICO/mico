import { Injectable } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { ApiObject } from './apiobject';


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

        // TODO
        const mockData: ApiObject[] = [
            {
                'id': '1',
                'name': 'Mock Service',
                'shortName': 'test.mock-service',
                'description': 'A generic dummy service',
                'internalDependencies': [2, 3],
                'externalDependencies': [4],
                'status': 'online',
                'external': false,
            },
            {
                'id': '2',
                'name': 'Hello World Service',
                'shortName': 'test.hello-world-service',
                'description': 'A generic hello world service',
                'internalDependencies': [],
                'externalDependencies': [4],
                'status': 'online',
                'external': false,
            },
            {
                'id': '3',
                'name': 'Bye World Service',
                'shortName': 'test.bye-world-service',
                'description': 'A generic service',
                'internalDependencies': [2],
                'externalDependencies': [],
                'status': 'offline',
                'external': false,
            },
            {
                'id': '4',
                'name': 'External Service',
                'shortName': 'ext.service',
                'description': 'A generic service',
                'internalDependencies': [],
                'externalDependencies': [],
                'status': 'problem',
                'external': true,
            },
            {
                'id': '5',
                'name': 'Internal Service',
                'shortName': 'int.service',
                'description': 'A generic service',
                'internalDependencies': [],
                'externalDependencies': [6],
                'status': 'online',
                'external': false,
            },
            {
                'id': '6',
                'name': 'External Service',
                'shortName': 'ext.service',
                'description': 'A generic service',
                'internalDependencies': [5],
                'externalDependencies': [],
                'status': 'problem',
                'external': true,
            },
        ];

        stream.next(freezeObject(mockData));

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

    getServiceById(id): Observable<ApiObject> {
        // TODO check if there is a resource for single services
        const resource = 'service/' + id;
        const stream = this.getStreamSource(resource);

        // TODO

        const mockData: ApiObject[] = [
            {
                'id': '1',
                'name': 'Mock Service',
                'shortName': 'test.mock-service',
                'description': 'A generic dummy service',
                'internalDependencies': [2, 3],
                'externalDependencies': [4],
                'status': 'online',
            },
            {
                'id': '2',
                'name': 'Hello World Service',
                'shortName': 'test.hello-world-service',
                'description': 'A generic hello world service',
                'internalDependencies': [],
                'externalDependencies': [4],
                'status': 'online',
            },
            {
                'id': '3',
                'name': 'Bye World Service',
                'shortName': 'test.bye-world-service',
                'description': 'A generic service',
                'internalDependencies': [2],
                'externalDependencies': [],
                'status': 'offline',
            },
            {
                'id': '4',
                'name': 'External Service',
                'shortName': 'ext.service',
                'description': 'A generic service',
                'internalDependencies': [],
                'externalDependencies': [],
                'status': 'problem'
            },
            {
                'id': '5',
                'name': 'Internal Service',
                'shortName': 'int.service',
                'description': 'A generic service',
                'internalDependencies': [],
                'externalDependencies': [6],
                'status': 'online'
            },
            {
                'id': '6',
                'name': 'External Service',
                'shortName': 'ext.service',
                'description': 'A generic service',
                'internalDependencies': [5],
                'externalDependencies': [],
                'status': 'problem'
            },
        ];

        const genericMockData: ApiObject = {
            'id': id,
            'name': 'Generic World Service id ' + id,
            'shortName': 'test.' + id + 'service',
            'description': 'A generic generated service',
        };

        if (id > 0 && id <= 4) {
            stream.next(freezeObject(mockData[id - 1]));
        } else {
            stream.next(freezeObject(genericMockData));
        }


        return (stream.asObservable() as Observable<Readonly<ApiObject[]>>).pipe(
            filter(data => data !== undefined)
        );
    }

    getServiceInterfaces(serviceId): Observable<ApiObject[]> {
        const resource = 'service/' + serviceId + '/interfaces';
        const stream = this.getStreamSource(resource);

        // TODO

        const mockData: ApiObject[] = [
            {
                'Name': 'test.mock-service.rest',
                'Description': 'the awesome REST interface for the even more awesome Mock Sertice!',
                'Port': '0815',
                'Protocol': 'gRPC',
                'TransportProtocol': 'HTTP',
                'Public-DNS': 'to be defined',
            },
            {
                'Name': 'test.mock-service.sth',
                'Description': 'some interface for test.mock-service',
                'Port': '12',
                'Protocol': 'SQL',
                'TransportProtocol': 'MQTT',
                'Public-DNS': 'to be defined',
            },
        ];


        const genericMockData: ApiObject = [{
            'Name': 'generic' + serviceId,
            'Description': 'A generic interface for service nr ' + serviceId,
            'Port': '11833-' + serviceId,
            'Protocol': 'pigeon5',
            'TransportProtocol': 'carrier pigeon',
            'Public-DNS': 'to be defined',
        }];

        if (serviceId > 0 && serviceId <= 1) {
            stream.next(freezeObject(mockData));
        } else {
            stream.next(freezeObject(genericMockData));
        }


        return (stream.asObservable() as Observable<Readonly<ApiObject[]>>).pipe(
            filter(data => data !== undefined)
        );
    }
}
