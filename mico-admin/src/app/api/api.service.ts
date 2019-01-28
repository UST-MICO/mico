import { Injectable } from '@angular/core';
import { Observable, Subject, BehaviorSubject, AsyncSubject } from 'rxjs';
import { filter, flatMap } from 'rxjs/operators';
import { ApiObject } from './apiobject';
import { ApiBaseFunctionService } from './api-base-function.service';
import { ApiModel, ApiModelAllOf } from './apimodel';


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

type ApiModelMap = { [prop: string]: ApiModel | ApiModelAllOf };


/**
 * Service to interact with the mico api.
 */
@Injectable({
    providedIn: 'root'
})
export class ApiService {
    private streams: Map<string, Subject<Readonly<any>>> = new Map();

    constructor(private rest: ApiBaseFunctionService, ) { }

    /**
     * Canonize a resource url.
     *
     * (Remove schema, host, port, api path prefix and leading/trailing slashes.)
     *
     * @param streamURL resource url
     */
    private canonizeStreamUrl(streamURL: string): string {
        try {
            const x = new URL(streamURL);
            streamURL = x.pathname;
        } catch (TypeError) { }
        streamURL = streamURL.replace(/(^\/)|(\/$)/g, '');
        return streamURL;
    }

    /**
     * Fetch the stream source for the given resource url.
     *
     * @param streamURL resource url
     * @param defaultSubject function to create a new streamSource if needed (default: BehaviourSubject)
     */
    private getStreamSource<T>(streamURL: string, defaultSubject: () => Subject<Readonly<T>> =
        () => new BehaviorSubject<Readonly<T>>(undefined)
    ): Subject<Readonly<T>> {
        streamURL = this.canonizeStreamUrl(streamURL);
        let stream = this.streams.get(streamURL);
        if (stream == null) {
            stream = defaultSubject();
            if (stream != null) {
                this.streams.set(streamURL, stream);
            }
        }
        return stream as Subject<Readonly<T>>;
    }

