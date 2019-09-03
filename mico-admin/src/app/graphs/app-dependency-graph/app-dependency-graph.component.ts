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

import { Component, OnInit, ViewChild, Input, OnChanges, SimpleChanges, OnDestroy } from '@angular/core';
import GraphEditor from '@ustutt/grapheditor-webcomponent/lib/grapheditor';
import { Edge, DraggedEdge, edgeId, TextComponent } from '@ustutt/grapheditor-webcomponent/lib/edge';
import { Node } from '@ustutt/grapheditor-webcomponent/lib/node';
import { ApiObject } from 'src/app/api/apiobject';
import { ApiService } from 'src/app/api/api.service';
import { Subscription, Subject } from 'rxjs';
import { STYLE_TEMPLATE, APPLICATION_NODE_TEMPLATE, SERVICE_NODE_TEMPLATE, ARROW_TEMPLATE, ServiceNode, ApplicationNode, ServiceInterfaceNode, SERVICE_INTERFACE_NODE_TEMPLATE, KAFKA_TOPIC_NODE_TEMPLATE } from './app-dependency-graph-constants';
import { MatDialog } from '@angular/material';
import { ChangeServiceVersionComponent } from 'src/app/dialogs/change-service-version/change-service-version.component';
import { debounceTime, take, takeLast } from 'rxjs/operators';
import { safeUnsubscribe, safeUnsubscribeList } from 'src/app/util/utils';
import { YesNoDialogComponent } from 'src/app/dialogs/yes-no-dialog/yes-no-dialog.component';
import { GraphAddEnvironmentVariableComponent } from 'src/app/dialogs/graph-add-environment-variable/graph-add-environment-variable.component';
import { GraphAddKafkaTopicComponent } from 'src/app/dialogs/graph-add-kafka-topic/graph-add-kafka-topic.component';
import { Router } from '@angular/router';
import { ServicePickerComponent } from 'src/app/dialogs/service-picker/service-picker.component';


const ROOT_NODE_ID = 'APPLICATION';


@Component({
    selector: 'mico-app-dependency-graph',
    templateUrl: './app-dependency-graph.component.html',
    styleUrls: ['./app-dependency-graph.component.css']
})
export class AppDependencyGraphComponent implements OnInit, OnChanges, OnDestroy {
    @ViewChild('graph') graph;
    @Input() shortName: string;
    @Input() version: string;

    // subscriptions
    private appSubscription: Subscription;
    private appStatusSubscription: Subscription;
    private serviceSubscriptions: Map<string, Subscription[]> = new Map<string, Subscription[]>();

    // data cache
    private application: ApiObject;
    private applicationStatus: ApiObject;
    // map all included service shortNames to their graph ids
    private includedServicesMap: Map<string, string> = new Map();
    private serviceInterfaces: Map<string, ApiObject[]> = new Map();
    private deploymentInformations: Map<string, ApiObject> = new Map();

    // subject to batch all update requests when anything changes
    private updateSubject: Subject<boolean> = new Subject<boolean>();

    // graph data
    private firstRender: boolean = true;

    private lastX = 0;

    private serviceNodeMap: Map<string, ServiceNode>;
    private serviceInterfaceNodeMap: Map<string, ServiceInterfaceNode>;
    private kafkaTopicNodes: Set<string>;

    private versionChangedFor: { node: Node, newVersion: ApiObject };

    constructor(
        private api: ApiService,
        private dialog: MatDialog,
        private router: Router,
    ) { }

