import { Injectable } from '@angular/core';
import { Observable, Subject, BehaviorSubject, AsyncSubject } from 'rxjs';
import { filter, flatMap, map } from 'rxjs/operators';
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
    if (Object.isFrozen(obj)) {
        return;
    }
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

    constructor(private rest: ApiBaseFunctionService) {}

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
        streamURL = streamURL.replace(/\/\//g, '/');
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
        if (!stream.isStopped) {
            this.rest.get<{ definitions: ApiModelMap, [prop: string]: any }>('v2/api-docs').subscribe(val => {

                stream.next(freezeObject(val.definitions));
                stream.complete();
            });
        }

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
            if (val.hasOwnProperty('_embedded')) {
                stream.next(freezeObject(val._embedded.micoApplicationList));
            } else {
                stream.next(freezeObject([]));
            }
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

            if (val.hasOwnProperty('_embedded')) {
                list = val._embedded.micoApplicationList;
            } else {
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

    putApplication(shortName, version, data): Observable<Readonly<ApiObject>> {

        if (data == null) {
            return;
        }

        const resource = 'applications/' + shortName + '/' + version;

        return this.rest.put<ApiObject>(resource, data).pipe(flatMap(val => {

            const stream = this.getStreamSource<ApiObject>(val._links.self.href);
            stream.next(val);

            return stream.asObservable().pipe(
                filter(application => application !== undefined)
            );
        }));
    }

    deleteApplication(shortName: string, version: string) {

        return this.rest.delete<any>('applications/' + shortName + '/' + version)
            .pipe(map(val => {

                this.getApplications();

                return true;
            }));

    }



    postApplicationServices(applicationShortName: string, applicationVersion: string, serviceData: any) {

        if (serviceData == null) {
            return;
        }

        const resource = 'applications/' + applicationShortName + '/' + applicationVersion + '/services';

        return this.rest.post<ApiObject>(resource, serviceData).pipe(map(val => {

            this.getApplication(applicationShortName, applicationVersion);

            return true;
        }));
    }

    deleteApplicationServices(applicationShortName: string, applicationVersion: string, serviceShortName) {

        return this.rest.delete<ApiObject>('applications/' + applicationShortName + '/' + applicationVersion
            + '/services/' + serviceShortName)
            .pipe(map(val => {
                console.log('DELETE includes', val);

                this.getApplication(applicationShortName, applicationVersion);

                return true;
            }));
    }


    // ==========
    // DEPLOYMENT
    // ==========

    /**
     * commands the mico-core application to deploy application {shortName}, {version}
     * @param shortName the applications shortName
     * @param version the applications version
     */
    postApplicationDeployCommand(shortName: string, version: string) {
        const resource = 'applications/' + shortName + '/' + version + '/deploy';

        return this.rest.post<any>(resource, null).pipe(map(val => {

            // TODO handle job ressource as soon as the api call returns a job ressource
            return true;
        }));
    }

    // TODO doc comment as soon as the endpoint is in this branch
    getApplicationDeploymentInformation(shortName: string, version: string) {
        const resource = 'applications/' + shortName + '/' + version + '/deploymentInformation';
        const stream = this.getStreamSource(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            stream.next(freezeObject(val));
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
            if (val.hasOwnProperty('_embedded')) {
                stream.next(freezeObject(val._embedded.micoServiceList));
            } else {
                stream.next(freezeObject([]));
            }
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

            if (val.hasOwnProperty('_embedded')) {
                list = val._embedded.micoServiceList;
            } else {
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

    deleteService(shortName, version) {
        return this.rest.delete<ApiObject>('services/' + shortName + '/' + version)
            .pipe(map(val => {
                console.log('DELETE SERVICE', val);

                this.getServices();

                return true;
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
            if (val.hasOwnProperty('_embedded')) {
                stream.next(freezeObject(val._embedded.serviceList));
            } else {
                stream.next(freezeObject([]));
            }
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

    getServiceInterfaces(shortName, version): Observable<ApiObject[]> {
        const resource = 'services/' + shortName + '/' + version + '/interfaces';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {

            if (val.hasOwnProperty('_embedded')) {
                stream.next(freezeObject(val._embedded.micoServiceInterfaceList));
            } else {
                stream.next(freezeObject([]));
            }

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
            data).pipe(flatMap(val => {

                console.log('RETURN', val);

                const stream = this.getStreamSource<ApiObject>(val._links.self.href);
                stream.next(val);

                this.getServiceInterfaces(shortName, version);

                return stream.asObservable().pipe(
                    filter(service => service !== undefined)
                );
            }));
    }

    deleteServiceInterface(shortName: string, version: string, serviceInterfaceName: string) {

        return this.rest.delete<ApiObject>('services/' + shortName + '/' + version + '/interfaces/' + serviceInterfaceName)
            .pipe(map(val => {
                console.log('DELETE INTERFACE', val);


                this.getServiceInterfaces(shortName, version);

                return true;
            }));
    }

    getServiceInterfacePublicIp(serviceShortName: string, serviceVersion: string, interfaceShortName: string) {

        const resource = 'services/' + serviceShortName + '/' + serviceVersion + '/interfaces/' + interfaceShortName + '/publicIP';
        const stream = this.getStreamSource<ApiObject>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {

            stream.next(freezeObject((val as ApiObject)));
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }
}
