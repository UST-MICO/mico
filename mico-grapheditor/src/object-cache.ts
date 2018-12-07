import { Node } from "./node";
import { Edge, edgeId } from "./edge";

export class GraphObjectCache {
    private nodes: Map<number|string, Node>;
    private edges: Map<number|string, Edge>;
    private edgesBySource: Map<number|string, Set<Edge>>;
    private edgesByTarget: Map<number|string, Set<Edge>>;

    constructor() {
        this.nodes = new Map();
        this.edges = new Map();
        this.edgesBySource = new Map();
        this.edgesByTarget = new Map();
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