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

import { Component, OnInit, ViewChild, Input, OnChanges, SimpleChanges } from '@angular/core';
import GraphEditor from '@ustutt/grapheditor-webcomponent/lib/grapheditor';
import { Edge } from '@ustutt/grapheditor-webcomponent/lib/edge';
import { Node } from '@ustutt/grapheditor-webcomponent/lib/node';
import { ApiObject } from 'src/app/api/apiobject';
import { ApiService } from 'src/app/api/api.service';
import { Subscription } from 'rxjs';

const STYLE_TEMPLATE = {
    id: 'style',
    innerHTML: `
        svg {position: absolute;}
        .ghost {opacity: 0.5;}
        .node {fill: #cccccc}
        .node.application {fill: #005c99}
        .link-handle {display: none; fill: black; opacity: 0.1; transition:r 0.25s ease-out;}
        .edge-group:not(.includes) .link-handle {display: initial}
        .link-handle:hover {opacity: 0.7; r: 5;}
        .text {fill: black; font-size: 6pt; text-overflow: ellipsis; word-break: break-word}
        .text.title {font-size: 8pt; text-decoration: underline; text-overflow: ellipsis; word-break: break-all;}
        .text.version {word-break: break-all;}
        .node:not(.application):not(.selected).hovered {fill: #efefef;}
        .node.application.hovered {fill: #0099ff;}
        .hovered .link-handle {display: initial;}
        .node.selected {fill: #ccff99; }
        .includes .edge {stroke: #0099ff; stroke-width: 2}
        .includes .marker {fill: #0099ff}
        .highlight-outgoing .edge {stroke: red;}
        .highlight-incoming .edge {stroke: green;}
        .highlight-outgoing .marker {fill: red;}
        .highlight-incoming .marker {fill: green;}`
};

const APPLICATION_NODE_TEMPLATE = {
    id: 'application',
    innerHTML: `<circle r="20" cx="0" cy="0"></circle>`
};

const SERVICE_NODE_TEMPLATE = {
    id: 'default',
    innerHTML: `<rect width="100" height="60" x="-50" y="-30"></rect>
        <text class="text title" data-content="title" data-click="title" width="90" x="-45" y="-16"></text>
        <text class="text description" data-content="description" data-click="description" width="90" height="30" x="-45" y="-5"></text>
        <text class="text version" data-content="version" data-click="version" width="40" x="-45" y="25"></text>`
};

const ARROW_TEMPLATE = {
    id: 'arrow',
    innerHTML: `<path d="M -9 -5 L 1 0 L -9 5 z" />`
};

@Component({
    selector: 'mico-app-dependency-graph',
    templateUrl: './app-dependency-graph.component.html',
    styleUrls: ['./app-dependency-graph.component.css']
})
export class AppDependencyGraphComponent implements OnInit, OnChanges {
    @ViewChild('graph') graph;
    @Input() shortName: string;
    @Input() version: string;

    appSubscription: Subscription;

    private lastX = 0;

    private nodeMap: Map<string, Node>;
    private edgeMap: Map<string, Edge>;

    constructor(private api: ApiService) {}

    ngOnInit() {
        if (this.graph == null) {
            console.warn("Graph not in dom!");
        }
        const graph: GraphEditor = this.graph.nativeElement;
        graph.setNodeClass = (className, node) => {
            if (className === node.type) {
                return true;
            }
            return false;
        };
        graph.setEdgeClass = (className, edge) => {
            if (className === edge.type) {
                return true;
            }
            return false;
        };
        graph.addEventListener('nodeclick', (event) => {
            if ((event as any).detail.node.id === 'APPLICATION') {
                event.preventDefault();
            }
        });
        graph.updateTemplates([SERVICE_NODE_TEMPLATE, APPLICATION_NODE_TEMPLATE], [STYLE_TEMPLATE], [ARROW_TEMPLATE]);
        graph.onCreateDraggedEdge = (edge) => {
            edge.markers = [{template: 'arrow', positionOnLine: 1, scale: 0.5, rotate: {relativeAngle: 0}}, ];
            return edge;
        };
        this.resetGraph();

    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.shortName != null || changes.version != null) {
            this.resetGraph();
            if (this.appSubscription != null) {
                this.appSubscription.unsubscribe();
            }
            if (this.shortName != null && this.version != null) {
                this.appSubscription = this.api.getApplication(this.shortName, this.version).subscribe(application => {
                    this.updateApplicationData(application);
                });
            }
        }
    }

    resetGraph() {
        this.nodeMap = new Map<string, Node>();
        this.edgeMap = new Map<string, Edge>();
        this.lastX = 0;
        const graph: GraphEditor = this.graph.nativeElement;

        graph.setNodes([]);
        graph.setEdges([]);

        graph.completeRender();
        graph.zoomToBoundingBox(false);
    }

    updateApplicationData(application) {
        const nodeMap = this.nodeMap;
        const edgeMap = this.edgeMap;
        const graph: GraphEditor = this.graph.nativeElement;

        const toDelete: Set<string> = new Set<string>();
        nodeMap.forEach(node => {
            if (node.id !== 'APPLICATION') {
                toDelete.add(node.id as string);
            }
        });

        if (!nodeMap.has('APPLICATION')) {
            const node: Node = {
                id: 'APPLICATION',
                x: 0,
                y: 0,
                type: 'application',
                title: application.name != null ? application.name : application.shortName,
                version: application.version,
                shortName: application.shortName,
                name: application.name,
                description: application.description,
                application: application,
            };
            nodeMap.set('APPLICATION', node);
            graph.addNode(node, false);
        }
        application.services.forEach((service) => {
            const serviceId = `${service.shortName}-${service.version}`;
            toDelete.delete(serviceId);
            if (nodeMap.has(serviceId)) {
                const node = nodeMap.get(serviceId);
                node.title = service.name != null ? service.name : service.shortName;
                node.version = service.version;
                node.shortName = service.shortName;
                node.name = service.name;
                node.description = service.description;
                node.service = service;
            } else {
                const node: Node = {
                    id: serviceId,
                    x: this.lastX,
                    y: 90,
                    type: 'service',
                    title: service.name != null ? service.name : service.shortName,
                    version: service.version,
                    shortName: service.shortName,
                    name: service.name,
                    description: service.description,
                };
                this.lastX += 110;
                nodeMap.set(serviceId, node);
                graph.addNode(node, false);
                const edge: Edge = {
                    source: 'APPLICATION',
                    target: serviceId,
                    type: 'includes',
                    markers: [{
                        template: 'arrow',
                        positionOnLine: 1,
                        scale: 1,
                        rotate: {
                            relativeAngle: 0,
                        },
                    }],
                };
                edgeMap.set(`sAPPLICATION-t${serviceId}`, edge);
                graph.addEdge(edge, false);
            }
        });

        toDelete.forEach(nodeId => {
            const node = nodeMap.get(nodeId);
            graph.removeNode(node);
            nodeMap.delete(nodeId);
        });

        graph.completeRender();
        graph.zoomToBoundingBox(false);
    }

    autozoom() {
        if (this.graph == null) {
            return;
        }

        this.graph.nativeElement.zoomToBoundingBox(false);
    }
}