    getModelDefinitions(): Observable<Readonly<ApiModelMap>> {
        const resource = 'models';
        const stream = this.getStreamSource<ApiModelMap>(resource, () => new AsyncSubject<Readonly<ApiModelMap>>());

        // TODO replace URL with a generic path
        this.rest.get<{ definitions: ApiModelMap, [prop: string]: any }>('http://localhost:8080/v2/api-docs').subscribe(val => {

            stream.next(freezeObject(val.definitions));
            stream.complete();
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }


    // =================
    // APPLICATION CALLS
    // =================

    /**
     * Get application list
     */
    getApplications(): Observable<Readonly<ApiObject[]>> {

        const resource = 'applications';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            // return actual application list
            stream.next(freezeObject(val._embedded.micoApplicationList));
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * Get all versions of an application based on its shortName
     * @param shortName the shortName of the applicaton
     */
    getApplicationVersions(shortName: string) {
        const resource = 'applications/' + shortName + '/';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {

            let list: ApiObject[];

            if (val['_embedded'] != null) {

                list = val._embedded.micoApplicationList;
            }

            if (list === undefined) {
                list = [];
            }

            stream.next(freezeObject(list));
        });

        return (stream.asObservable()).pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * get an application based on its shortName and version
     *
     * @param shortName of the application
     * @param version of the application
     */
    getApplication(shortName: string, version: string) {
        const resource = 'applications/' + shortName + '/' + version;
        const stream = this.getStreamSource<ApiObject>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            stream.next(freezeObject(val));
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }

    postApplication(data) {
        if (data == null) {
            return;
        }


        const resource = 'applications/';

        return this.rest.post<ApiObject>(resource, data).pipe(flatMap(val => {
            this.getApplications();

            const stream = this.getStreamSource<ApiObject>(val._links.self.href);
            stream.next(val);

            return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
                filter(service => service !== undefined)
            );
        }));
    }

    /**
     * commands the mico-core application to deploy application {shortName}, {version}
     * @param shortName the applications shortName
     * @param version the applications version
     */
    postApplicationDeployCommand(shortName: string, version: string) {
        const resource = 'applications/' + shortName + '/' + version + '/deploy';

        return this.rest.post<ApiObject>(resource, null).pipe(flatMap(val => {

            const stream = this.getStreamSource<ApiObject>(val._links.self.href);
            stream.next(val);

            // TODO call API endpoints like application status

            return stream.asObservable().pipe(
                filter(service => service !== undefined)
            );
        }));
    }

    // TODO doc comment as soon as the endpoint is in this branch
    getApplicationDeploymentInformation(shortName: string, version: string) {
        const resource = 'applications/' + shortName + '/' + version + '/deploymentInformation';
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            stream.next(freezeObject((val as ApiObject)));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
            filter(data => data !== undefined)
        );
    }

    // =============
    // SERVICE CALLS
    // =============

    /**
     * Get service list
     */
    getServices(): Observable<Readonly<ApiObject[]>> {
        const resource = 'services';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            // return actual service list
            stream.next(freezeObject(val._embedded.micoServiceList));
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * Get all versions of a service based on its shortName
     */
    getServiceVersions(shortName): Observable<ApiObject[]> {

        const resource = 'services/' + shortName + '/';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {

            let list: ApiObject[];

            if (val['_embedded'] != null) {

                list = val._embedded.micoServiceList;
            }

            if (list === undefined) {
                list = [];
            }

            stream.next(freezeObject(list));
        });

        return stream.asObservable().pipe(
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
        const stream = this.getStreamSource<ApiObject>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            stream.next(freezeObject(val));
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }

    postService(data): Observable<Readonly<ApiObject>> {

        if (data == null) {
            return;
        }

        const resource = 'services/';

        return this.rest.post<ApiObject>(resource, data).pipe(flatMap(val => {

            const stream = this.getStreamSource<ApiObject>(val._links.self.href);
            stream.next(val);

            this.getServices();
            this.getServiceVersions(data.shortName);

            return stream.asObservable().pipe(
                filter(service => service !== undefined)
            );
        }));

    }

    postServiceViaGithub(data): Observable<Readonly<ApiObject>> {
        if (data == null) {
            return;
        }

        return this.rest.post<ApiObject>('services/import/github', data, undefined, false).pipe(flatMap(val => {

            const stream = this.getStreamSource<ApiObject>(val._links.self.href);
            stream.next(val);

            this.getServices();
            this.getServiceVersions(data.shortName);

            return stream.asObservable().pipe(
                filter(service => service !== undefined)
            );
        }));
    }


    putService(shortName, version, data): Observable<Readonly<ApiObject>> {

        if (data == null) {
            return;
        }

        const resource = 'services/' + shortName + '/' + version;

        return this.rest.put<ApiObject>(resource, data).pipe(flatMap(val => {

            const stream = this.getStreamSource<ApiObject>(val._links.self.href);
            stream.next(val);

            return stream.asObservable().pipe(
                filter(service => service !== undefined)
            );
        }));
    }

    /**
     * Get all services a specific service depends on.
     *
     * @param shortName unique short name of the service
     * @param version service version to be returned
     */
    getServiceDependees(shortName, version): Observable<ApiObject[]> {

        const resource = 'services/' + shortName + '/' + version + '/dependees';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            stream.next(freezeObject(val._embedded.serviceList));
        });

        return stream.asObservable().pipe(
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
        const stream = this.getStreamSource<ApiObject>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            stream.next(freezeObject(val));
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }

    // =======================
    // SERVICE INTERFACE CALLS
    // =======================

    getServiceInterfaces(shortName, version): Observable<ApiObject> {
        const resource = 'services/' + shortName + '/' + version + '/interfaces';
        const stream = this.getStreamSource<ApiObject>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            stream.next(freezeObject((val as ApiObject)));
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }

    postServiceInterface(shortName, version, data) {
        if (data == null) {
            return;
        }

        return this.rest.post<ApiObject>('services/' + shortName + '/' + version + '/interfaces',
            data, undefined, false).pipe(flatMap(val => {

                const stream = this.getStreamSource<ApiObject>(val._links.self.href);
                stream.next(val);

                this.getServiceInterfaces(shortName, version);

                return stream.asObservable().pipe(
                    filter(service => service !== undefined)
                );
            }));
    }
}
