export interface RotationVector {
    dx: number;
    dy: number;
}

/**
 * Normalize an existing vector to length 1.
 *
 * @param vector vector to normalize
 */
export function normalizeVector(vector: RotationVector): RotationVector {
    let x = vector.dx;
    let y = vector.dy;
    const length = Math.sqrt(x*x + y*y);
    return {
        dx: x/length,
        dy: y/length,
    };
}


/**
 * Calculate the Angle of a rotation vector in degree.
 *
 * @param vector vector to normalize
 */
export function calculateAngle(vector: RotationVector): number {
    if (vector.dx === 0 && vector.dy === 0) {
        return 0;
    }
    vector = normalizeVector(vector);
    const angle = Math.atan2(vector.dy, vector.dx)
    return angle * 180/Math.PI;
}
