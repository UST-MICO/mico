import { RotationVector } from "./rotation-vector";

export interface Marker {
    template: string;
    positionOnLine: number|string;
    scale?: number;
    rotate?: {
        normal?: RotationVector;
        relativeAngle: number;
    }
}
