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

import { Node } from '@ustutt/grapheditor-webcomponent/lib/node';
import { ApiObject } from 'src/app/api/apiobject';


export const STYLE_TEMPLATE = {
    id: 'style',
    innerHTML: `
        svg {
            position: absolute;
        }
        .ghost {
            opacity: 0.5;
        }
        .node {
            fill: #cccccc;
        }
        .node.application {
            fill: #005c99;
        }
        .link-handle {
            display: none;
            fill: black;
            opacity: 0.1;
            transition:r 0.25s ease-out;
        }
        .edge-group:not(.provides) .link-handle {
            display: initial
        }
        .link-handle:hover {
            opacity: 0.7; r: 5;
        }
        .text {
            fill: black;
            font-size: 6pt;
            text-overflow: ellipsis;
            word-break: break-word;
        }
        .application .text {
            fill: white;
        }
        .application.undeployed .deployment-indicator {
            fill: white;
        }
        .application.error .deployment-indicator {
            fill: red;
        }
        .application.deployed .deployment-indicator {
            fill: lightgreen;
        }
        .application.pending .deployment-indicator {
            fill: gold;
            animation-name: pending;
            animation-duration: 2s;
            animation-iteration-count: infinite;
            animation-timing-function: linear;
        }
        @keyframes pending {
            20% {opacity: 0.2};
            50% {opacity: 1};
            10% {opacity: 1};
        }
        .text.title {
            font-size: 8pt;
            text-decoration: underline;
            text-overflow: ellipsis;
            word-break: break-all;
        }
        .service .title {
            cursor: pointer;
        }
        .text.interface-name {
            text-overflow: ellipsis;
            word-break: break-all;
        }
        .text.protocol {
            opacity: 0.75;
            font-size: 5pt;
            text-overflow: ellipsis;
            word-break: break-all;
        }
        .text.instance-id {
            word-break: break-all;
        }
        .text.small {
            font-size: 5pt;
        }
        .text.version {
            word-break: break-all;
            cursor: pointer;
        }
        .text.version:hover {
            text-decoration: underline;
        }
        .text.clickable {
            cursor: pointer;
        }
        .text.clickable:hover {
            text-decoration: underline dashed;
        }
        .node .warning-label {
            display: none;
        }
        .node.error .warning-label {
            display: initial;
        }
        .warning-background {
            fill: white;
        }
        .warning-foreground {
            fill: red;
        }
        .configure-foreground {
            fill: black
        }
        .node:not(.application):not(.selected).hovered {
            fill: #dfdfdf;
        }
        .node.application.hovered {
            fill: #0099ff;
        }
        .node .foreground {
            fill: #dfdfdf;
        }
        .node:not(.selected).hovered .foreground {
            fill: #efefef;
        }
        .hovered:not(.service-interface) .link-handle {
            display: initial;
        }
        .node.selected {
            fill: #ccff99;
        }
        .edge {
            stroke-linecap: round;
        }
        .includes .edge {
            stroke: #0099ff;
            stroke-width: 2;
        }
        .includes .marker {
            fill: #0099ff;
        }
        .provides .edge {
            stroke: #00ff33;
        }
        .interface-connection .edge {
            stroke-dasharray: 3 2;
        }
        .highlight-outgoing .edge {
            stroke: red;
        }
        .highlight-incoming .edge {
            stroke: green;
        }
        .highlight-outgoing .marker {
            fill: red;
        }
        .highlight-incoming .marker {
            fill: green;
        }
        .highlight-outgoing .text {
            fill: red;
        }
        .highlight-incoming .text {
            fill: green;
        }`
};

export const APPLICATION_NODE_TEMPLATE = {
    id: 'application',
    innerHTML: `<polygon points="-49,-15 0,-15 49,-15 58,0 49,15 0,15 -49,15 -58,0" data-link-handles="corners"></polygon>
        <text class="text title" data-content="title" data-click="title" width="90" x="-45" y="-3"></text>
        <circle class="deployment-indicator" cx="-42" cy="6" r="3.5"></circle>
        <text class="text" data-content="status.value" width="80" x="-35" y="9"></text>`
};

