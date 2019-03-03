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

import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError, } from 'rxjs/operators';
import { ApiObject, ApiLinksObject, LinkObject, isApiObject, isApiLinksObject, isLinkObject } from './apiobject';
import { MatSnackBar } from '@angular/material';



@Injectable({
    providedIn: 'root'
})
export class ApiBaseFunctionService {

    constructor(private http: Http, private snackBar: MatSnackBar) { }

    private extractUrl(url: string | LinkObject | ApiLinksObject | ApiObject): string {
        if (typeof url === 'string' || url instanceof String) {
            return this.prepareRelativeUrl(url as string);
        }
        if (isApiObject(url)) {
            url = url._links;
        }
        if (isApiLinksObject(url)) {
            url = url.self;
        }
        if (isLinkObject(url)) {
            url = url.href;
        }
        return this.prepareRelativeUrl(url as string);
    }

    private prepareRelativeUrl(url: string): string {
        if (url.startsWith('http')) {
            return url;
        }
        let url_string: string = environment.settings.api;
        if (url_string.endsWith('/')) {
            url_string = url_string.slice(0, url_string.length - 1);
        }
        // specific exception for swagger json (does not work with tailing /)
        if (!url.endsWith('/') && !url.endsWith('api-docs')) {
            if ((url.lastIndexOf('.') < 0) || (url.lastIndexOf('/') > url.lastIndexOf('.'))) {
                url = url + '/';
            }
        }
        if (url.startsWith('/')) {
            return url_string + url;
        } else {
            return url_string + '/' + url;
        }
    }

    private headers(token?: string, mimetypeJSON: boolean = true): RequestOptions {
        const headers = new Headers();
        if (mimetypeJSON) {
            headers.append('Content-Type', 'application/json');
        }
        if (token != null) {
            headers.append('Authorization', 'Bearer ' + token);
        }

        return new RequestOptions({ headers: headers });
    }


    /**
     * Takes a caught error and displays it in a snack bar.
     *
     * @param error the error object which was caught
     * @param httpVerb the http verb from the method, where the error occured (e.g. GET)
     */
    private showError = (error, httpVerb) => {
        if (error.hasOwnProperty('_body')) {
            try {
                // handling for defined error messagges
                const message = JSON.parse(error._body).message;
                const path = JSON.parse(error._body).path;

                this.snackBar.open('An error occured in ' + httpVerb + ' ' + path + ': ' + message, 'Ok', {
                    duration: 0,
                });
            } catch (e) {
                // generic handling for errors
                console.error(e);
                this.snackBar.open('An error occured in a ' + httpVerb + ' Method. The error could not be handled correctly. ' +
                    'See the console for details.', 'Ok', {
                        duration: 0,
                    });
            }
        }
        return throwError(error);
    }

    get<T>(url: string | LinkObject | ApiLinksObject | ApiObject, token?: string, params?): Observable<T> {
        url = this.extractUrl(url);

        const options = this.headers(token);
        if (params != null) {
            options.params = params;
        }

        const request = this.http.get(url, options).pipe(
            catchError((error) => this.showError(error, 'GET')),
            map((res: Response) => {
                return res.json();
            }));

        return request;
    }


    post<T>(url: string | LinkObject | ApiLinksObject | ApiObject, data, token?: string, isJson = true): Observable<T> {
        url = this.extractUrl(url);
        let tempData = data;
        if (data != null) {
            if (isJson) {
                tempData = JSON.stringify(tempData);
            }
        }
        return this.http.post(url, tempData, this.headers(token))
            .pipe(
                catchError((error) => this.showError(error, 'POST')),
                map((res: Response) => {
                    if (res.hasOwnProperty('_body')) {
                        if ((res as any)._body == null || (res as any)._body.length < 1) {
                            // handle empty results
                            return undefined;
                        }
                    }
                    return res.json();

                }));
    }

    put<T>(url: string | LinkObject | ApiLinksObject | ApiObject, data, token?: string, isJson = true): Observable<T> {
        url = this.extractUrl(url);
        let tempData = data;
        if (isJson) {
            tempData = JSON.stringify(tempData);
        }
        return this.http.put(url, tempData, this.headers(token))
            .pipe(
                catchError((error) => this.showError(error, 'PUT')),
                map((res: Response) => {
                    return res.json();
                }));
    }

    delete<T>(url: string | LinkObject | ApiLinksObject | ApiObject, token?: string): Observable<T> {
        url = this.extractUrl(url);

        return this.http.delete(url, this.headers(token))
            .pipe(
                catchError((error) => this.showError(error, 'DELETE')),
                map((res: Response) => {

                    this.snackBar.open('Element deleted successfully.', 'Ok', {
                        duration: 5,
                    });

                    if (res.hasOwnProperty('_body')) {
                        if ((res as any)._body == null || (res as any)._body.length < 1) {
                            // handle empty results
                            return undefined;
                        }
                    }
                    return res.json();
                }));
    }
}
