import { Injectable } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { filter, flatMap } from 'rxjs/operators';
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

    getModelDefinitions() {
        const resource = 'models';
        const stream = this.getStreamSource(resource);

        // TODO replace URL with a generic path
        /*
        this.rest.get('http://localhost:8080/v2/api-docs').subscribe(val => {
            // return actual service list
            stream.next((val as ApiObject).definitions as ApiObject);
        });
        */

        // TODO replace with api call
        const model = JSON.parse('{"swagger":"2.0","info":{"description":"A Management System for Microservice Compositions","version":"0.1","title":"MICO"},"host":"localhost:8080","basePath":"/","tags":[{"name":"application-controller","description":"Application Controller"},{"name":"basic-error-controller","description":"Basic Error Controller"},{"name":"operation-handler","description":"Operation Handler"},{"name":"service-controller","description":"Service Controller"},{"name":"service-interface-controller","description":"Service Interface Controller"},{"name":"web-mvc-links-handler","description":"Web Mvc Links Handler"}],"paths":{"/actuator":{"get":{"tags":["web-mvc-links-handler"],"summary":"links","operationId":"linksUsingGET","produces":["application/json","application/vnd.spring-boot.actuator.v2+json"],"responses":{"200":{"description":"OK","schema":{"type":"object","additionalProperties":{"type":"object","additionalProperties":{"$ref":"#/definitions/Link"}}}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false}},"/actuator/health":{"get":{"tags":["operation-handler"],"summary":"handle","operationId":"handleUsingGET_2","produces":["application/json","application/vnd.spring-boot.actuator.v2+json"],"parameters":[{"in":"body","name":"body","description":"body","required":false,"schema":{"type":"object","additionalProperties":{"type":"string"}}}],"responses":{"200":{"description":"OK","schema":{"type":"object"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false}},"/actuator/health/{component}":{"get":{"tags":["operation-handler"],"summary":"handle","operationId":"handleUsingGET_1","produces":["application/json","application/vnd.spring-boot.actuator.v2+json"],"parameters":[{"in":"body","name":"body","description":"body","required":false,"schema":{"type":"object","additionalProperties":{"type":"string"}}}],"responses":{"200":{"description":"OK","schema":{"type":"object"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false}},"/actuator/health/{component}/{instance}":{"get":{"tags":["operation-handler"],"summary":"handle","operationId":"handleUsingGET","produces":["application/json","application/vnd.spring-boot.actuator.v2+json"],"parameters":[{"in":"body","name":"body","description":"body","required":false,"schema":{"type":"object","additionalProperties":{"type":"string"}}}],"responses":{"200":{"description":"OK","schema":{"type":"object"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false}},"/actuator/info":{"get":{"tags":["operation-handler"],"summary":"handle","operationId":"handleUsingGET_3","produces":["application/json","application/vnd.spring-boot.actuator.v2+json"],"parameters":[{"in":"body","name":"body","description":"body","required":false,"schema":{"type":"object","additionalProperties":{"type":"string"}}}],"responses":{"200":{"description":"OK","schema":{"type":"object"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false}},"/applications":{"get":{"tags":["application-controller"],"summary":"getAllApplications","operationId":"getAllApplicationsUsingGET","produces":["application/hal+json"],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourcesOfResourceOfApplication"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false},"post":{"tags":["application-controller"],"summary":"createApplication","operationId":"createApplicationUsingPOST","consumes":["application/json"],"produces":["application/hal+json"],"parameters":[{"in":"body","name":"newApplication","description":"newApplication","required":true,"schema":{"$ref":"#/definitions/Application"}}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourceOfApplication"}},"201":{"description":"Created"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false}},"/applications/{shortName}/{version}":{"get":{"tags":["application-controller"],"summary":"getApplicationByShortNameAndVersion","operationId":"getApplicationByShortNameAndVersionUsingGET","produces":["application/hal+json"],"parameters":[{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourceOfApplication"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false},"put":{"tags":["application-controller"],"summary":"updateApplication","operationId":"updateApplicationUsingPUT","consumes":["application/json"],"produces":["application/hal+json"],"parameters":[{"in":"body","name":"application","description":"application","required":true,"schema":{"$ref":"#/definitions/Application"}},{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourceOfApplication"}},"201":{"description":"Created"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false}},"/error":{"get":{"tags":["basic-error-controller"],"summary":"errorHtml","operationId":"errorHtmlUsingGET","produces":["text/html"],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ModelAndView"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false},"head":{"tags":["basic-error-controller"],"summary":"errorHtml","operationId":"errorHtmlUsingHEAD","consumes":["application/json"],"produces":["text/html"],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ModelAndView"}},"204":{"description":"No Content"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"}},"deprecated":false},"post":{"tags":["basic-error-controller"],"summary":"errorHtml","operationId":"errorHtmlUsingPOST","consumes":["application/json"],"produces":["text/html"],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ModelAndView"}},"201":{"description":"Created"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false},"put":{"tags":["basic-error-controller"],"summary":"errorHtml","operationId":"errorHtmlUsingPUT","consumes":["application/json"],"produces":["text/html"],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ModelAndView"}},"201":{"description":"Created"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false},"delete":{"tags":["basic-error-controller"],"summary":"errorHtml","operationId":"errorHtmlUsingDELETE","produces":["text/html"],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ModelAndView"}},"204":{"description":"No Content"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"}},"deprecated":false},"options":{"tags":["basic-error-controller"],"summary":"errorHtml","operationId":"errorHtmlUsingOPTIONS","consumes":["application/json"],"produces":["text/html"],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ModelAndView"}},"204":{"description":"No Content"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"}},"deprecated":false},"patch":{"tags":["basic-error-controller"],"summary":"errorHtml","operationId":"errorHtmlUsingPATCH","consumes":["application/json"],"produces":["text/html"],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ModelAndView"}},"204":{"description":"No Content"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"}},"deprecated":false}},"/services":{"get":{"tags":["service-controller"],"summary":"getServiceList","operationId":"getServiceListUsingGET","produces":["application/hal+json"],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourcesOfResourceOfService"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false},"post":{"tags":["service-controller"],"summary":"createService","operationId":"createServiceUsingPOST","consumes":["application/json"],"produces":["application/hal+json"],"parameters":[{"in":"body","name":"newService","description":"newService","required":true,"schema":{"$ref":"#/definitions/Service"}}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourceOfService"}},"201":{"description":"Created"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false}},"/services/{shortName}/":{"get":{"tags":["service-controller"],"summary":"getVersionsOfService","operationId":"getVersionsOfServiceUsingGET","produces":["application/hal+json"],"parameters":[{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourcesOfResourceOfService"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false}},"/services/{shortName}/{version}":{"get":{"tags":["service-controller"],"summary":"getServiceByShortNameAndVersion","operationId":"getServiceByShortNameAndVersionUsingGET","produces":["application/hal+json"],"parameters":[{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourceOfService"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false},"put":{"tags":["service-controller"],"summary":"updateService","operationId":"updateServiceUsingPUT","consumes":["application/json"],"produces":["application/hal+json"],"parameters":[{"in":"body","name":"service","description":"service","required":true,"schema":{"$ref":"#/definitions/Service"}},{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourceOfService"}},"201":{"description":"Created"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false},"delete":{"tags":["service-controller"],"summary":"deleteService","operationId":"deleteServiceUsingDELETE","produces":["application/hal+json"],"parameters":[{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResponseEntity"}},"204":{"description":"No Content"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"}},"deprecated":false}},"/services/{shortName}/{version}/dependees":{"get":{"tags":["service-controller"],"summary":"getDependees","operationId":"getDependeesUsingGET","produces":["application/hal+json"],"parameters":[{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourcesOfResourceOfService"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false},"post":{"tags":["service-controller"],"summary":"createNewDependee","operationId":"createNewDependeeUsingPOST","consumes":["application/json"],"produces":["application/hal+json"],"parameters":[{"in":"body","name":"newServiceDependee","description":"newServiceDependee","required":true,"schema":{"$ref":"#/definitions/DependsOn"}},{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourceOfService"}},"201":{"description":"Created"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false},"delete":{"tags":["service-controller"],"summary":"deleteAllDependees","operationId":"deleteAllDependeesUsingDELETE","produces":["application/hal+json"],"parameters":[{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourceOfService"}},"204":{"description":"No Content"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"}},"deprecated":false}},"/services/{shortName}/{version}/dependees/{shortNameToDelete}/{versionToDelete}":{"delete":{"tags":["service-controller"],"summary":"deleteDependee","operationId":"deleteDependeeUsingDELETE","produces":["application/hal+json"],"parameters":[{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"shortNameToDelete","in":"path","description":"shortNameToDelete","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"},{"name":"versionToDelete","in":"path","description":"versionToDelete","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourceOfService"}},"204":{"description":"No Content"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"}},"deprecated":false}},"/services/{shortName}/{version}/dependers":{"get":{"tags":["service-controller"],"summary":"getDependers","operationId":"getDependersUsingGET","produces":["application/hal+json"],"parameters":[{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourcesOfResourceOfService"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false}},"/services/{shortName}/{version}/interfaces/":{"get":{"tags":["service-interface-controller"],"summary":"getInterfacesOfService","operationId":"getInterfacesOfServiceUsingGET","produces":["application/hal+json"],"parameters":[{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourcesOfResourceOfServiceInterface"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false},"post":{"tags":["service-interface-controller"],"summary":"createServiceInterface","operationId":"createServiceInterfaceUsingPOST","consumes":["application/json"],"produces":["application/hal+json"],"parameters":[{"in":"body","name":"serviceInterface","description":"serviceInterface","required":true,"schema":{"$ref":"#/definitions/ServiceInterface"}},{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourceOfServiceInterface"}},"201":{"description":"Created"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false}},"/services/{shortName}/{version}/interfaces/{serviceInterfaceName}":{"get":{"tags":["service-interface-controller"],"summary":"getInterfaceByName","operationId":"getInterfaceByNameUsingGET","produces":["application/hal+json"],"parameters":[{"name":"serviceInterfaceName","in":"path","description":"serviceInterfaceName","required":true,"type":"string"},{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK","schema":{"$ref":"#/definitions/ResourceOfServiceInterface"}},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"},"404":{"description":"Not Found"}},"deprecated":false},"delete":{"tags":["service-interface-controller"],"summary":"deleteServiceInterface","operationId":"deleteServiceInterfaceUsingDELETE","produces":["application/hal+json"],"parameters":[{"name":"serviceInterfaceName","in":"path","description":"serviceInterfaceName","required":true,"type":"string"},{"name":"shortName","in":"path","description":"shortName","required":true,"type":"string"},{"name":"version","in":"path","description":"version","required":true,"type":"string"}],"responses":{"200":{"description":"OK"},"204":{"description":"No Content"},"401":{"description":"Unauthorized"},"403":{"description":"Forbidden"}},"deprecated":false}}},"definitions":{"Application":{"type":"object","required":["description","name","shortName","version"],"properties":{"contact":{"type":"string"},"crawlingSource":{"type":"string","enum":["GITHUB","DOCKER","NOT_DEFINED"]},"dependsOn":{"type":"array","items":{"$ref":"#/definitions/DependsOn"}},"deployInformation":{"type":"string"},"description":{"type":"string"},"dockerImageName":{"type":"string"},"dockerImageUri":{"type":"string"},"dockerfile":{"type":"string"},"externalVersion":{"type":"string"},"id":{"type":"integer","format":"int64"},"lifecycle":{"type":"string"},"links":{"type":"array","items":{"type":"string"}},"name":{"type":"string"},"owner":{"type":"string"},"predecessor":{"$ref":"#/definitions/Service"},"serviceInterfaces":{"type":"array","items":{"$ref":"#/definitions/ServiceInterface"}},"serviceLinks":{"type":"array","items":{"type":"string"}},"shortName":{"type":"string"},"tags":{"type":"array","items":{"type":"string"}},"type":{"type":"string"},"vcsRoot":{"type":"string"},"version":{"type":"string"},"visualizationData":{"type":"string"}},"title":"Application"},"DependsOn":{"type":"object","properties":{"maxVersion":{"type":"string"},"minVersion":{"type":"string"},"serviceDependee":{"$ref":"#/definitions/Service"}},"title":"DependsOn"},"Link":{"type":"object","properties":{"href":{"type":"string"},"templated":{"type":"boolean"}},"title":"Link"},"MapOfstringAndLink":{"type":"object","title":"MapOfstringAndLink","additionalProperties":{"$ref":"#/definitions/Link"}},"ModelAndView":{"type":"object","properties":{"empty":{"type":"boolean"},"model":{"type":"object"},"modelMap":{"type":"object","additionalProperties":{"type":"object"}},"reference":{"type":"boolean"},"status":{"type":"string","enum":["100 CONTINUE","101 SWITCHING_PROTOCOLS","102 PROCESSING","103 CHECKPOINT","200 OK","201 CREATED","202 ACCEPTED","203 NON_AUTHORITATIVE_INFORMATION","204 NO_CONTENT","205 RESET_CONTENT","206 PARTIAL_CONTENT","207 MULTI_STATUS","208 ALREADY_REPORTED","226 IM_USED","300 MULTIPLE_CHOICES","301 MOVED_PERMANENTLY","302 FOUND","302 MOVED_TEMPORARILY","303 SEE_OTHER","304 NOT_MODIFIED","305 USE_PROXY","307 TEMPORARY_REDIRECT","308 PERMANENT_REDIRECT","400 BAD_REQUEST","401 UNAUTHORIZED","402 PAYMENT_REQUIRED","403 FORBIDDEN","404 NOT_FOUND","405 METHOD_NOT_ALLOWED","406 NOT_ACCEPTABLE","407 PROXY_AUTHENTICATION_REQUIRED","408 REQUEST_TIMEOUT","409 CONFLICT","410 GONE","411 LENGTH_REQUIRED","412 PRECONDITION_FAILED","413 PAYLOAD_TOO_LARGE","413 REQUEST_ENTITY_TOO_LARGE","414 URI_TOO_LONG","414 REQUEST_URI_TOO_LONG","415 UNSUPPORTED_MEDIA_TYPE","416 REQUESTED_RANGE_NOT_SATISFIABLE","417 EXPECTATION_FAILED","418 I_AM_A_TEAPOT","419 INSUFFICIENT_SPACE_ON_RESOURCE","420 METHOD_FAILURE","421 DESTINATION_LOCKED","422 UNPROCESSABLE_ENTITY","423 LOCKED","424 FAILED_DEPENDENCY","426 UPGRADE_REQUIRED","428 PRECONDITION_REQUIRED","429 TOO_MANY_REQUESTS","431 REQUEST_HEADER_FIELDS_TOO_LARGE","451 UNAVAILABLE_FOR_LEGAL_REASONS","500 INTERNAL_SERVER_ERROR","501 NOT_IMPLEMENTED","502 BAD_GATEWAY","503 SERVICE_UNAVAILABLE","504 GATEWAY_TIMEOUT","505 HTTP_VERSION_NOT_SUPPORTED","506 VARIANT_ALSO_NEGOTIATES","507 INSUFFICIENT_STORAGE","508 LOOP_DETECTED","509 BANDWIDTH_LIMIT_EXCEEDED","510 NOT_EXTENDED","511 NETWORK_AUTHENTICATION_REQUIRED"]},"view":{"$ref":"#/definitions/View"},"viewName":{"type":"string"}},"title":"ModelAndView"},"ResourceOfApplication":{"type":"object","required":["description","name","shortName","version"],"properties":{"_links":{"type":"array","xml":{"name":"link","attribute":false,"wrapped":false},"items":{"$ref":"#/definitions/Link"}},"contact":{"type":"string"},"crawlingSource":{"type":"string","enum":["GITHUB","DOCKER","NOT_DEFINED"]},"dependsOn":{"type":"array","items":{"$ref":"#/definitions/DependsOn"}},"deployInformation":{"type":"string"},"description":{"type":"string"},"dockerImageName":{"type":"string"},"dockerImageUri":{"type":"string"},"dockerfile":{"type":"string"},"externalVersion":{"type":"string"},"id":{"type":"integer","format":"int64"},"lifecycle":{"type":"string"},"name":{"type":"string"},"owner":{"type":"string"},"predecessor":{"$ref":"#/definitions/Service"},"serviceInterfaces":{"type":"array","items":{"$ref":"#/definitions/ServiceInterface"}},"serviceLinks":{"type":"array","items":{"type":"string"}},"shortName":{"type":"string"},"tags":{"type":"array","items":{"type":"string"}},"type":{"type":"string"},"vcsRoot":{"type":"string"},"version":{"type":"string"},"visualizationData":{"type":"string"}},"title":"ResourceOfApplication"},"ResourceOfService":{"type":"object","required":["description","name","shortName","version"],"properties":{"_links":{"type":"array","xml":{"name":"link","attribute":false,"wrapped":false},"items":{"$ref":"#/definitions/Link"}},"contact":{"type":"string"},"crawlingSource":{"type":"string","enum":["GITHUB","DOCKER","NOT_DEFINED"]},"dependsOn":{"type":"array","items":{"$ref":"#/definitions/DependsOn"}},"description":{"type":"string"},"dockerImageName":{"type":"string"},"dockerImageUri":{"type":"string"},"dockerfile":{"type":"string"},"externalVersion":{"type":"string"},"id":{"type":"integer","format":"int64"},"lifecycle":{"type":"string"},"name":{"type":"string"},"owner":{"type":"string"},"predecessor":{"$ref":"#/definitions/Service"},"serviceInterfaces":{"type":"array","items":{"$ref":"#/definitions/ServiceInterface"}},"serviceLinks":{"type":"array","items":{"type":"string"}},"shortName":{"type":"string"},"tags":{"type":"array","items":{"type":"string"}},"type":{"type":"string"},"vcsRoot":{"type":"string"},"version":{"type":"string"}},"title":"ResourceOfService"},"ResourceOfServiceInterface":{"type":"object","properties":{"_links":{"type":"array","xml":{"name":"link","attribute":false,"wrapped":false},"items":{"$ref":"#/definitions/Link"}},"description":{"type":"string"},"port":{"type":"string"},"protocol":{"type":"string"},"publicDns":{"type":"string"},"serviceInterfaceName":{"type":"string"},"transportProtocol":{"type":"string"},"type":{"type":"string"}},"title":"ResourceOfServiceInterface"},"ResourcesOfResourceOfApplication":{"type":"object","properties":{"_embedded":{"type":"array","xml":{"name":"embedded","attribute":false,"wrapped":true},"items":{"$ref":"#/definitions/ResourceOfApplication"}},"_links":{"type":"array","xml":{"name":"link","attribute":false,"wrapped":false},"items":{"$ref":"#/definitions/Link"}}},"title":"ResourcesOfResourceOfApplication","xml":{"name":"entities","attribute":false,"wrapped":false}},"ResourcesOfResourceOfService":{"type":"object","properties":{"_embedded":{"type":"array","xml":{"name":"embedded","attribute":false,"wrapped":true},"items":{"$ref":"#/definitions/ResourceOfService"}},"_links":{"type":"array","xml":{"name":"link","attribute":false,"wrapped":false},"items":{"$ref":"#/definitions/Link"}}},"title":"ResourcesOfResourceOfService"},"ResourcesOfResourceOfServiceInterface":{"type":"object","properties":{"_embedded":{"type":"array","xml":{"name":"embedded","attribute":false,"wrapped":true},"items":{"$ref":"#/definitions/ResourceOfServiceInterface"}},"_links":{"type":"array","xml":{"name":"link","attribute":false,"wrapped":false},"items":{"$ref":"#/definitions/Link"}}},"title":"ResourcesOfResourceOfServiceInterface","xml":{"name":"entities","attribute":false,"wrapped":false}},"ResponseEntity":{"type":"object","properties":{"body":{"type":"object"},"statusCode":{"type":"string","enum":["100 CONTINUE","101 SWITCHING_PROTOCOLS","102 PROCESSING","103 CHECKPOINT","200 OK","201 CREATED","202 ACCEPTED","203 NON_AUTHORITATIVE_INFORMATION","204 NO_CONTENT","205 RESET_CONTENT","206 PARTIAL_CONTENT","207 MULTI_STATUS","208 ALREADY_REPORTED","226 IM_USED","300 MULTIPLE_CHOICES","301 MOVED_PERMANENTLY","302 FOUND","302 MOVED_TEMPORARILY","303 SEE_OTHER","304 NOT_MODIFIED","305 USE_PROXY","307 TEMPORARY_REDIRECT","308 PERMANENT_REDIRECT","400 BAD_REQUEST","401 UNAUTHORIZED","402 PAYMENT_REQUIRED","403 FORBIDDEN","404 NOT_FOUND","405 METHOD_NOT_ALLOWED","406 NOT_ACCEPTABLE","407 PROXY_AUTHENTICATION_REQUIRED","408 REQUEST_TIMEOUT","409 CONFLICT","410 GONE","411 LENGTH_REQUIRED","412 PRECONDITION_FAILED","413 PAYLOAD_TOO_LARGE","413 REQUEST_ENTITY_TOO_LARGE","414 URI_TOO_LONG","414 REQUEST_URI_TOO_LONG","415 UNSUPPORTED_MEDIA_TYPE","416 REQUESTED_RANGE_NOT_SATISFIABLE","417 EXPECTATION_FAILED","418 I_AM_A_TEAPOT","419 INSUFFICIENT_SPACE_ON_RESOURCE","420 METHOD_FAILURE","421 DESTINATION_LOCKED","422 UNPROCESSABLE_ENTITY","423 LOCKED","424 FAILED_DEPENDENCY","426 UPGRADE_REQUIRED","428 PRECONDITION_REQUIRED","429 TOO_MANY_REQUESTS","431 REQUEST_HEADER_FIELDS_TOO_LARGE","451 UNAVAILABLE_FOR_LEGAL_REASONS","500 INTERNAL_SERVER_ERROR","501 NOT_IMPLEMENTED","502 BAD_GATEWAY","503 SERVICE_UNAVAILABLE","504 GATEWAY_TIMEOUT","505 HTTP_VERSION_NOT_SUPPORTED","506 VARIANT_ALSO_NEGOTIATES","507 INSUFFICIENT_STORAGE","508 LOOP_DETECTED","509 BANDWIDTH_LIMIT_EXCEEDED","510 NOT_EXTENDED","511 NETWORK_AUTHENTICATION_REQUIRED"]},"statusCodeValue":{"type":"integer","format":"int32"}},"title":"ResponseEntity"},"Service":{"type":"object","required":["description","name","shortName","version"],"properties":{"contact":{"type":"string"},"crawlingSource":{"type":"string","enum":["GITHUB","DOCKER","NOT_DEFINED"]},"dependsOn":{"type":"array","items":{"$ref":"#/definitions/DependsOn"}},"description":{"type":"string"},"dockerImageName":{"type":"string"},"dockerImageUri":{"type":"string"},"dockerfile":{"type":"string"},"externalVersion":{"type":"string"},"id":{"type":"integer","format":"int64"},"lifecycle":{"type":"string"},"links":{"type":"array","items":{"type":"string"}},"name":{"type":"string"},"owner":{"type":"string"},"predecessor":{"$ref":"#/definitions/Service"},"serviceInterfaces":{"type":"array","items":{"$ref":"#/definitions/ServiceInterface"}},"serviceLinks":{"type":"array","items":{"type":"string"}},"shortName":{"type":"string"},"tags":{"type":"array","items":{"type":"string"}},"type":{"type":"string"},"vcsRoot":{"type":"string"},"version":{"type":"string"}},"title":"Service"},"ServiceInterface":{"type":"object","properties":{"description":{"type":"string"},"port":{"type":"string"},"protocol":{"type":"string"},"publicDns":{"type":"string"},"serviceInterfaceName":{"type":"string"},"transportProtocol":{"type":"string"},"type":{"type":"string"}},"title":"ServiceInterface"},"View":{"type":"object","properties":{"contentType":{"type":"string"}},"title":"View"}}}');

        stream.next((model as ApiObject).definitions);

        return (stream.asObservable() as Observable<ApiObject>).pipe(
            filter(data => data !== undefined)
        );
    }


    // =================
    // APPLICATION CALLS
    // =================

    /**
     * Get application list
     */
    getApplications(): Observable<Readonly<ApiObject>> {

        const resource = 'applications';
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            // return actual application list
            stream.next(freezeObject((val as ApiObject)._embedded.applicationList as ApiObject));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
            filter(data => data !== undefined)
        );
    }

    getApplicationById(id): Observable<Readonly<ApiObject>> {
        // TODO check if there is a resource for single applications
        const resource = 'applications';
        const stream = this.getStreamSource(resource);

        // TODO
        const mockData: ApiObject = {
            '_links': 'self',
            'id': id,
            'name': 'Hello World Application id ' + id,
            'shortName': 'test.' + id + 'application',
            'description': 'A generic application',
        };

        stream.next(mockData);

        return (stream.asObservable() as Observable<ApiObject>).pipe(
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
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            stream.next(freezeObject((val as ApiObject)));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
            filter(data => data !== undefined)
        );
    }

    postApplication(data) {
        if (data == null) {
            return;
        }


        const resource = 'applications/';

        return this.rest.post(resource, data).pipe(flatMap(val => {

            const stream = this.getStreamSource(val._links.self.href);
            stream.next(val);

            this.getApplications();

            return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
                filter(service => service !== undefined)
            );
        }));
    }

    // =============
    // SERVICE CALLS
    // =============

    /**
     * Get service list
     */
    getServices(): Observable<Readonly<ApiObject>> {
        const resource = 'services';
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            // return actual service list
            stream.next(freezeObject((val as ApiObject)._embedded.serviceList as ApiObject));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
            filter(data => data !== undefined)
        );
    }

    /**
     * Get all versions of a service based on its shortName
     */
    getServiceVersions(shortName): Observable<ApiObject> {

        const resource = 'services/' + shortName + '/';
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            let list = (val as ApiObject)._embedded.serviceList;
            if (list === undefined) {
                list = (val as ApiObject)._embedded.applicationList;
            }
            stream.next(freezeObject(list as ApiObject));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
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

        return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
            filter(data => data !== undefined)
        );
    }

    postService(data): Observable<Readonly<ApiObject>> {

        if (data == null) {
            return;
        }


        const resource = 'services/';

        return this.rest.post(resource, data).pipe(flatMap(val => {

            const stream = this.getStreamSource(val._links.self.href);
            stream.next(val);

            this.getServices();
            this.getServiceVersions(data.shortName);

            return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
                filter(service => service !== undefined)
            );
        }));

    }

    putService(shortName, version, data): Observable<Readonly<ApiObject>> {

        if (data == null) {
            return;
        }

        const resource = 'services/' + shortName + '/' + version;

        return this.rest.put(resource, data).pipe(flatMap(val => {

            const stream = this.getStreamSource(val._links.self.href);
            stream.next(val);

            return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
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
    getServiceDependees(shortName, version): Observable<ApiObject> {

        const resource = 'services/' + shortName + '/' + version + '/dependees';
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            stream.next(freezeObject((val._embedded.serviceList as ApiObject)));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
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

        return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
            filter(data => data !== undefined)
        );
    }

    getServiceInterfaces(shortName, version): Observable<ApiObject> {
        const resource = 'services/' + shortName + '/' + version + '/interfaces';
        const stream = this.getStreamSource(resource);

        this.rest.get(resource).subscribe(val => {
            stream.next(freezeObject((val as ApiObject)));
        });

        return (stream.asObservable() as Observable<Readonly<ApiObject>>).pipe(
            filter(data => data !== undefined)
        );
    }
}
