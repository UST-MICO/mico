import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions, ResponseContentType } from '@angular/http';
import { Observable } from 'rxjs';
import { map, } from 'rxjs/operators';


// TODO remove API Object ... (use the other one)
export interface LinkObject {
    readonly href: string;
    readonly templated?: boolean;
}

export interface ApiLinksObject {
    readonly self: LinkObject;
    [propName: string]: LinkObject;
}

export interface ApiObject {
    readonly _links: ApiLinksObject;
    [propName: string]: any;
}

function isApiObject(toTest: any): toTest is ApiObject {
    return '_links' in toTest;
}

function isApiLinksObject(toTest: any): toTest is ApiLinksObject {
    return 'self' in toTest;
}

function isLinkObject(toTest: any): toTest is LinkObject {
    return 'href' in toTest;
}

@Injectable({
    providedIn: 'root'
})
export class ApiBaseFunctionService {

    private base: string = 'http://localhost:8080/'; // (window as any).basePath;

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
        return this.prepareRelativeUrl(url);
    }

    private prepareRelativeUrl(url: string): string {
        if (url.startsWith('http')) {
            return url;
        }
        let url_string: string = this.base;
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


    get(url: string | LinkObject | ApiLinksObject | ApiObject, token?: string, params?): Observable<ApiObject> {
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

}
