/**
 * Node interface.
 */
export interface Node {
    /** Unique identifier. */
    id: number|string,
    /** X coordinate of Node(center). */
    x: number,
    /** Y coordinate of Node(center). */
    y: number,
    /** Node type. Can be used for styling. */
    type?: any,
    [prop: string]: any,
}
