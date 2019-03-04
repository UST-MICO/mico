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
import { STYLE_TEMPLATE, SERVICE_NODE_TEMPLATE, ARROW_TEMPLATE } from './graph-constants';
import { MatDialog } from '@angular/material';
import { ChangeServiceVersionComponent } from 'src/app/dialogs/change-service-version/change-service-version.component';
import { debounceTime } from 'rxjs/operators';


@Component({
    selector: 'mico-service-dependency-graph',
    templateUrl: './service-dependency-graph.component.html',
    styleUrls: ['./service-dependency-graph.component.css']
})
export class ServiceDependencyGraphComponent implements OnInit, OnChanges {
    @ViewChild('graph') graph;
    @Input() shortName: string;
    @Input() version: string;

    rootId: string;

    serviceSubscription: Subscription;
    graphSubscription: Subscription;

    private lastX = 0;

    private nodeMap: Map<string, Node>;
    private edgeMap: Map<string, Edge>;

    private versionChangedFor: { node: Node, newVersion: ApiObject};

    constructor(private api: ApiService, private dialog: MatDialog) {}

    ngOnInit() {
        if (this.graph == null) {
            console.warn('Graph not in dom!');
        }
        const graph: GraphEditor = this.graph.nativeElement;
        graph.setNodeClass = (className, node) => {
            if (className === node.type) { // set node class according to node type
                return true;
            }
            // set dependency level based on distance from root
            if (className === 'root' && node.dependencyLevel === 0) {
                return true;
            }
            if (className === 'direct-dependency' && node.dependencyLevel === 1) {
                return true;
            }
            if (className === 'dependency' && node.dependencyLevel >= 1) {
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
        graph.addEventListener('nodemove', (event: CustomEvent) => {
            event.detail.node.wasMovedByUser = true;
        });
        graph.onCreateDraggedEdge = this.onCreateDraggedEdge;
        graph.updateTemplates([SERVICE_NODE_TEMPLATE], [STYLE_TEMPLATE], [ARROW_TEMPLATE]);
        this.resetGraph();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.shortName != null || changes.version != null) {
            this.rootId = `${this.shortName}-${this.version}`;
            this.resetGraph();
            if (this.graphSubscription != null) {
                this.graphSubscription.unsubscribe();
            }
            if (this.serviceSubscription != null) {
                this.serviceSubscription.unsubscribe();
            }
            if (this.shortName != null && this.version != null) {
                // listen for dependency graph updates
                this.graphSubscription = this.api.getServiceDependencyGraph(this.shortName, this.version).pipe(
                    debounceTime(300),
                ).subscribe(dependencyGraph => {
                    this.updateGraph(dependencyGraph);
                });
                // listen for direct updates of this service (e.g. to get edits in description or name)
                this.serviceSubscription = this.api.getService(this.shortName, this.version).subscribe(service => {
                    const serviceId = `${service.shortName}-${service.version}`;
                    const node = this.nodeMap.get(serviceId);
                    if (node != null && this.versionChangedFor == null) {
                        this.updateNode(node, service);
                        const graph: GraphEditor = this.graph.nativeElement;
                        graph.completeRender();
                    }
                });
            }
        }
    }

    /**
     * Handle node click events.
     */
    onNodeClick = (event: CustomEvent) => {
        if (event.detail.node.id === this.rootId) {
            event.preventDefault();  // prevent selecting/clicking root node
            return;
        }
        if (event.detail.key === 'version') {  // user clicked on service version
            event.preventDefault();

            if (event.detail.node.dependencyLevel !== 1) {
                // only version of direct dependencies can be changed
                return;
            }

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
        this.api.deleteServiceDependee(this.shortName, this.version, node.service.shortName, node.service.version).subscribe((success) => {
            if (!success) {
                return;
            }
            this.api.postServiceDependee(this.shortName, this.version, newVersion).subscribe();
        });
    }

    /**
     * Update markerEnd and type of newly created edges.
     */
    onCreateDraggedEdge = (edge: DraggedEdge) => {
        edge.markerEnd = {template: 'arrow', positionOnLine: 1, lineOffset: 4, scale: 0.5, rotate: {relativeAngle: 0}};
        edge.validTargets.clear();
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
     * Update the existing graph to match the new service Dependency network data.
     *
     * @param dependencyGraph service dependency graph
     */
    updateGraph(dependencyGraph) {
        // keep local reference in case of resetGraph changes global variables.
        const nodeMap = this.nodeMap;
        const edgeMap = this.edgeMap;
        const graph: GraphEditor = this.graph.nativeElement;

        // mark all nodes as possible to delete
        const toDelete: Set<string> = new Set<string>(nodeMap.keys());

        // map services to graph nodes
        dependencyGraph.micoServices.forEach(service => {
            const serviceId = `${service.shortName}-${service.version}`;
            console.log(serviceId, nodeMap.has(serviceId))
            toDelete.delete(serviceId); // remove toDelete mark from node
            if (nodeMap.has(serviceId)) {
                // update existing node
                const node = nodeMap.get(serviceId);
                this.updateNode(node, service);
            } else {
                // create new node
                const node: Node = {
                    id: serviceId,
                    x: serviceId === this.rootId ? 0 : this.lastX,
                    y: 0,
                    type: 'service',
                    title: service.name != null ? service.name : service.shortName,
                    version: service.version,
                    shortName: service.shortName,
                    name: service.name,
                    description: service.description,
                    outgoingEdges: new Set<string>(),
                    dependencyLevel: 0, // distanceFromRoot
                    wasMovedByUser: false,
                    service: service,
                };
                // super basic layout algorithm:
                if (serviceId !== this.rootId) {
                    this.lastX += 110;
                }
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
            }
        });

        // mark all edges as possible to delete
        const edgesToDelete: Set<string> = new Set<string>(edgeMap.keys());

        // map edges to graph edges
        dependencyGraph.micoServiceDependencyGraphEdgeList.forEach(edge => {
            const edgeId = `s${edge.sourceShortName}-${edge.sourceVersion}t${edge.targetShortName}-${edge.targetVersion}`;
            if (!edgeMap.has(edgeId)) {
                const newEdge: Edge = {
                    source: `${edge.sourceShortName}-${edge.sourceVersion}`,
                    target: `${edge.targetShortName}-${edge.targetVersion}`,
                    markerEnd: {template: 'arrow', positionOnLine: 1, lineOffset: 4, scale: 0.5, rotate: {relativeAngle: 0}},
                };
                const sourceNode = nodeMap.get(newEdge.source as string);
                if (sourceNode != null) {
                    sourceNode.outgoingEdges.add(edgeId);
                }
                edgeMap.set(edgeId, newEdge);
                graph.addEdge(newEdge, false);
            }
        });

        toDelete.forEach(nodeId => {
            // delete all nodes still marked as toDelete
            const node = nodeMap.get(nodeId);
            graph.removeNode(node);
            nodeMap.delete(nodeId);
        });

        edgesToDelete.forEach(edgeId => {
            const edge = edgeMap.get(edgeId);
            const sourceNode = nodeMap.get(edge.source as string);
            if (sourceNode != null) {
                sourceNode.outgoingEdges.delete(edgeId);
            }
            edgeMap.delete(edgeId);
        });

        this.updateDependencyLevels();

        graph.completeRender();
        graph.zoomToBoundingBox(false);
    }

    private updateNode(node: Node, service: any) {
        node.title = service.name != null ? service.name : service.shortName;
        node.version = service.version;
        node.shortName = service.shortName;
        node.name = service.name;
        node.description = service.description;
        node.service = service;
    }

    /**
     * (Re-)Calculate distance from root for all nodes.
     *
     * Also updates y coordinate for nodes never moved by the user.
     */
    updateDependencyLevels() {
        this.nodeMap.forEach((node) => {
            node.dependencyLevel = Infinity; // reset all distances
        });
        const root = this.nodeMap.get(this.rootId);
        root.dependencyLevel = 0;
        const fringe = new Set<string>(); // all nodes that need to be expanded
        fringe.add(root.id as string);
        while (fringe.size > 0) {
            // pop first node from fringe
            const nodeId = fringe.values().next().value;
            fringe.delete(nodeId);
            const node = this.nodeMap.get(nodeId);
            if (node.outgoingEdges != null) {
                // update distance for all targets
                node.outgoingEdges.forEach(edgeId => {
                    const edge = this.edgeMap.get(edgeId);
                    const targetNode = this.nodeMap.get(edge.target as string);
                    // if distance should change (new distance would be smaller)
                    if (targetNode != null && targetNode.dependencyLevel > (node.dependencyLevel + 1)) {
                        targetNode.dependencyLevel = node.dependencyLevel + 1;
                        if (!targetNode.wasMovedByUser) {
                            // update node.y only if it was not moved by the user
                            targetNode.y = targetNode.dependencyLevel * 90;
                        }
                        // only add node to fringe if the distance has changed
                        // infinite loops will not occur because the distance
                        // is always growing in a loop
                        fringe.add(targetNode.id as string);
                    }
                });
            }
        }
    }

    autozoom() {
        if (this.graph == null) {
            return;
        }

        this.graph.nativeElement.zoomToBoundingBox(true);
    }
}
