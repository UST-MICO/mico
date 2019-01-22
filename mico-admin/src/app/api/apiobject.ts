export interface ApiObject {
    _links: ApiLinksObject;
    _embedded?: {
        [prop: string]: ApiObject[];
    }
    [propName: string]: any;
}

export interface LinkObject {
    readonly href: string;
    readonly templated?: boolean;
}

export interface ApiLinksObject {
    readonly self: LinkObject;
    [propName: string]: LinkObject;
}


export function isApiObject(toTest: any): toTest is ApiObject {
    return '_links' in toTest;
}

export function isApiLinksObject(toTest: any): toTest is ApiLinksObject {
    return 'self' in toTest;
}

export function isLinkObject(toTest: any): toTest is LinkObject {
    return 'href' in toTest;
}
