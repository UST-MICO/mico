import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions, ResponseContentType } from '@angular/http';
import { Observable } from 'rxjs';
import { map, } from 'rxjs/operators';
import { ApiObject, ApiLinksObject, LinkObject, isApiObject, isApiLinksObject, isLinkObject } from './apiobject';



@Injectable({
    providedIn: 'root'
})
export class ApiBaseFunctionService {

    constructor(private http: Http) { }

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
        if (!url.endsWith('/')) {
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


    get<T>(url: string | LinkObject | ApiLinksObject | ApiObject, token?: string, params?): Observable<T> {
        url = this.extractUrl(url);

        const options = this.headers(token);
        if (params != null) {
            options.params = params;
        }

        const request = this.http.get(url, options).pipe(map((res: Response) => {
            return res.json();
        }));

        return request;
    }


    post<T>(url: string | LinkObject | ApiLinksObject | ApiObject, data, token?: string, isJson = true): Observable<T> {
        url = this.extractUrl(url);
        let tempData = data;
        if (isJson) {
            tempData = JSON.stringify(tempData);
        }
        return this.http.post(url, tempData, this.headers(token))
            .pipe(map((res: Response) => {
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
            .pipe(map((res: Response) => {
                return res.json();
            }));
    }
}
