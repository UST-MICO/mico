import { LinkHandle } from "./link-handle";
import { Marker } from "./marker";

export interface Edge {
    id?: number|string,
    source: number|string,
    target: number|string,
    sourceHandle?: LinkHandle,
    targetHandle?: LinkHandle,
    type?: any,
    markers?: Marker[],
    [prop: string]: any,
}

export interface DraggedEdge extends Edge {
    id: string;
    createdFrom?: number|string;
    validTargets: Set<string>;
    currentTarget: {x: Number, y: number};
}

export function edgeId(edge) {
    if (edge.id != null) {
        return edge.id;
    }
    return `s${edge.source},t${edge.target}`;
}
