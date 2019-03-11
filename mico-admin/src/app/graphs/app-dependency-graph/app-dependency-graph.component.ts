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
import { Edge, DraggedEdge } from '@ustutt/grapheditor-webcomponent/lib/edge';
import { Node } from '@ustutt/grapheditor-webcomponent/lib/node';
import { ApiObject } from 'src/app/api/apiobject';
import { ApiService } from 'src/app/api/api.service';
import { Subscription } from 'rxjs';
import { STYLE_TEMPLATE, APPLICATION_NODE_TEMPLATE, SERVICE_NODE_TEMPLATE, ARROW_TEMPLATE } from './app-dependency-graph-constants';
import { MatDialog } from '@angular/material';
import { ChangeServiceVersionComponent } from 'src/app/dialogs/change-service-version/change-service-version.component';
import { UtilsService } from 'src/app/util/utils.service';
import { debounceTime } from 'rxjs/operators';


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

    private versionChangedFor: { node: Node, newVersion: ApiObject };

    constructor(private api: ApiService,
        private dialog: MatDialog,
        private util: UtilsService,
    ) { }

    ngOnInit() {
        if (this.graph == null) {
            console.warn('Graph not in dom!');
        }
        const graph: GraphEditor = this.graph.nativeElement;
        graph.setNodeClass = (className, node) => {
            if (className === node.type) { // set node class according to node type
                return true;
            }
            return false;
        };
        graph.setEdgeClass = (className, edge) => {
            if (className === edge.type) { // set edge class according to edge type
                return true;
            }
            return false;
        };
        graph.addEventListener('nodeclick', this.onNodeClick);
        graph.onCreateDraggedEdge = this.onCreateDraggedEdge;
        graph.updateTemplates([SERVICE_NODE_TEMPLATE, APPLICATION_NODE_TEMPLATE], [STYLE_TEMPLATE], [ARROW_TEMPLATE]);
        this.resetGraph();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.shortName != null || changes.version != null) {
            this.resetGraph();

            this.util.safeUnsubscribe(this.appSubscription);

            if (this.shortName != null && this.version != null) {
                this.appSubscription = this.api.getApplication(this.shortName, this.version).subscribe(application => {
                    this.updateGraphFromApplicationData(application);
                });
            }
        }
    }

    /**
     * Handle node click events.
     */
    onNodeClick = (event: CustomEvent) => {
        if (event.detail.node.id === 'APPLICATION') {
            event.preventDefault();  // prevent selecting application node
            return;
        }
        if (event.detail.key === 'version') {  // user clicked on service version
            event.preventDefault();

            const dialogRef = this.dialog.open(ChangeServiceVersionComponent, {
                data: {
                    service: event.detail.node.service,
                }
            });

            dialogRef.afterClosed().subscribe((selected) => {
                if (selected == null || selected === '' || event.detail.node.service.version === selected.version) {
                    return;
                }
                this.changeServiceVersion(event.detail.node, selected);
            });
            return;
        }
    }

    /**
     * Replace an existing service node with a new service.
     *
     * @param node service node of the old service
     * @param newVersion new service version
     */
    changeServiceVersion(node: Node, newVersion: ApiObject) {
        this.versionChangedFor = {node: node, newVersion: newVersion};
        this.api.deleteApplicationServices(this.shortName, this.version, node.service.shortName).pipe(
            debounceTime(300),
        ).subscribe((success) => {
            if (!success) {
                return;
            }
            this.api.postApplicationServices(this.shortName, this.version, newVersion.shortName, newVersion.version).subscribe();
        });
    }

    /**
     * Update markerEnd and type of newly created edges.
     */
    onCreateDraggedEdge = (edge: DraggedEdge) => {
        edge.markerEnd = { template: 'arrow', positionOnLine: 1, lineOffset: 4, scale: 0.5, rotate: { relativeAngle: 0 } };
        edge.validTargets.clear();
        if (edge.source === 'APPLICATION') {
            edge.type = 'includes';
            edge.markerEnd.lineOffset = 8;
            edge.markerEnd.scale = 1;
        }
        return edge;
    }

    /**
     * Reset the graph (all edges and nodes) and clears cache/layout data.
     */
    resetGraph() {
        this.nodeMap = new Map<string, Node>();
        this.edgeMap = new Map<string, Edge>();
        this.lastX = 0;
        this.versionChangedFor = null;
        const graph: GraphEditor = this.graph.nativeElement;

        graph.setNodes([]);
        graph.setEdges([]);

        graph.completeRender();
        graph.zoomToBoundingBox(false);
    }

    /**
     * Update the existing graph to match the new Application data.
     *
     * @param application mico application
     */
    updateGraphFromApplicationData(application) {
        // keep local reference in case of resetGraph changes global variables.
        const nodeMap = this.nodeMap;
        const edgeMap = this.edgeMap;
        const graph: GraphEditor = this.graph.nativeElement;

        // mark all nodes (except root) as possible to delete
        const toDelete: Set<string> = new Set<string>();
        nodeMap.forEach(node => {
            if (node.id !== 'APPLICATION') {
                toDelete.add(node.id as string);
            }
        });

        if (!nodeMap.has('APPLICATION')) {
            // create new application root node if node does not exist
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

        // map services to graph nodes
        application.services.forEach((service) => {
            const serviceId = `${service.shortName}-${service.version}`;
            toDelete.delete(serviceId); // remove toDelete mark from node
            if (nodeMap.has(serviceId)) {
                // update existing node
                const node = nodeMap.get(serviceId);
                node.title = service.name != null ? service.name : service.shortName;
                node.version = service.version;
                node.shortName = service.shortName;
                node.name = service.name;
                node.description = service.description;
                node.service = service;
            } else {
                // create new node
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
                // super basic layout algorithm:
                this.lastX += 110;
                if (this.versionChangedFor != null) {
                    if (this.versionChangedFor.newVersion.shortName === service.shortName &&
                        this.versionChangedFor.newVersion.version === service.version) {
                        node.x = this.versionChangedFor.node.x;
                        node.y = this.versionChangedFor.node.y;
                        this.lastX -= 110;
                        this.versionChangedFor = null;
                    }
                }
                nodeMap.set(serviceId, node);
                graph.addNode(node, false);
                // add edge from root to service node
                const edge: Edge = {
                    source: 'APPLICATION',
                    target: serviceId,
                    type: 'includes',
                    markerEnd: {
                        template: 'arrow',
                        positionOnLine: 1,
                        lineOffset: 8,
                        scale: 1,
                        rotate: {
                            relativeAngle: 0,
                        },
                    },
                };
                edgeMap.set(`sAPPLICATION-t${serviceId}`, edge);
                graph.addEdge(edge, false);
            }
        });

        toDelete.forEach(nodeId => {
            // delete all nodes still marked as toDelete
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

        this.graph.nativeElement.zoomToBoundingBox(true);
    }
}
