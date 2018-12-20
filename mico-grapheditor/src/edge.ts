import { LinkHandle } from "./link-handle";

export interface Edge {
    id?: number|string,
    source: number|string,
    target: number|string,
    sourceHandle?: LinkHandle,
    targetHandle?: LinkHandle,
    type?: any,
    [prop: string]: any,
}

export function edgeId(edge) {
    if (edge.id != null) {
        return edge.id;
    }
    return `s${edge.source},t${edge.target}`;
}