export const SERVICE_INTERFACE_NODE_TEMPLATE = {
    id: 'service-interface',
    innerHTML: `<circle r=20></circle>
        <text class="text interface-name" data-content="title" data-click="title" width="34" text-anchor="middle" x="0" y="0"></text>
        <text class="text protocol" data-content="protocol" data-click="title" width="30" text-anchor="middle" x="0" y="10">PPP</text>`
};

export const KAFKA_TOPIC_NODE_TEMPLATE = {
    id: 'kafka-topic',
    innerHTML: `<polygon points="-29,0 -28,-6 28,-6 29,0 28,6 -28,6" data-link-handles="corners"></polygon>
    <ellipse class="background" cx="25" cy="0" rx="4" ry="9"/>
    <rect class="background" x="-25" y="-9" width="50" height="18"></rect>
    <ellipse class="foreground" cx="-25" cy="0" rx="4" ry="9"/>
    <text class="text title" data-content="title" data-click="title" width="50" text-anchor="middle" x="2" y="3"></text>
    <title data-content="title"></title>`
};



// export const KAFKA_FAAS_CONNECTOR_NODE_TEMPLATE = {
//     id: 'kafka-faas-connector',
//     innerHTML: `<polygon points="-32.1,0 -32,-8 32,-8 33,0 32,8 -32,8" data-link-handles="corners"></polygon>
//     <path d="M-38 -15 L28 -15 A 6 15 0 0 1 28 15 L-38 15 A 6 15 0 0 0 -38 -15 Z" />
//     <text class="text title clickable" data-content="title" data-click="faas-function" width="55" x="-27" y="-3"></text>
//     <title data-content="title"></title>
//     <g>
//         <text class="text version small" data-content="data.version" data-click="version" width="24" x="-27" y="10"></text>
//         <title data-content="data.version"></title>
//     </g>
//     <g>
//         <text class="text instance-id small" data-content="data.instanceId" width="24" x="0" y="10"></text>
//         <title data-content="data.instanceId"></title>
//     </g>
//     <g class="warning-label" transform="scale(0.4) translate(55,-35)">
//         <circle class="warning-background" cx="12" cy="12" r="11.5"></circle>
//         <path class="warning-foreground" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"></path>
//         <title data-content="error"></title>
//     </g>`
// };


export const KAFKA_FAAS_CONNECTOR_NODE_TEMPLATE = {
    id: 'kafka-faas-connector',
    innerHTML: `<polygon points="0,44 0,12 55,12 55,44 55,28 0,28" fill="white" data-link-handles="corners"></polygon>
    <img class="clickable" data-href="data.patternIconURL" data-click="pattern-config" width="55" height="55" x="0" y="0"></image>
    <title data-content="title"></title>
    <g>
        <text class="text instance-id small clickable" data-content="data.configurationButtonText" data-click="pattern-config" width="55" x="0" y="50"></text>
        <title data-content="data.configurationButtonText"></title>
     </g>
    <g>
        <text class="text instance-id small clickable" data-content="data.deleteButtonText" data-click="delete-pattern" width="55" x="35" y="50"></text>
        <title data-content="data.deleteButtonText"></title>
     </g>`
};


export const SERVICE_NODE_TEMPLATE = {
    id: 'default',
    innerHTML: `<rect width="100" height="60" x="-50" y="-30"></rect>
        <text class="text title" data-content="title" data-click="title" width="90" x="-45" y="-16"></text>
        <text class="text description" data-content="description" data-click="description" width="90" height="30" x="-45" y="-5"></text>
        <text class="text version" data-content="version" data-click="version" width="40" x="-45" y="25"></text>`
};

export const ARROW_TEMPLATE = {
    id: 'arrow',
    innerHTML: `<path d="M -9 -5 L 1 0 L -9 5 z" />`
};

export interface ServiceNode extends Node {
    name: string;
    version: string;
    shortName: string;
    description: string;
    interfaces: Set<string>;
    service: ApiObject;
}

export interface ServiceInterfaceNode extends Node {
    dx: number;
    dy: number;
    name: string;
    serviceId: string;
    description: string;
    protocol: string;
}

export interface ApplicationNode extends Node {
    name: string;
    version: string;
    shortName: string;
    description: string;
    status?: any;
}

