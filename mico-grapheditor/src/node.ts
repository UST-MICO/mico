export interface Node {
    id: number|string,
    x: number,
    y: number,
    type?: any,
    [prop: string]: any,
}
