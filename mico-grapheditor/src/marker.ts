import { RotationVector } from "./rotation-vector";

interface RotationData {
    /** Absolute rotation via direction vector. */
    normal?: RotationVector;
    /** Relative angle in degree. */
    relativeAngle: number;
}

export interface Marker {
    /** the marker template id to use for this marker. */
    template: string;
    /** The relative position of the marker on the edge (between 0 and 1). */
    positionOnLine: number|string;
    /** A factor to scale the marker. */
    scale?: number;
    /** Rotation information for the marker. */
    rotate?: RotationData;
}
