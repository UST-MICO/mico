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
     * uses: GET application
     */
    getApplications(): Observable<Readonly<ApiObject[]>> {

        const resource = 'applications';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            // return actual application list
            if (val.hasOwnProperty('_embedded')) {
                stream.next(freezeObject(val._embedded.micoApplicationWithServicesResponseDTOList));
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
     * uses: GET application/{shortName}
     *
     * @param shortName the shortName of the applicaton
     */
    getApplicationVersions(shortName: string) {
        const resource = 'applications/' + shortName + '/';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {

            let list: ApiObject[];

            if (val.hasOwnProperty('_embedded')) {
                list = val._embedded.micoApplicationWithServicesResponseDTOList;
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
     * delete all versions of an application
     * uses: DELETE application/{shortName}
     *
     * @param shortName shortName of the application versions to be deleted
     */
    deleteAllApplicationVersions(shortName: string) {

        return this.rest.delete<ApiObject>('applications/' + shortName)
            .pipe(map(val => {

                this.getApplications();
                return true;
            }));
    }

    /**
     * get an application based on its shortName and version
     * uses: GET applications/{shortName}/{version}
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

    /**
     * Creates a new application
     * uses: POST applications
     *
     * @param data object holding the applications information
     */
    postApplication(data) {
        if (data == null) {
            return;
        }


        const resource = 'applications/';

        return this.rest.post<ApiObject>(resource, data).pipe(flatMap(val => {
            this.getApplications();
            this.getApplicationVersions(data.shortName);

            const stream = this.getStreamSource<ApiObject>(val._links.self.href);
            stream.next(val);

            return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
                filter(service => service !== undefined)
            );
        }));
    }

    /**
     * Creates a new application version from an existing application
     * uses: POST applications/{shortName}/{version}/promote
     *
     * @param shortName shortName of the application
     * @param version version of the application
     * @param newVersion the new version of the application
     */
    promoteApplication(shortName, version, newVersion: string) {

        const resource = 'applications/' + shortName + '/' + version + '/promote';

        return this.rest.post<ApiObject>(resource, { version: newVersion }).pipe(flatMap(val => {
            this.getApplications();
            this.getApplicationVersions(val.shortName);

            const stream = this.getStreamSource<ApiObject>(val._links.self.href);
            stream.next(val);

            return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
                filter(service => service !== undefined)
            );
        }));
    }

    /**
     * Updates an existing application
     * uses: PUT applications/{shortName}/{version}
     *
     * @param shortName shortName of the application
     * @param version version of the application
     * @param data object holding the updated application information
     */
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

    /**
     * Deletes a specific application version
     * uses: DELETE applications/{shortName}/{version}
     *
     * @param shortName shortName of the application
     * @param version version of the application
     */
    deleteApplication(shortName: string, version: string) {

        return this.rest.delete<any>('applications/' + shortName + '/' + version)
            .pipe(map(val => {

                this.getApplications().subscribe();
                this.getApplicationVersions(shortName).subscribe();

                return true;
            }));

    }

    /**
     * Adds an includes relationship from an appliction to a service
     * uses: POST applications/{applicationShortName}/{applicationVersion}/services/{serviceShortName}/{serviceVersion}
     *
     * @param applicationShortName the applications shortName
     * @param applicationVersion the applications version
     * @param serviceShortName the services shortName
     * @param serviceVersion the services version
     */
    postApplicationServices(applicationShortName: string, applicationVersion: string, serviceShortName: string, serviceVersion: string) {

        const resource = 'applications/' + applicationShortName + '/' + applicationVersion +
            '/services/' + serviceShortName + '/' + serviceVersion;

        return this.rest.post<ApiObject>(resource, null).pipe(map(val => {

            this.getApplicationVersions(applicationShortName);
            this.getApplication(applicationShortName, applicationVersion);

            return true;
        }));
    }

    /**
     * Deletes an includes relationship from an application to a service
     * uses: DELETE applications/{shortName}/{version}/services
     *
     * @param applicationShortName the applications shortName
     * @param applicationVersion the applications version
     * @param serviceShortName the services shortName
     */
    deleteApplicationServices(applicationShortName: string, applicationVersion: string, serviceShortName) {

        return this.rest.delete<ApiObject>('applications/' + applicationShortName + '/' + applicationVersion
            + '/services/' + serviceShortName)
            .pipe(map(val => {

                this.getApplicationVersions(applicationShortName);
                this.getApplication(applicationShortName, applicationVersion);

                return true;
            }));
    }

    /**
     * Returns the deployment information of an applications included service
     * uses: GET applications/{applicationShortName}/{applicationVersion}/deploymentInformation/{serviceShortName}
     *
     * @param applicationShortName shortName of the application
     * @param applicationVersion version of the application
     * @param serviceShortName shortName of the service
     */
    getServiceDeploymentInformation(applicationShortName: string, applicationVersion: string, serviceShortName) {

        const resource = 'applications/' + applicationShortName + '/' + applicationVersion + '/deploymentInformation/' + serviceShortName;
        const stream = this.getStreamSource<ApiObject>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            stream.next(freezeObject(val));
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }


    /**
     * Updates the deployment information of an applications service
     * uses: PUT applications/{applicationShortName}/{applicationVersion}/deploymentInformation/{serviceShortName}
     *
     * @param applicationShortName shortName of the application
     * @param applicationVersion version of the application
     * @param serviceShortName shortName of the service
     * @param data object holding the updated deployment information
     */
    putServiceDeploymentInformation(applicationShortName: string, applicationVersion: string, serviceShortName, data) {
        if (data == null) {
            return;
        }

        const resource = 'applications/' + applicationShortName + '/' + applicationVersion + '/deploymentInformation/' + serviceShortName;

        return this.rest.put<ApiObject>(resource, data).pipe(flatMap(val => {

            const stream = this.getStreamSource<ApiObject>(val._links.self.href);
            stream.next(val);

            return stream.asObservable().pipe(
                filter(application => application !== undefined)
            );
        }));
    }


    // ==========
    // DEPLOYMENT
    // ==========

    /**
     * commands the mico-core application to deploy an application
     * uses: POST applications/{shortName}/{version}/deploy
     *
     * @param shortName the applications shortName
     * @param version the applications version
     */
    postApplicationDeployCommand(shortName: string, version: string) {
        const resource = 'applications/' + shortName + '/' + version + '/deploy';

        return this.rest.post<any>(resource, null).pipe(map(val => {

            // TODO handle job ressource as soon as the api call returns a job ressource
            return val;
        }));
    }

    /**
     * commands the mico-core application to undeploy an application
     * uses: POST applications/{shortName}/{version}/undeploy
     *
     * @param shortName the applications shortName
     * @param version the applications version
     */
    postApplicationUndeployCommand(shortName: string, version: string) {
        const resource = 'applications/' + shortName + '/' + version + '/undeploy';

        return this.rest.post<any>(resource, null).pipe(map(val => {

            // TODO handle job ressource as soon as the api call returns a job ressource
            return val;
        }));
    }


    /**
     * returns runtime information (status) about a specific application and its services
     * uses GET applications/{shortName}/{version}/status
     *
     * @param shortName the applications shortName
     * @param version the applications version
     */
    getApplicationStatus(shortName: string, version: string) {
        const resource = 'applications/' + shortName + '/' + version + '/status';
        const stream = this.getStreamSource(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            stream.next(freezeObject(val));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
            filter(data => data !== undefined)
        );
    }


    /**
     * Gets the public IP of a deployed service
     * uses: services/{shortName}/{version}/interaces/{interfaceName}/publicIP
     *
     * @param serviceShortName shortName of the service
     * @param serviceVersion version of the service
     * @param interfaceShortName name of the serviceInterface
     */
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


    /**
     * returns runtime information (status) about a specific service
     * uses GET services/{shortName}/{version}/status
     *
     * @param shortName the services shortName
     * @param version the services version
     */
    getServiceStatus(shortName: string, version: string) {
        const resource = 'services/' + shortName + '/' + version + '/status';
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
     * Get a list of all services
     * uses: GET services
     */
    getServices(): Observable<Readonly<ApiObject[]>> {
        const resource = 'services';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            // return actual service list
            if (val.hasOwnProperty('_embedded')) {
                stream.next(freezeObject(val._embedded.micoServiceResponseDTOList));
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
     * uses: GET services/{shortName}
     *
     * @param shortName the serices shortName
     */
    getServiceVersions(shortName): Observable<ApiObject[]> {

        const resource = 'services/' + shortName + '/';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {

            let list: ApiObject[];

            if (val.hasOwnProperty('_embedded')) {
                list = val._embedded.micoServiceResponseDTOList;
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
     * delete all versions of a service
     * uses: DELETE services/{shortName}
     *
     * @param shortName shortName of the service versions to be deleted
     */
    deleteAllServiceVersions(shortName: string) {

        return this.rest.delete<ApiObject>('services/' + shortName)
            .pipe(map(val => {

                this.getServices();
                return true;
            }));
    }

    /**
     * Get a specific version of a service
     * uses: GET services/{shortName}/{version}
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

    /**
     * Creates a new service
     * uses: POST services
     *
     * @param data object holding the services information
     */
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

    /**
     * takes an url to a github repository and returns the available versions of the repository.
     * uses: GET services/import/github
     *
     * @param url url to the github repository
     */
    getServiceVersionsViaGithub(url: string): Observable<Readonly<String[]>> {
        const resource = 'services/import/github' + '?url=' + url;
        const stream = this.getStreamSource<String[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {

            let list: string[] = [];

            if (val.hasOwnProperty('_embedded')) {
                list = val._embedded.micoVersionRequestDTOList.map(version => version.version);
            }

            stream.next(freezeObject(list));
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * crawls a specific version from a github repository
     * uses: POST services/import/github
     *
     * @param url url to the github repository
     * @param version version to be crawled
     */
    postServiceViaGithub(url: string, version: string): Observable<Readonly<ApiObject>> {

        return this.rest.post<ApiObject>('services/import/github', { url: url, version: version }, undefined, false)
            .pipe(flatMap(val => {

                const stream = this.getStreamSource<ApiObject>(val._links.self.href);
                stream.next(val);

                this.getServices();
                this.getServiceVersions(val.shortName);

                return stream.asObservable().pipe(
                    filter(service => service !== undefined)
                );
            }));
    }

    /**
     * Updates a services information
     * uses: PUT service/{shortName}/{version}
     *
     * @param shortName the services shortName
     * @param version the services version
     * @param data object holding the services updated version
     */
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
     * Creates a new service version from an existing service
     * uses: POST services/{shortName}/{version}/promote
     *
     * @param shortName shortName of the service
     * @param version version of the service
     * @param newVersion the new version of the service
     */
    promoteService(shortName, version, newVersion: string) {

        const resource = 'services/' + shortName + '/' + version + '/promote';

        return this.rest.post<ApiObject>(resource, { version: newVersion }).pipe(flatMap(val => {
            this.getServices();
            this.getServiceVersions(val.shortName);

            const stream = this.getStreamSource<ApiObject>(val._links.self.href);
            stream.next(val);

            return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
                filter(service => service !== undefined)
            );
        }));
    }

    deleteService(shortName, version) {
        return this.rest.delete<ApiObject>('services/' + shortName + '/' + version)
            .pipe(map(val => {

                this.getServices();

                return true;
            }));
    }

    /**
     * Get all services a specific service depends on.
     * uses: GET services/{shortName}/{version}/dependees
     *
     * @param shortName unique short name of the service
     * @param version service version to be returned
     */
    getServiceDependees(shortName, version): Observable<ApiObject[]> {

        const resource = 'services/' + shortName + '/' + version + '/dependees';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            if (val.hasOwnProperty('_embedded')) {
                stream.next(freezeObject(val._embedded.micoServiceResponseDTOList));
            } else {
                stream.next(freezeObject([]));
            }
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * Adds a depends on relation from a service to an other service (dependee)
     * uses: POST services/{shortName}/{version}/dependees
     *
     * @param serviceShortName shortName of the depending service
     * @param serviceVersion version of the depending service
     * @param dependee object holding the dependee
     */
    postServiceDependee(serviceShortName, serviceVersion, dependee) {
        if (dependee == null) {
            return;
        }

        const url = 'services/' + serviceShortName + '/' + serviceVersion + '/dependees/' + dependee.shortName + '/' + dependee.version;

        return this.rest.post<ApiObject>(url, undefined, undefined, false).pipe(flatMap(val => {

            const stream = this.getService(serviceShortName, serviceVersion);
            this.getServiceDependees(serviceShortName, serviceVersion);
            this.getServiceDependencyGraph(serviceShortName, serviceVersion);

            return stream;
        }));
    }

    /**
     * deletes a depends on relation of a service
     * uses: DELETE services/{shortName}/{version}/dependees
     *
     * @param serviceShortName shortName of the depending service
     * @param serviceVersion version of the depending service
     * @param dependeeShortName shortName of the depended service
     * @param dependeeVersion version of the depended service
     */

    deleteServiceDependee(serviceShortName: string, serviceVersion: string, dependeeShortName: string, dependeeVersion: string) {
        return this.rest.delete<ApiObject>('services/' + serviceShortName + '/' + serviceVersion + '/dependees/' +
            dependeeShortName + '/' + dependeeVersion)
            .pipe(map(val => {

                this.getServices();
                this.getService(serviceShortName, serviceVersion);
                this.getServiceDependees(serviceShortName, serviceVersion);
                this.getServiceDependencyGraph(serviceShortName, serviceVersion);

                return true;
            }));

    }

    /**
     * Get all services depending on a specific service
     * uses: GET services/{shortName}/{version}/dependers
     *
     * @param shortName unique short name of the service
     * @param version service version to be returned
     */
    getServiceDependers(shortName, version): Observable<ApiObject[]> {

        const resource = 'services/' + shortName + '/' + version + '/dependers';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {
            if (val.hasOwnProperty('_embedded')) {
                stream.next(freezeObject(val._embedded.micoServiceResponseDTOList));
            } else {
                stream.next(freezeObject([]));
            }
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * Get full dependency graph of service
     * uses: GET services/{shortName}/{version}/dependencyGraph
     *
     * @param shortName unique short name of the service
     * @param version service version to be returned
     */
    getServiceDependencyGraph(shortName, version): Observable<ApiObject> {

        const resource = 'services/' + shortName + '/' + version + '/dependencyGraph';
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

    /**
     * get all service interfaces of a specified service
     * uses: GET services/{shortName}/{version}/interfaces
     *
     * @param shortName shortName of the service
     * @param version version of the service
     */
    getServiceInterfaces(shortName, version): Observable<ApiObject[]> {
        const resource = 'services/' + shortName + '/' + version + '/interfaces';
        const stream = this.getStreamSource<ApiObject[]>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {

            if (val.hasOwnProperty('_embedded')) {
                stream.next(freezeObject(val._embedded.micoServiceInterfaceResponseDTOList));
            } else {
                stream.next(freezeObject([]));
            }

        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * Adds a new service interface to a service
     * uses: POST services/{shortName}/{version}/interfaces
     *
     * @param shortName shortName of the service
     * @param version version of the service
     * @param data object holding the interfaces information
     */
    postServiceInterface(shortName, version, data) {
        if (data == null) {
            return;
        }

        return this.rest.post<ApiObject>('services/' + shortName + '/' + version + '/interfaces',
            data).pipe(flatMap(val => {

                const stream = this.getStreamSource<ApiObject>(val._links.self.href);
                stream.next(freezeObject(val));

                this.getServiceInterfaces(shortName, version);

                return stream.asObservable().pipe(
                    filter(service => service !== undefined)
                );
            }));
    }

    /**
     * Updates a service interface
     * uses: PUT services/{shortName}/{version}/interfaces/{serviceInterfaceName}
     *
     * @param shortName the services shortName
     * @param version the services version
     * @param serviceInterfaceName the interfaces name
     * @param serviceInterfaceData the updated interface data
     */
    putServiceInterface(shortName: string, version: string, serviceInterfaceName: string, serviceData: any) {
        const resource = 'services/' + shortName + '/' + version + '/interfaces/' + serviceInterfaceName;

        return this.rest.put<ApiObject>(resource, serviceData).pipe(flatMap(val => {

            const stream = this.getStreamSource<ApiObject>(val._links.self.href);
            stream.next(freezeObject(val));

            this.getServiceInterfaces(shortName, version);

            return stream.asObservable().pipe(
                filter(service => service !== undefined)
            );
        }));
    }

    /**
     * Deletes a service interface
     * uses: DELETE services/{shortName}/{version}/interfaces/{serviceInterfaceName}
     *
     * @param shortName the services shortName
     * @param version the services version
     * @param serviceInterfaceName the interfaces name
     */
    deleteServiceInterface(shortName: string, version: string, serviceInterfaceName: string) {

        return this.rest.delete<ApiObject>('services/' + shortName + '/' + version + '/interfaces/' + serviceInterfaceName)
            .pipe(map(val => {

                this.getServiceInterfaces(shortName, version);

                return true;
            }));
    }

    // ===============
    // BACKGROUND JOBS
    // ===============

    /**
     * retrieves the status of a deployment
     * uses: GET jobs/{applicationShortName}/{applicationVersion}/status
     *
     * @param applicationShortName shortName of the application
     * @param applicationVersion version of the application
     */
    getJobStatus(applicationShortName: string, applicationVersion: string) {
        const resource = 'jobs/' + applicationShortName + '/' + applicationVersion + '/status';
        const stream = this.getStreamSource<ApiObject>(resource);

        this.rest.get<ApiObject>(resource).subscribe(val => {

            stream.next(freezeObject(val));
        });

        return stream.asObservable().pipe(
            filter(data => data !== undefined)
        );
    }
}
