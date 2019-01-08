import { map as Map } from "d3";
import { Node } from "./node";
import { Edge, edgeId } from "./edge";
import { DEFAULT_NODE_TEMPLATE } from "./templates";
import { LinkHandle, calculateNormal } from "./link-handle";

export class GraphObjectCache {

    private nodeTemplates: Map<string, string>;
    private nodeTemplateLinkHandles: Map<string, LinkHandle[]>;
    private markerTemplates: Map<string, string>;
    private nodes: Map<number|string, Node>;
    private edges: Map<number|string, Edge>;
    private edgesBySource: Map<number|string, Set<Edge>>;
    private edgesByTarget: Map<number|string, Set<Edge>>;

    constructor() {
        this.nodeTemplates = new Map();
        this.nodeTemplateLinkHandles = new Map();
        this.markerTemplates = new Map();
        this.nodes = new Map();
        this.edges = new Map();
        this.edgesBySource = new Map();
        this.edgesByTarget = new Map();
    }

    updateNodeTemplateCache(templates: {id: string, innerHTML: string, [prop: string]: any}[]) {
        const templateMap = new Map();
        templates.forEach((template) => templateMap.set(template.id, template.innerHTML));
        this.nodeTemplates = templateMap;
        this.nodeTemplateLinkHandles = new Map();
    }

    updateMarkerTemplateCache(templates: {id: string, innerHTML: string, [prop: string]: any}[]) {
        const templateMap = new Map();
        templates.forEach((template) => templateMap.set(template.id, template.innerHTML));
        this.markerTemplates = templateMap;
    }

    updateNodeCache(nodes: Node[]) {
        const nodeMap = new Map();
        nodes.forEach((node) => nodeMap.set(node.id, node));
        this.nodes = nodeMap;
    }

    updateEdgeCache(edges: Edge[]) {
        const edgeMap = new Map();
        const bySourceMap = new Map();
        const byTargetMap = new Map();
        edges.forEach((edge) => {
            edgeMap.set(edgeId(edge), edge);
            let bySource: Set<Edge> = bySourceMap.get(edge.source);
            if (bySource == null) {
                bySource = new Set();
                bySourceMap.set(edge.source, bySource)
            }
            bySource.add(edge);
            let byTarget: Set<Edge> = byTargetMap.get(edge.target);
            if (byTarget == null) {
                byTarget = new Set();
                byTargetMap.set(edge.target, byTarget)
            }
            bySource.add(edge);
        });
        this.edges = edgeMap;
        this.edgesBySource = bySourceMap;
        this.edgesByTarget = byTargetMap;
    }

    getMarkerTemplate(markerType: string) {
        if (markerType == null) {
            console.log('Marker type was null!');
            return;
        }
        return this.markerTemplates.get(markerType);
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

    setNodeTemplateLinkHandles(nodeType: string, linkHandles: LinkHandle[]) {
        nodeType = this.getNodeTemplateId(nodeType);
        this.nodeTemplateLinkHandles.set(nodeType, linkHandles);
    }

    getNodeTemplateLinkHandles(nodeType: string): LinkHandle[] {
        nodeType = this.getNodeTemplateId(nodeType);
        return this.nodeTemplateLinkHandles.get(nodeType);
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

    getEdgeLinkHandles(edge: Edge) {
        const source = this.getNode(edge.source);
        const target = edge.target != null ? this.getNode(edge.target) : edge.currentTarget;
        const sourceHandles = edge.sourceHandle != null ? [edge.sourceHandle] : this.getNodeTemplateLinkHandles(source.type);
        let targetHandles;
        if (edge.targetHandle != null) {
            targetHandles = [edge.targetHandle]
        } else if (edge.target != null) {
            targetHandles = this.getNodeTemplateLinkHandles(target.type);
        } else {
            // target only null for dragged edges
            targetHandles = [{id: 0, x: 0, y: 0}];
        }
        const result = this.calculateNearestHandles(sourceHandles, source, targetHandles, target);
        return {
            sourceHandle: result.sourceHandle,
            sourceCoordinates: {x: (source.x + result.sourceHandle.x), y: (source.y + result.sourceHandle.y)},
            targetHandle: result.targetHandle,
            targetCoordinates: {x: (target.x + result.targetHandle.x), y: (target.y + result.targetHandle.y)},
        };
    }

    private calculateNearestHandles(sourceHandles: LinkHandle[], source: Node, targetHandles: LinkHandle[], target: {x: number, y: number}) {
        let currentSourceHandle: LinkHandle = {id: 0, x: 0, y: 0, normal: {dx: 1, dy: 1}};
        if (sourceHandles != null && sourceHandles.length > 0) {
            currentSourceHandle = sourceHandles[0];
        } else {
            calculateNormal(currentSourceHandle);
        }
        let currentTargetHandle: LinkHandle = {id: 0, x: 0, y: 0, normal: {dx: 1, dy: 1}};
        if (targetHandles != null && targetHandles.length > 0) {
            currentTargetHandle = targetHandles[0];
        } else {
            calculateNormal(currentTargetHandle);
        }
        let currentDist = Math.pow((source.x + currentSourceHandle.x) - target.x, 2) + Math.pow((source.y + currentSourceHandle.y) - target.y, 2);
        targetHandles.forEach((targetHandle) => {
            for (let i = 0; i < sourceHandles.length; i++) {
                const handle = sourceHandles[i];
                const dist = Math.pow((source.x + handle.x) - (target.x + targetHandle.x), 2) + Math.pow((source.y + handle.y) - (target.y + targetHandle.y), 2);
                if (dist <= currentDist) {
                    currentSourceHandle = handle;
                    currentTargetHandle = targetHandle;
                    currentDist = dist;
                }
            }
        });
        return {
            sourceHandle: currentSourceHandle,
            targetHandle: currentTargetHandle,
        };
    }
}