    ngOnInit() {
        this.updateSubject.pipe(debounceTime(300)).subscribe(this.updateGraph);
        if (this.graph == null) {
            console.warn('Graph not in dom!');
        }
        const graph: GraphEditor = this.graph.nativeElement;
        graph.setNodeClass = (className, node) => {
            if (className === node.type) { // set node class according to node type
                return true;
            }
            if (node.type === 'application') {
                // deployed status for application node
                if (className === 'undeployed' && (node.status == null || node.status.value === 'Unknown' || node.status.value === 'Undeployed')) {
                    return true;
                }
                if (className === 'deployed' && (node.status != null && node.status.value === 'Deployed')) {
                    return true;
                }
                if (className === 'pending' && (node.status != null && node.status.value === 'Pending')) {
                    return true;
                }
                if (className === 'error' && (node.status != null && node.status.value === 'Incomplete')) {
                    return true;
                }
            }
            return false;
        };
        graph.setEdgeClass = (className, edge) => {
            if (className === edge.type) { // set edge class according to edge type
                return true;
            }
            return false;
        };
        graph.calculateLinkHandlesForEdge = (edge, sourceHandles, source, targetHandles, target) => {
            if (edge.type === 'topic') {
                if (edge.role === 'INPUT') {
                    return {
                        sourceHandles: sourceHandles.filter(handle => handle.x > 0),
                        targetHandles: targetHandles,
                    };
                }
                if (edge.role === 'OUTPUT') {
                    return {
                        sourceHandles: sourceHandles,
                        targetHandles: targetHandles.filter(handle => handle.x < 0),
                    };
                }
            }
            return null;
        };
        graph.addEventListener('nodeclick', this.onNodeClick);
        graph.addEventListener('nodepositionchange', this.onNodeMove);
        graph.addEventListener('edgeadd', this.onEdgeAdd);
        graph.addEventListener('edgedrop', this.onEdgeDrop);
        graph.addEventListener('edgeremove', this.onEdgeRemove);
        graph.onCreateDraggedEdge = this.onCreateDraggedEdge;
        graph.onDraggedEdgeTargetChange = this.onDraggedEdgeTargetChange;
        graph.updateTemplates(
            [SERVICE_NODE_TEMPLATE, SERVICE_INTERFACE_NODE_TEMPLATE, KAFKA_TOPIC_NODE_TEMPLATE, APPLICATION_NODE_TEMPLATE],
            [STYLE_TEMPLATE],
            [ARROW_TEMPLATE]
        );
        this.resetGraph();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.shortName != null || changes.version != null) {
            this.resetGraph();

            safeUnsubscribe(this.appSubscription);
            safeUnsubscribe(this.appStatusSubscription);

            if (this.shortName != null && this.version != null) {
                this.appSubscription = this.api.getApplication(this.shortName, this.version).subscribe(application => {
                    this.application = application;

                    const includedServicesMap = new Map();
                    this.includedServicesMap = includedServicesMap;

                    application.services.forEach(service => {
                        const serviceId = `${service.shortName}-${service.version}`;
                        includedServicesMap.set(service.shortName, serviceId);
                        // handle service subscriptions
                        if (!this.serviceSubscriptions.has(serviceId)) {
                            const subscriptions = [];
                            subscriptions.push(this.api.getServiceInterfaces(service.shortName, service.version).subscribe((interfaces) => {
                                this.serviceInterfaces.set(serviceId, interfaces);
                                this.updateSubject.next(true);
                            }));
                            subscriptions.push(this.api.getServiceDeploymentInformation(this.shortName, this.version, service.shortName)
                                .subscribe((deploymentInformation) => {
                                    this.deploymentInformations.set(serviceId, deploymentInformation);
                                    this.updateSubject.next(false);
                                })
                            );
                            this.serviceSubscriptions.set(serviceId, subscriptions);
                        }
                    });
                    this.firstRender = true;
                    this.updateSubject.next(true);
                });
                this.appStatusSubscription = this.api.getApplicationDeploymentStatus(this.shortName, this.version).subscribe(status => {
                    this.applicationStatus = status;
                    this.updateSubject.next(false);
                });
            }
        }
    }

    ngOnDestroy() {
        safeUnsubscribe(this.appSubscription);
        this.serviceSubscriptions.forEach(safeUnsubscribeList);
    }

    /**
     * Handle node click events.
     */
    onNodeClick = (event: CustomEvent) => {
        if (event.detail.node.id === ROOT_NODE_ID || event.detail.node.type === 'service-interface') {
            event.preventDefault();  // prevent selecting application node and interface nodes
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
        if (event.detail.key === 'title') {
            this.router.navigate(['service-detail', event.detail.node.shortName, event.detail.node.version]);
        }
    }

    /**
     * Handle node move events.
     */
    onNodeMove = (event: CustomEvent) => {
        if (event.detail.node.id === ROOT_NODE_ID) {
            return;
        }
        if (event.detail.node.type === 'service') {  // user moved a service node
            const serviceNode: ServiceNode = event.detail.node;

            // update service interfaces to move with service node
            serviceNode.interfaces.forEach(interfaceId => {
                const node = this.serviceInterfaceNodeMap.get(interfaceId);
                if (node != null) {
                    node.x = serviceNode.x + node.dx;
                    node.y = serviceNode.y + node.dy;
                }
            });

            return;
        }
        if (event.detail.node.type === 'service-interface') {  // user moved a service interface node
            const node: ServiceInterfaceNode = event.detail.node;

            // update service interface deltas (the relative position to the service node)
            const serviceNode = this.serviceNodeMap.get(node.serviceId);
            if (serviceNode != null) {
                node.dx = node.x - serviceNode.x;
                node.dy = node.y - serviceNode.y;
            }

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
        this.versionChangedFor = { node: node, newVersion: newVersion };
        this.api.postApplicationServices(this.shortName, this.version, newVersion.shortName, newVersion.version).subscribe();
    }

    /**
     * Update markerEnd and type of newly created edges.
     */
    onCreateDraggedEdge = (edge: DraggedEdge) => {
        if (this.serviceInterfaceNodeMap.has(edge.source.toString())) {
            return;
        }
        const graph: GraphEditor = this.graph.nativeElement;

        if (edge.createdFrom != null) {
            // remove valid targets from edges that were created from an existing edge
            // forcing the user to drop the edge in the void
            edge.validTargets.clear();
            const sourceEdge = graph.getEdge(edge.createdFrom);
            if (sourceEdge != null) {
                // allow user dropping the edge on original target
                edge.validTargets.add(sourceEdge.target.toString());
            }
            return edge;
        }

        edge.markerEnd = { template: 'arrow', positionOnLine: 1, lineOffset: 4, scale: 0.5, rotate: { relativeAngle: 0 } };
        if (edge.source === ROOT_NODE_ID) {
            edge.type = 'includes';
            edge.markerEnd.lineOffset = 8;
            edge.markerEnd.scale = 1;
            edge.validTargets.clear();
        }
        if (this.serviceNodeMap.has(edge.source.toString())) {
            // if the source of the edge was a service node
            edge.type = 'interface-connection';
            edge.validTargets.clear();
            // compute valid targets for new edge
            this.serviceInterfaceNodeMap.forEach((node, key) => {
                if (node.serviceId !== edge.source) {
                    // remove all interfaces from the source service
                    edge.validTargets.add(key);
                }
            });
            if (graph.getNode(edge.source).service.kafkaEnabled) {
                // kafka enabled nodes can target kafka topics
                const depl = this.deploymentInformations.get(edge.source.toString());
                if (!depl.topics.some(t => t.role === 'OUTPUT')) {
                    // only nodes that don't have an output topic set
                    this.kafkaTopicNodes.forEach(e => edge.validTargets.add(e));
                }
            }
            const outgoingEdges: Set<Edge> = graph.getEdgesBySource(edge.source);
            outgoingEdges.forEach(e => e.target != null && edge.validTargets.delete(e.target.toString()));
        }
        const sourceNode: Node = graph.getNode(edge.source);
        if (sourceNode.type === 'kafka-topic') {
            edge.type = 'topic';
            edge.role = 'INPUT';
            delete edge.markerEnd;
            edge.markers = [{
                template: 'arrow',
                positionOnLine: 0.65,
                scale: 0.5,
                rotate: { relativeAngle: 0 }
            }];
            edge.texts = [{
                width: 40,
                positionOnLine: 0.65,
                offsetX: 5,
                offsetY: 3,
                value: 'INPUT',
            }];
            edge.validTargets.clear();
            this.serviceNodeMap.forEach((node, key) => {
                if (node.service.kafkaEnabled) {
                    const inputEdges: Set<Edge> = graph.getEdgesByTarget(node.id);
                    let isValidTarget = true;
                    inputEdges.forEach((edge) => {
                        if (edge.type === 'topic' && edge.role === 'INPUT') {
                            isValidTarget = false;
                        }
                    });
                    if (isValidTarget) {
                        edge.validTargets.add(node.id.toString());
                    }
                }
            });
        }
        return edge;
    }

    /**
     * Callback to change dragged edges when the current target changes.
     */
    onDraggedEdgeTargetChange = (edge: DraggedEdge, source: Node, target: Node) => {
        if (source.type === 'service') {
            if (target == null || target.type === 'service-interface') {
                edge.type = 'interface-connection';
                edge.markerEnd = { template: 'arrow', positionOnLine: 1, lineOffset: 4, scale: 0.5, rotate: { relativeAngle: 0 } };
                edge.markers = [];
                edge.texts = [];
                return;
            }
            if (target.type === 'kafka-topic') {
                edge.type = 'topic';
                edge.role = 'OUTPUT';
                delete edge.markerEnd;
                edge.markers = [{
                    template: 'arrow',
                    positionOnLine: 0.65,
                    scale: 0.5,
                    rotate: { relativeAngle: 0 }
                }];
                edge.texts = [{
                    width: 40,
                    positionOnLine: 0.65,
                    offsetX: 9,
                    value: 'OUTPUT',
                }];
                return;
            }
        }
    }

    /**
     * Handle edgeadd events from the grapheditor.
     */
    onEdgeAdd = (event: CustomEvent) => {
        const graph: GraphEditor = this.graph.nativeElement;
        const edge: Edge = event.detail.edge;
        if (edge.type === 'interface-connection') {
            // fetch all involved nodes for an interface connection edge
            const sourceNode = graph.getNode(edge.source) as ServiceNode;
            const targetNode = graph.getNode(edge.target) as ServiceInterfaceNode;
            const targetService = graph.getNode(targetNode.serviceId) as ServiceNode;
            this.createInterfaceConnection(edge, sourceNode, targetService, targetNode);
        }
        if (edge.type === 'topic') {
            let serviceId: string;
            let topicId: string;
            if (edge.role === 'INPUT') {
                serviceId = edge.target.toString();
                topicId = edge.source.toString();
            } else if (edge.role === 'OUTPUT') {
                serviceId = edge.source.toString();
                topicId = edge.target.toString();
            } else {
                // should never happen...
                console.warn("Only INPUT and OUTPUT topics should be shown in the grapheditor.");
                return;
            }
            const service = this.serviceNodeMap.get(serviceId);
            const topic = graph.getNode(topicId);
            // TODO add topic to deployment information
            const depl = this.deploymentInformations.get(serviceId);
            // deepcopy since depl is readonly
            const deplCopy = JSON.parse(JSON.stringify(depl));
            deplCopy.topics.push({
                role: edge.role,
                name: topic.data.topicName,
            });
            const putSub = this.api.putServiceDeploymentInformation(this.application.shortName, this.application.version, service.shortName, deplCopy).subscribe(() => {
                safeUnsubscribe(putSub);
            });
        }
    }

    /**
     * Handle edge drop events.
     */
    onEdgeDrop = (event: CustomEvent) => {
        if (event.detail.sourceNode.id === ROOT_NODE_ID) {
            const edge: Edge = event.detail.edge;
            if (edge.createdFrom != null && edge.createdFrom !== '') {
                // do not handle edges that were created from an existing edge here
                return;
            }
            const dialogRef = this.dialog.open(ServicePickerComponent, {
                data: {
                    filter: '',
                    choice: 'single',
                    existingDependencies: this.application.services,
                    serviceId: '',
                }
            });

            const subDependeesDialog = dialogRef.afterClosed().subscribe(result => {
                safeUnsubscribe(subDependeesDialog);

                if (result === '') {
                    return;
                }

                result.forEach(service => {
                    this.api.postApplicationServices(this.application.shortName,
                        this.application.version, service.shortName, service.version)
                        .subscribe();
                });
            });
        }
        if (event.detail.sourceNode.type === 'service') {
            const edge: Edge = event.detail.edge;
            if (edge.createdFrom != null && edge.createdFrom !== '') {
                // do not handle edges that were created from an existing edge here
                return;
            }
            // TODO create new topic edge
            const serviceNode: ServiceNode = event.detail.sourceNode;
            if (!serviceNode.service.kafkaEnabled) {
                // not kafka enabled nodes don't need topics
                return;
            }
            const depl = this.deploymentInformations.get(serviceNode.id.toString());
            const existingRoles: string[] = depl.topics.map(t => t.role);
            if (existingRoles.length >= 2) {
                // both roleas already used.
                return;
            }
            const dialogRef = this.dialog.open(GraphAddKafkaTopicComponent, {
                data: {
                    serviceShortName: serviceNode.shortName,
                    existingRoles: existingRoles,
                }
            });

            const subTopicDialog = dialogRef.afterClosed().subscribe(result => {
                safeUnsubscribe(subTopicDialog);

                if (result === '') {
                    return;
                }

                // deepcopy since depl is readonly
                const deplCopy = JSON.parse(JSON.stringify(depl));
                deplCopy.topics.push({
                    role: result.role,
                    name: result.kafkaTopicName,
                });
                const putSub = this.api.putServiceDeploymentInformation(this.application.shortName, this.application.version, serviceNode.service.shortName, deplCopy).subscribe(() => {
                    safeUnsubscribe(putSub);
                });
            });
        }
    }

    /**
     * Handle edgeremove events from the grapheditor.
     */
    onEdgeRemove = (event: CustomEvent) => {
        const graph: GraphEditor = this.graph.nativeElement;
        if (event.detail.eventSource !== 'USER_INTERACTION') {
            // for deletes that do not require further user interaction
            return;
        }
        const edge: Edge = event.detail.edge;
        if (edge.type === 'interface-connection') {
            // fetch all involved nodes for an interface connection edge
            const sourceNode = graph.getNode(edge.source) as ServiceNode;
            const targetNode = graph.getNode(edge.target) as ServiceInterfaceNode;
            const targetService = graph.getNode(targetNode.serviceId) as ServiceNode;
            this.removeInterfaceConnection(edge, sourceNode, targetService, targetNode);
        }
        if (edge.type === 'includes') {
            const serviceNode = graph.getNode(edge.target);
            const service = serviceNode.service;
            const dialogRef = this.dialog.open(YesNoDialogComponent, {
                data: {
                    object: service.shortName,
                    question: 'deleteDependency'
                }
            });

            const subServiceDependency = dialogRef.afterClosed().subscribe(shouldDelete => {
                safeUnsubscribe(subServiceDependency);
                if (shouldDelete) {
                    this.api.deleteApplicationServices(this.application.shortName, this.application.version, service.shortName)
                        .subscribe();
                } else {
                    graph.addEdge(edge);
                    graph.completeRender();
                }
            });
        }
        if (edge.type === 'topic') {
            let serviceId: string;
            if (edge.role === 'INPUT') {
                serviceId = edge.target.toString();
            } else if (edge.role === 'OUTPUT') {
                serviceId = edge.source.toString();
            } else {
                // should never happen...
                console.warn("Only INPUT and OUTPUT topics should be shown in the grapheditor.");
                return;
            }
            const service = this.serviceNodeMap.get(serviceId);
            const depl = this.deploymentInformations.get(serviceId);
            // deepcopy since depl is readonly
            const deplCopy = JSON.parse(JSON.stringify(depl));
            deplCopy.topics = depl.topics.filter(t => t.role !== edge.role);
            const putSub = this.api.putServiceDeploymentInformation(this.application.shortName, this.application.version, service.shortName, deplCopy).subscribe(() => {
                safeUnsubscribe(putSub);
            });
        }
    }

    /**
     * Show dialog to create a new interface connection.
     *
     * Removes edge if user cancels.
     *
     * @param edge the removed edge
     * @param sourceNode source service
     * @param targetService target service
     * @param targetInterface target interface
     */
    createInterfaceConnection(edge: Edge, sourceNode: ServiceNode, targetService: ServiceNode, targetInterface: ServiceInterfaceNode) {

        // show dialog
        const dialogRef = this.dialog.open(GraphAddEnvironmentVariableComponent, {
            data: {
                applicationShortName: this.shortName,
                applicationVersion: this.version,
                serviceShortName: sourceNode.shortName,
                targetServiceShortName: targetService.shortName,
                interfaceName: targetInterface.name,
            }
        });
        const subDialog = dialogRef.afterClosed().subscribe(result => {
            if (!result) {
                const graph: GraphEditor = this.graph.nativeElement;
                // prevent dialog popup on edge delete
                graph.removeEdge(edge, false);
                graph.completeRender();
                return;
            }

            this.api.getServiceDeploymentInformation(this.shortName, this.version, sourceNode.shortName)
                .pipe(take(2), takeLast(1))
                .subscribe(deplInf => {
                    const tempDeplInf = JSON.parse(JSON.stringify(deplInf));
                    tempDeplInf.interfaceConnections.push(result);

                    const subPutDeplInf = this.api.putServiceDeploymentInformation(this.shortName, this.version, sourceNode.shortName,
                        tempDeplInf)
                        .subscribe(() => {
                            safeUnsubscribe(subPutDeplInf);
                        });
                });
            safeUnsubscribe(subDialog);
        });
    }

    /**
     * Show dialog to remove interface connection.
     *
     * Restores edge if user cancels.
     *
     * @param edge the removed edge
     * @param sourceNode source service
     * @param targetService target service
     * @param targetInterface target interface
     */
    removeInterfaceConnection(edge: Edge, sourceNode: ServiceNode, targetService: ServiceNode, targetInterface: ServiceInterfaceNode) {
        const dialogRef = this.dialog.open(YesNoDialogComponent, {
            data: {
                object: {
                    sourceServiceName: sourceNode.title,
                    targetServiceName: targetService.title,
                    interfaceName: targetInterface.title,
                },
                question: 'deleteServiceToInterfaceConnection'
            }
        });

        const dialogSub = dialogRef.afterClosed().subscribe(result => {
            if (!result) {
                const graph: GraphEditor = this.graph.nativeElement;
                graph.addEdge(edge, false);
                graph.completeRender();
            } else {
                // delete all matching interface connections
                this.api.getServiceDeploymentInformation(this.shortName, this.version, sourceNode.shortName)
                .pipe(take(2), takeLast(1))
                .subscribe(deplInf => {
                    const tempDeplInf = JSON.parse(JSON.stringify(deplInf));
                    tempDeplInf.interfaceConnections = tempDeplInf.interfaceConnections.filter(conn => {
                        return (conn.micoServiceInterfaceName !== targetInterface.name) ||
                               (conn.micoServiceShortName !== targetService.shortName);
                    });

                    const subPutDeplInf = this.api.putServiceDeploymentInformation(this.shortName, this.version, sourceNode.shortName,
                        tempDeplInf)
                        .subscribe(() => {
                            safeUnsubscribe(subPutDeplInf);
                        });
                });
            }
            dialogSub.unsubscribe();
        });
    }

    /**
     * Reset the graph (all edges and nodes) and clears cache/layout data.
     */
    resetGraph() {
        this.serviceNodeMap = new Map<string, ServiceNode>();
        this.serviceInterfaceNodeMap = new Map<string, ServiceInterfaceNode>();
        this.kafkaTopicNodes = new Set<string>();
        this.firstRender = true;
        this.lastX = 0;
        this.versionChangedFor = null;
        const graph: GraphEditor = this.graph.nativeElement;

        graph.setNodes([]);
        graph.setEdges([]);

        graph.completeRender();
        graph.zoomToBoundingBox(false);
    }

    /**
     * Update the graph based on all currently available data in the cache.
     */
    updateGraph = (withZoom: boolean = false) => {
        const graph: GraphEditor = this.graph.nativeElement;
        // local cache that is not affected by subsequent updates from observables
        const application = this.application;
        const applicationStatus = this.applicationStatus;
        const serviceInterfaces = new Map<string, ApiObject[]>();
        const deployInfo = new Map<string, ApiObject>();

        application.services.forEach(service => {
            // fill local cache
            const serviceId = `${service.shortName}-${service.version}`;
            const interfaces = this.serviceInterfaces.get(serviceId);
            const deplInfo = this.deploymentInformations.get(serviceId);
            if (interfaces != null) {
                serviceInterfaces.set(serviceId, interfaces);
            }
            if (deplInfo != null) {
                deployInfo.set(serviceId, deplInfo);
            }
        });

        // update graph

        this.updateGraphFromApplicationData(application, applicationStatus);
        serviceInterfaces.forEach((interfaces, serviceId) => this.updateServiceInterfaceData(serviceId, interfaces));
        deployInfo.forEach((deplInfo, serviceId) => this.updateInterfaceConnectionEdgesFromDeploymentInformation(serviceId, deplInfo));
        deployInfo.forEach((deplInfo, serviceId) => this.updateTopicsFromDeploymentInformation(serviceId, deplInfo));

        // render changes
        graph.completeRender();
        if (withZoom || this.firstRender) {
            this.firstRender = false;
            graph.zoomToBoundingBox(false);
        }
    }

    /**
     * Update the existing graph to match the new Application data.
     *
     * @param application mico application
     * @param applicationStatus mico application deployment status
     */
    updateGraphFromApplicationData(application, applicationStatus) {
        // keep local reference in case of resetGraph changes global variables.
        const nodeMap = this.serviceNodeMap;
        const interfaceNodeMap = this.serviceInterfaceNodeMap;

        const graph: GraphEditor = this.graph.nativeElement;

        // mark all nodes as possible to delete
        const toDelete: Set<string> = new Set<string>();
        nodeMap.forEach(node => {
            toDelete.add(node.id as string);
        });

        if (graph.getNode(ROOT_NODE_ID) == null) {
            // create new application root node if node does not exist
            const node: ApplicationNode = {
                id: ROOT_NODE_ID,
                x: 0,
                y: 0,
                type: 'application',
                title: application.name != null ? application.name : application.shortName,
                version: application.version,
                shortName: application.shortName,
                name: application.name,
                description: application.description,
                status: applicationStatus,
                application: application,
            };
            graph.addNode(node, false);
        } else {
            const node: ApplicationNode = graph.getNode(ROOT_NODE_ID) as ApplicationNode;
            node.status = applicationStatus;
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
                const node: ServiceNode = {
                    id: serviceId,
                    x: this.lastX,
                    y: 90,
                    type: 'service',
                    title: service.name != null ? service.name : service.shortName,
                    version: service.version,
                    shortName: service.shortName,
                    name: service.name,
                    description: service.description,
                    service: service,
                    interfaces: new Set<string>(),
                };
                // super basic layout algorithm:
                this.lastX += 120;
                if (this.versionChangedFor != null) {
                    if (this.versionChangedFor.newVersion.shortName === service.shortName &&
                        this.versionChangedFor.newVersion.version === service.version) {
                        node.x = this.versionChangedFor.node.x;
                        node.y = this.versionChangedFor.node.y;
                        this.lastX -= 120;
                        this.versionChangedFor = null;
                    }
                }
                nodeMap.set(serviceId, node);
                graph.addNode(node, false);
                // add edge from root to service node
                const edge: Edge = {
                    source: ROOT_NODE_ID,
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
                graph.addEdge(edge, false);
            }
        });

        toDelete.forEach(nodeId => {
            // delete all nodes still marked as toDelete
            const node = nodeMap.get(nodeId);
            node.interfaces.forEach((interfaceId) => {
                const interfaceNode = interfaceNodeMap.get(interfaceId);
                graph.removeNode(interfaceNode);
                interfaceNodeMap.delete(interfaceId);
            });
            graph.removeNode(node);
            nodeMap.delete(nodeId);
        });
    }

    /**
     * Update the interfaces of a service
     *
     * @param serviceId the graph id of the service the interface corresponds to
     * @param interfaces the interface list of the service
     */
    private updateServiceInterfaceData(serviceId: string, interfaces: ApiObject[]) {
        const graph: GraphEditor = this.graph.nativeElement;
        const nodeMap = this.serviceInterfaceNodeMap;

        const serviceNode = this.serviceNodeMap.get(serviceId);
        if (serviceNode == null) {
            return;
        }
        const toDelete = new Set(serviceNode.interfaces);
        interfaces.forEach(serviceInterface => {
            const interfaceId = `${serviceId}-${serviceInterface.serviceInterfaceName}`;
            toDelete.delete(interfaceId); // remove toDelete mark from node
            if (nodeMap.has(interfaceId)) {
                // update interface
                const node = nodeMap.get(interfaceId);
                node.name = serviceInterface.serviceInterfaceName;
                node.description = serviceInterface.description;
                node.protocol = serviceInterface.protocol;
            } else {
                // create new interface
                const node: ServiceInterfaceNode = {
                    id: interfaceId,
                    x: serviceNode.x,
                    y: 150,
                    dx: 0,
                    dy: 60,
                    type: 'service-interface',
                    title: serviceInterface.serviceInterfaceName,
                    name: serviceInterface.serviceInterfaceName,
                    serviceId: serviceId,
                    description: serviceInterface.description,
                    protocol: serviceInterface.protocol,
                };
                serviceNode.interfaces.add(interfaceId);
                nodeMap.set(interfaceId, node);
                graph.addNode(node, false);
                const edge: Edge = {
                    source: serviceId,
                    target: interfaceId,
                    type: 'provides',
                };
                graph.addEdge(edge, false);
            }
        });

        toDelete.forEach((interfaceId) => {
            const node = nodeMap.get(interfaceId);
            serviceNode.interfaces.delete(interfaceId);
            graph.removeNode(node);
            nodeMap.delete(interfaceId);
        });
    }

    /**
     * Update the interface connection edges based on the deployment info of a service.
     *
     * @param serviceId the graph id of the service the interface corresponds to
     * @param deployInfo the deployment info of the service
     */
    updateInterfaceConnectionEdgesFromDeploymentInformation(serviceId: string, deployInfo: ApiObject) {
        const graph: GraphEditor = this.graph.nativeElement;
        const nodeMap = this.serviceNodeMap;
        const interfaceNodeMap = this.serviceInterfaceNodeMap;

        const existingEdges = graph.getEdgesBySource(serviceId);
        const toRemove = new Map<string, Edge>();

        existingEdges.forEach(edge => {
            if (edge.type === 'interface-connection') {
                toRemove.set(edgeId(edge), edge);
            }
        });

        deployInfo.interfaceConnections.forEach(connection => {
            const targetServiceId = this.includedServicesMap.get(connection.micoServiceShortName);
            const interfaceId = `${targetServiceId}-${connection.micoServiceInterfaceName}`;
            const edge: Edge = {
                source: serviceId,
                target: interfaceId,
                type: 'interface-connection',
                markerEnd: {
                    template: 'arrow',
                    positionOnLine: 1,
                    lineOffset: 4,
                    scale: 0.5,
                    rotate: { relativeAngle: 0 }
                },
            };
            if (graph.getEdge(edgeId(edge)) == null) {
                if (graph.getNode(edge.target) != null && graph.getNode(edge.source) != null) {
                    // only add edge if both nodes are part of graph
                    // this can happen because of observable timings...
                    graph.addEdge(edge, false);
                }
            }
            toRemove.delete(edgeId(edge));
        });

        toRemove.forEach((edge) => {
            graph.removeEdge(edge, false);
        });
    }



    /**
     * Update the topic connection edges based on the deployment info of a service.
     *
     * @param serviceId the graph id of the service the topic corresponds to
     * @param deployInfo the deployment info of the service
     */
    updateTopicsFromDeploymentInformation(serviceId: string, deployInfo: ApiObject) {
        const graph: GraphEditor = this.graph.nativeElement;

        const toRemove = new Map<string, Edge>();

        graph.getEdgesBySource(serviceId).forEach(edge => {
            if (edge.type === 'topic') {
                toRemove.set(edgeId(edge), edge);
            }
        });

        graph.getEdgesByTarget(serviceId).forEach(edge => {
            if (edge.type === 'topic') {
                toRemove.set(edgeId(edge), edge);
            }
        });

        const serviceNode: Node = graph.getNode(serviceId);

        deployInfo.topics.forEach(connection => {
            if (connection.role !== 'INPUT' && connection.role !== 'OUTPUT') {
                return;
            }
            const topicId = `TOPIC/${connection.name}`;
            let topicNode: Node = graph.getNode(topicId);
            if (topicNode == null) {
                topicNode = {
                    id: topicId,
                    x: serviceNode.x,
                    y: 170,
                    type: `kafka-topic`,
                    title: connection.name,
                    data: {
                        topicName: connection.name,
                    }
                };
                if (connection.role === 'INPUT') {
                    topicNode.x -= 30;
                }
                if (connection.role === 'OUTPUT') {
                    topicNode.x += 30;
                }
                graph.addNode(topicNode, false);
                this.kafkaTopicNodes.add(topicId);
            }

            const isInput = (connection.role === 'INPUT');
            const isOutput = (connection.role === 'OUTPUT');

            const textComponent: TextComponent = {
                width: 40,
                positionOnLine: 0.65,
                offsetX: 5,
                value: connection.role,
            };

            const newEdge: Edge = {
                source: null,
                target: null,
                type: 'topic',
                role: connection.role,
                markers: [{
                    template: 'arrow',
                    positionOnLine: 0.65,
                    scale: 0.5,
                    rotate: { relativeAngle: 0 }
                }],
                texts: [textComponent],
            };

            if (isInput) {
                newEdge.source = topicId;
                newEdge.target = serviceId;
                textComponent.offsetY = 3;
            }

            if (isOutput) {
                newEdge.source = serviceId;
                newEdge.target = topicId;
                textComponent.offsetX = 9;
            }

            if (graph.getEdge(edgeId(newEdge)) == null) {
                graph.addEdge(newEdge, false);
            }

            toRemove.delete(edgeId(newEdge));
        });

        toRemove.forEach((edge) => {
            graph.removeEdge(edge, false);
        });
    }

    autozoom() {
        if (this.graph == null) {
            return;
        }

        this.graph.nativeElement.zoomToBoundingBox(true);
    }
}
