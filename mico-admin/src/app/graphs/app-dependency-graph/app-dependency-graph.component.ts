import { Component, OnInit, ViewChild, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Edge } from 'grapheditor-webcomponent/lib/edge';
import { Node } from 'grapheditor-webcomponent/lib/node';
import { ApiObject } from 'src/app/api/apiobject';

const STYLE_TEMPLATE = {
    id: 'style',
    innerHTML: `
        svg {position: absolute;}
        .ghost {opacity: 0.5;}
        .node {fill: white}
        .link-handle {display: none; fill: black; opacity: 0.1; transition:r 0.25s ease-out;}
        .edge-group .link-handle {display: initial}
        .link-handle:hover {opacity: 0.7; r: 5;}
        .text {fill: black; font-size: 6pt; text-overflow: ellipsis; word-break: break-word}
        .text.title {font-size: initial; word-break: break-all;}
        .node.hovered {fill: red;}
        .hovered .link-handle {display: initial;}
        .node.selected {fill: green; }
        .highlight-outgoing .edge {stroke: red;}
        .highlight-incoming .edge {stroke: green;}
        .highlight-outgoing .marker {fill: red;}
        .highlight-incoming .marker {fill: green;}`
};

const NODE_TEMPLATE = {
    id: 'default',
    innerHTML: `<rect width="100" height="60" x="-50" y="-30"></rect>
        <text class="title text" data-content="title" data-click="title" x="-40" y="-10"></text>
        <text class="text" data-content="version" x="-40" y="10"></text>`
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
    @Input() application: ApiObject;

    private nodeMap: Map<string, Node>;
    private nodes: Node[] = [];

    constructor() {}

    ngOnInit() {
        if (this.graph != null) {
            this.graph.nativeElement.initialize();
            this.graph.nativeElement.updateTemplates([NODE_TEMPLATE], [STYLE_TEMPLATE], [ARROW_TEMPLATE]);
            this.graph.onCreateDraggedEdge = (edge) => {
                edge.markers = [{template: 'arrow', positionOnLine: 1, scale: 0.5, rotate: {relativeAngle: 0}}, ];
                return edge;
            };
        }

        this.updateGraph();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.application != null) {
            const prev = changes.application.previousValue;
            const now = changes.application.currentValue;
            if (now == null) {
                return;
            }
            if (prev == null || prev.shortName !== now.shortName || prev.version != now.version) {
                const nodeMap = new Map<string, Node>();
                const nodes = [];
                if (now.services != null) {
                    now.services.forEach((service) => {
                        const node: Node = {
                            id: `${service.shortName}-${service.version}`,
                            x: 0,
                            y: 0,
                            title: service.name != null ? service.name : service.shortName,
                            version: service.version,
                            shortName: service.shortName,
                            name: service.name,
                            description: service.description,
                        };
                        nodeMap.set(node.id as string, node);
                        nodes.push(node);
                    });
                }
                this.nodeMap = nodeMap;
                this.nodes = nodes;
            }
            this.updateGraph();
        }
    }

    updateGraph() {
        if (this.graph == null) {
            return;
        }
        const graph = this.graph.nativeElement;

        graph.setNodes(this.nodes);

        const graphEdges: Edge[] = [];

        if (this.application != null) {
            [].forEach(edge => {
                graphEdges.push({
                    source: edge.source,
                    target: edge.target
                });
            });
        }

        graph.setEdges(graphEdges);

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
