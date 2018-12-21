import { Node } from "./node";
import { Edge, edgeId } from "./edge";
import { DEFAULT_NODE_TEMPLATE } from "./templates";

export class GraphObjectCache {

    private nodeTemplates: Map<string, string>;
    private nodes: Map<number|string, Node>;
    private edges: Map<number|string, Edge>;
    private edgesBySource: Map<number|string, Set<Edge>>;
    private edgesByTarget: Map<number|string, Set<Edge>>;

    constructor() {
        this.nodeTemplates = new Map();
        this.nodes = new Map();
        this.edges = new Map();
        this.edgesBySource = new Map();
        this.edgesByTarget = new Map();
    }

    updateNodeTemplateCache(templates: {id: string, innerHTML: string, [prop: string]: any}[]) {
        const templateMap = new Map();
        templates.forEach((template) => templateMap.set(template.id, template.innerHTML));
        this.nodeTemplates = templateMap;
    }

    updateNodeCache(nodes: Node[]) {
        const nodeMap = new Map();
        nodes.forEach((node) => nodeMap.set(node.id, node));
        this.nodes = nodeMap;
    }

    updateEdgeCache(edges: Edge[]) {
        const edgeMap = new Map();
        edges.forEach((edge) => edgeMap.set(edgeId(edge), edge));
        this.edges = edgeMap;
    }

    getNodeTemplateId(nodeType: string) {
        if (nodeType == null || !this.nodeTemplates.has(nodeType)) {
            return 'default';
        } else {
            return nodeType;
        }
    }

    getNodeTemplate(nodeType: string) {
        if (nodeType == null) {
            nodeType = 'default';
        }
        let template = this.nodeTemplates.get(nodeType);
        if (template == null) {
            template = this.nodeTemplates.get('default');
        }
        if (template == null) {
            template = DEFAULT_NODE_TEMPLATE;
        }
        return template;
    }

    getNode(id: number|string) {
        return this.nodes.get(id);
    }

    getEdge(id: number|string) {
        return this.edges.get(id);
    }

    getEdgesByTarget(targetId: number|string): Set<Edge> {
        const edges = this.edgesByTarget.get(targetId);
        if (edges == null) {
            return new Set();
        }
        return edges;
    }

    getEdgesBySource(targetId: number|string): Set<Edge> {
        const edges = this.edgesBySource.get(targetId);
        if (edges == null) {
            return new Set();
        }
        return edges;
    }
}