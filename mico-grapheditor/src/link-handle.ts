import { RotationVector, normalizeVector } from "./rotation-vector";

export interface LinkHandle {
    id: number;
    x: number;
    y: number;
    normal?: RotationVector
}

/**
 * Calculate a vector of length 1 facing away from 0,0 towards handle.x,handle.y.
 *
 * @param handle link handle to calculate normal for
 */
export function calculateNormal(handle: LinkHandle) {
    let x = handle.normal != null ? handle.normal.dx : handle.x;
    let y = handle.normal != null ? handle.normal.dy : handle.y;

    handle.normal = normalizeVector({
        dx: x,
        dy: y,
    });
}

/**
 * Generate link handles list for rectangle.
 *
 * @param x
 * @param y
 * @param width
 * @param height
 * @param linkHandles one of ['all', 'edges', 'corners', 'minimal'] 'minimal' is an alias for 'edges'
 */
export function handlesForRectangle(x: number, y: number, width: number, height: number, linkHandles: string): LinkHandle[] {
    const handles: LinkHandle[] = [];
    if (linkHandles === 'all' || linkHandles === 'corners') {
        handles.push({id: 0, x: x, y: y});
    }
    if (linkHandles === 'all' || linkHandles === 'edges' || linkHandles === 'minimal') {
        handles.push({id: 0, x: x+(width/2), y: y});
    }
    if (linkHandles === 'all' || linkHandles === 'corners') {
        handles.push({id: 0, x: x+width, y: y});
    }
    if (linkHandles === 'all' || linkHandles === 'edges' || linkHandles === 'minimal') {
        handles.push({id: 0, x: x+width, y: y+(height/2)});
    }
    if (linkHandles === 'all' || linkHandles === 'corners') {
        handles.push({id: 0, x: x, y: y+height});
    }
    if (linkHandles === 'all' || linkHandles === 'edges' || linkHandles === 'minimal') {
        handles.push({id: 0, x: x+(width/2), y: y+height});
    }
    if (linkHandles === 'all' || linkHandles === 'corners') {
        handles.push({id: 0, x: x+width, y: y+height});
    }
    if (linkHandles === 'all' || linkHandles === 'edges' || linkHandles === 'minimal') {
        handles.push({id: 0, x: x, y: y+(height/2)});
    }
    handles.forEach((element, index) => {element.id = index});
    handles.forEach(calculateNormal);
    return handles;
}

/**
 * Generate link handles list for circle.
 *
 * @param radius
 * @param linkHandles one of ['all', 'minimal']
 */
export function handlesForCircle(radius: number, linkHandles: string): LinkHandle[] {
    const handles: LinkHandle[] = [];
    handles.push({id: 0, x: 0, y: radius});
    if (linkHandles === 'all') {
        handles.push({
            id: 0,
            x: Math.sin((Math.PI/2)+(Math.PI/4))*radius,
            y: Math.cos((Math.PI/2)+(Math.PI/4))*radius,
        });
    }
    handles.push({id: 0, x: radius, y: 0});
    if (linkHandles === 'all') {
        handles.push({
            id: 0,
            x: Math.sin(Math.PI/4)*radius,
            y: Math.cos(Math.PI/4)*radius,
        });
    }
    handles.push({id: 0, x: 0, y: -radius});
    if (linkHandles === 'all') {
        handles.push({
            id: 0,
            x: Math.sin((3*Math.PI/2)+(Math.PI/4))*radius,
            y: Math.cos((3*Math.PI/2)+(Math.PI/4))*radius,
        });
    }
    handles.push({id: 0, x: -radius, y: 0});
    if (linkHandles === 'all') {
        handles.push({
            id: 0,
            x: Math.sin((2*Math.PI/2)+(Math.PI/4))*radius,
            y: Math.cos((2*Math.PI/2)+(Math.PI/4))*radius,
        });
    }
    handles.forEach((element, index) => {element.id = index});
    handles.forEach(calculateNormal);
    return handles;
}

