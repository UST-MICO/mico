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
        .edge-group:not(.includes) .link-handle {
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
        .text.title {
            font-size: 8pt;
            text-decoration: underline;
            text-overflow: ellipsis;
            word-break: break-all;
        }
        .text.version {
            word-break: break-all;
        }
        .node:not(.application):not(.selected).hovered {
            fill: #efefef;
        }
        .node.application.hovered {
            fill: #0099ff;
        }
        .hovered .link-handle {
            display: initial;
        }
        .node.selected {
            fill: #ccff99;
        }
        .includes .edge {
            stroke: #0099ff;
            stroke-width: 2
        }
        .includes .marker {
            fill: #0099ff
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
        }`
};

export const APPLICATION_NODE_TEMPLATE = {
    id: 'application',
    innerHTML: `<circle r="20" cx="0" cy="0"></circle>`
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
