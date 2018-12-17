export interface Edge {
    id?: number|string,
    source: number|string,
    target: number|string,
    type?: any,
    [prop: string]: any,
}

export function edgeId(edge) {
    if (edge.id != null) {
        return edge.id;
    }
    return `s${edge.source},t${edge.target}`;
}
