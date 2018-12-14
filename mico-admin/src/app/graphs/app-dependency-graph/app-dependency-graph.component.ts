import { Component, OnInit, ViewChild, Input } from '@angular/core';
import GraphEditor from 'mico-grapheditor/src/mico-graph';
import { Edge } from 'mico-grapheditor/src/edge';
import { Node } from 'mico-grapheditor/src/node';

const STYLE_TEMPLATE = {
    id: 'style',
    innerHTML: `
        .node {fill: aqua;}
        .text {fill: black;}
        .text.title {width: 80;}
        .node.hovered {fill: red;}
        .node.selected {fill: green; content:attr(class)}
        .edge.highlight-outgoing {stroke: red;}
        .edge.highlight-incoming {stroke: green;}`
};

const NODE_TEMPLATE = {
    id: 'node',
    innerHTML: `<rect width="100" height="60" x="-50" y="-30"></rect>
        <text class="title text" data-content="title" data-click="title" x="-40" y="-10"></text>
        <text class="text" data-content="type" x="-40" y="10"></text>`
};

@Component({
    selector: 'mico-app-dependency-graph',
    templateUrl: './app-dependency-graph.component.html',
    styleUrls: ['./app-dependency-graph.component.css']
})
export class AppDependencyGraphComponent implements OnInit {

    @ViewChild('graph') graph;
    @Input() nodes: [any];
    @Input() edges: [any];

    // TODO Button ZoomToBounding Box

    constructor() { }

    ngOnInit() {
        if (this.graph != null) {
            this.graph.nativeElement.initialize();
            this.graph.nativeElement.updateTemplates([NODE_TEMPLATE], [STYLE_TEMPLATE]);
        }

        console.log(JSON.stringify(this.nodes));
        console.log(JSON.stringify(this.edges));

        this.updateGraph();
    }

    updateGraph() {
        if (this.graph == null) {
            return;
        }
        const graph = this.graph.nativeElement;

        //console.log(document.importNode(graph.children[1].content, true));

        // TODO aus @Input nodes definieren/erstellen
        const graphNodes = [];
        let x = 0;
        let y = 0;
        const xSize = 70;
        const ySize = 40;

        this.nodes.forEach(node => {

            graphNodes.push({
                id: node.id,
                x: x * xSize,
                y: y * ySize,
                title: node.title,
                type: node.type,
            });

            x += 1;
            if (x > 5) {
                x = 0;
                y += 1;
            }
        });


        graph.setNodes(graphNodes);
        console.log(graphNodes);

        const graphEdges = [];
        this.edges.forEach(edge => {
            graphEdges.push({
                source: edge.source,
                target: edge.target,
            });
        });

        graph.setEdges(graphEdges);
        console.log(graphEdges);

        /*
        graph.setNodes([
            {
                id: 1,
                x: 0,
                y: 0,
                title: 'hello world',
                type: 'rest',
            },
            {
                id: 2,
                x: 150,
                y: 100,
                title: 'HI2',
                type: 'gRPC',
            }
        ]);
        */
        graph.completeRender();

    }

}
