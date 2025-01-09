import { compareStringsAscii } from '@/types/utils/common';
import { type CSSProperties } from 'react';

export type Node = {
    id: string;
    position: Position;
};

export type Edge = {
    id: string;
    from: string;
    to: string;
};

/** Maps edges by their from- and to- nodes. */
export class EdgeMap<TEdge extends Edge = Edge> {
    private readonly idMap = new Map<string, TEdge>();
    private readonly fromMap = new Map<string, TEdge[]>();
    private readonly toMap = new Map<string, TEdge[]>();

    constructor(edges: TEdge[]) {
        edges.forEach(edge => {
            this.idMap.set(edge.id, edge);

            const fromId = edge.from;
            const fromEdges = this.fromMap.get(fromId) ?? [];
            fromEdges.push(edge);
            this.fromMap.set(fromId, fromEdges);

            const toId = edge.to;
            const toEdges = this.toMap.get(toId) ?? [];
            toEdges.push(edge);
            this.toMap.set(toId, toEdges);
        });
    }

    getEdgesForNode(nodeId: string): { from: TEdge[], to: TEdge[] } {
        return {
            from: this.fromMap.get(nodeId) ?? [],
            to: this.toMap.get(nodeId) ?? [],
        };
    }

    /**
     * All edges in the same output bundle have the same from/to nodes (but they might be switched).
     * The groups are sorted in the order how they should be displayed.
     */
    static bundleEdges<TEdge extends Edge = Edge>(edges: TEdge[]): TEdge[][] {
        const bundles = new Map<string, TEdge[]>();
        edges.forEach(edge => {
            const key = edge.from < edge.to ? `${edge.from}#${edge.to}` : `${edge.to}#${edge.from}`;
            const bundle = bundles.get(key) ?? [];
            bundle.push(edge);
            bundles.set(key, bundle);
        });

        return [ ...bundles.values() ].map(bundle => bundle.sort((a, b) => compareStringsAscii(a.id, b.id)));
    }

    values(): MapIterator<TEdge> {
        return this.idMap.values();
    }
}

export function getEdgeDegree({ from, to }: Edge, index: number, total: number): number {
    const base = index - (total - 1) / 2;
    const sign = from < to ? 1 : -1;

    return sign * base;
}

export type DragState = {
    /** The whole canvas is being dragged. */
    type: 'canvas';
    draggedPoint: Position;
} | {
    /** Just the node is being dragged. */
    type: 'node';
    nodeId: string;
    /** The difference between node position and mouse position. It should be kept constant during whole dragging process. */
    mouseDelta: Position;
};

export type SelectState = {
    initial: Position;
    current: Position;
};

/** Internal position of the nodes. In some relative units. */
export type Position = {
    x: number;
    y: number;
};

/** Distance from the top-left corner of the canvas. In pixels. */
type Offset = {
    left: number;
    top: number;
};

export type Coordinates = {
    /** Where the position { x: 0; y: 0 } is on the canvas. */
    origin: Offset;
    /** Distance in pixels = scale * distance in relative units. */
    scale: number;
}

export function offsetToPosition(offset: Offset, coordinates: Coordinates): Position {
    return {
        x: (offset.left - coordinates.origin.left) / coordinates.scale,
        y: (offset.top - coordinates.origin.top) / coordinates.scale,
    };
}

function positionToOffset(position: Position, coordinates: Coordinates): Offset {
    return {
        left: coordinates.origin.left + coordinates.scale * position.x,
        top: coordinates.origin.top + coordinates.scale * position.y,
    };
}

export function getMouseOffset(event: { clientX: number, clientY: number }, canvas: HTMLElement): Offset {
    const rect = canvas.getBoundingClientRect();
    return {
        left: event.clientX - rect.left,
        top: event.clientY - rect.top,
    };
}

export function getMousePosition(event: { clientX: number, clientY: number }, canvas: HTMLElement, coordinates: Coordinates): Position {
    return offsetToPosition(getMouseOffset(event, canvas), coordinates);
}

export function computeCoordinates(nodes: Node[], width: number, height: number): Coordinates {
    const minX = Math.min(...nodes.map(node => node.position.x));
    const maxX = Math.max(...nodes.map(node => node.position.x));
    const minY = Math.min(...nodes.map(node => node.position.y));
    const maxY = Math.max(...nodes.map(node => node.position.y));

    const scale = Math.min(width / (100 + maxX - minX), height / (100 + maxY - minY));

    const centroid = {
        x: (minX + maxX) / 2,
        y: (minY + maxY) / 2,
    };

    const origin = {
        left: (width / 2) - scale * centroid.x,
        top: (height / 2) - scale * centroid.y,
    };

    return { origin, scale };
}

export function isPointInBox(point: Position, boxStart: Position, boxEnd: Position): boolean {
    return Math.min(boxStart.x, boxEnd.x) < point.x
        && Math.max(boxStart.x, boxEnd.x) > point.x
        && Math.min(boxStart.y, boxEnd.y) < point.y
        && Math.max(boxStart.y, boxEnd.y) > point.y;
}

export function isEdgeInBox(from: Position, to: Position, boxStart: Position, boxEnd: Position): boolean {
    // Let's approximate the edge by its middle point. It ain't much but it's honest work.
    const middle = {
        x: (from.x + to.x) / 2,
        y: (from.y + to.y) / 2,
    };
    return isPointInBox(middle, boxStart, boxEnd);
}

const THROTTLE_DURATION_MS = 20;
export function throttle<T extends(...args: Parameters<T>) => void>(callback: T): T {
    let timeout: NodeJS.Timeout | undefined = undefined;

    return ((...args: Parameters<T>) => {
        if (timeout)
            return;

        callback(...args);
        timeout = setTimeout(() => {
            timeout = undefined;
        }, THROTTLE_DURATION_MS);
    }) as T;
}

export function computeNodeStyle(node: Node, coordinates: Coordinates): CSSProperties {
    const offset = positionToOffset(node.position, coordinates);

    return {
        left: `${offset.left}px`,
        top: `${offset.top}px`,
    };
}

/** How far from the node center is the edge start/end. In pixels. */
const EDGE_OFFSET = 20;
/** The minimal distance between middle points of edges between the two same nodes. In pixels. */
const EDGE_DELTA = 60;
/** The tangent of the angle between the two arrow sides. */
const EDGE_ARROW_TAN = Math.tan(40 / 2 * Math.PI / 180);
/** The lengths of the two arrow sides. In pixels. */
const EDGE_ARROW_LENGTH = 16;

export function computeEdgePath(from: Node, to: Node, degree: number, coordinates: Coordinates): string {
    const start = positionToOffset(from.position, coordinates);
    const end = positionToOffset(to.position, coordinates);

    return degree === 0
        ? computeEdgeStraightPath(start, end)
        : computeEdgeCurvedPath(start, end, degree * EDGE_DELTA);
}

/**
 * Computes a curved SVG path from `A` to `B` that has a `delta` distance (in the middle) from a linear path between these two points. If `delta` is negative, the curve will be on the other side.
 * Uses quadratic Bezier curve.
 */
function computeEdgeCurvedPath(A: Offset, B: Offset, delta: number): string {
    // Let's transform the coordinates to an xy plane (not to be confused with the relative Position units). We will use lowercased letters for the xy plane and capitals for the left-top coordinates.
    // The start and end points (A, B) are at the points (-x_0, y_0) and (x_0, y_0) respectively.
    const leftDiff = B.left - A.left;
    const topDiff = B.top - A.top;
    const diagonal = Math.sqrt(leftDiff**2 + topDiff**2);

    // This should be obvious.
    const x_0 = diagonal / 2;
    const y_0 = delta;

    // Any vector (x, y) can be transformed to the left-top coordinates by the following formulas:
    // L = X_L * x + Y_L * y
    // T = X_T * x + Y_T * y
    // In order to find the coefficients, we first transform the vector AB. This gives us:
    // X_L = (B.L - A.L) / (2 * x_0)
    // X_T = (B.T - A.T) / (2 * x_0)

    // Now, we want to transform the vector AO, where O is the origin of our new coordinates (0, 0). We need any linearly independent vector (with AB) and this one should be the easiest.
    // Through some geometry, we can find that:
    // O.L = (A.L + B.L) / 2 + (B.T - A.T) * y_0 / (2 * x_0)
    // O.T = (A.T + B.T) / 2 - (B.L - A.L) * y_0 / (2 * x_0)
    // AO.L = O.L - A.L = (B.L - A.L) / 2 + (B.T - A.T) * y_0 / (2 * x_0)
    // AO.T = O.T - A.T = (B.T - A.T) / 2 - (B.L - A.L) * y_0 / (2 * x_0)
    // Which gives us:
    // Y_L = (A.T - B.T) / (2 * x_0) = -X_T
    // Y_T = (B.L - A.L) / (2 * x_0) = X_L

    // Let's substitute K = X_L and Q = X_T for simplicity.
    const K = leftDiff / diagonal;
    const Q = topDiff / diagonal;

    // So, we can use the following formulas:
    // L = O_L + X_L * x + Y_L * y = O_L + K * x - Q * y
    // T = O_T + X_T * x + Y_T * y = O_T + Q * x + K * y

    // Note that this holds only for vectors, not for points. In order to transform a point, we have to create a vector from some well-known point (e.g., the origin) to the point, transform the vector, and then add the result to the well-known point in the left-top coordinates.
    const O_L = (A.left + B.left) / 2 + y_0 * Q;
    const O_T = (A.top + B.top) / 2 - y_0 * K;

    // The arc is a quadratic Bezier curve with the control point at (0, -y_0). Therefore, the curve is a parabola with the equation y = a * x^2, where a = y_0 / x_0^2.
    // However, we don't want the the arc to go all the way from A to B. We want it to start the EDGE_OFFSET distance after A (and end before B). Let's call these points C = (-x, y) and D = (x, y).
    // There's guaranteed to be another qudratic Bezier curve from C to D that is equivalent to the original one from C to D. In order to find it, we need to find the points C and D first.
    // This is tricky, since the condition we are working with is |DB| = |CD| = EDGE_OFFSET, but that leads to a quartic equation.
    // So, as a simplification, we will choose a different condition. Let's approximate the curve by its tangent from the point B, the go the EDGE_OFFSET along it from B, and use it as the x coordinate.

    // First, we use the derivative, y' = 2 * a * x, to construct the approximate tangent f = a * x_0 * (2 * x - x_0).
    // We can find the point D by solving the equation (x_0 - x)^2 + (y_0 - f^2)^2 = o^2, where o is the EDGE_OFFSET.
    // The solution is x = x_0 - o / sqrt(1 + y_0^2 / x_0^2).
    const x = x_0 - EDGE_OFFSET / Math.sqrt(1 + y_0**2 / x_0**2);
    const y = y_0 * x**2 / x_0**2;

    // Now we have the points C and D. Let's transform them to the left-top coordinates.
    const Kx = K * x;
    const Qx = Q * x;
    const Ky = K * y;
    const Qy = Q * y;

    const C_L = O_L - Kx - Qy;
    const C_T = O_T - Qx + Ky;

    const D_L = O_L + Kx - Qy;
    const D_T = O_T + Qx + Ky;

    // Finally, the control point P on the y axis and has the same y-distance from the origin as the points C and D. So:
    const P_L = O_L + Qy;
    const P_T = O_T - Ky;

    // This works quite nicely, however, the distance from the curve to the node depends on the distance of the nodes. We can fix this by solving the above mentioned quartic equation, or by some numerical method.
    // For now, this is fine.
    const arcPath = `M ${C_L} ${C_T} Q ${P_L} ${P_T} ${D_L} ${D_T}`;

    // In order to find the arrowheads, we create two linear functions that cross the curve at the point (x, y).
    // The first will have smaller tangent, the second will have greater tangent.

    // The parabola derivative at x is:
    const tan_0 = 2 * x * y_0 / x_0**2;

    // The derivative of the first function is just a sum of tangents:
    const tan_1 = (tan_0 + EDGE_ARROW_TAN) / (1 - tan_0 * EDGE_ARROW_TAN);

    // Now we compute the end of the first arrowhead, (x_1, y_1), which is EDGE_ARROW_LENGTH distance from (x, y) along the tangent. Then we transform it to the left-top coordinates.
    const x_1 = x - EDGE_ARROW_LENGTH / Math.sqrt(1 + tan_1**2);
    const y_1 = y + tan_1 * (x_1 - x);

    const S1_L = O_L + K * x_1 - Q * y_1;
    const S1_T = O_T + Q * x_1 + K * y_1;

    // The second arrowhead is basically the same story.
    const tan_2 = (tan_0 - EDGE_ARROW_TAN) / (1 + tan_0 * EDGE_ARROW_TAN);

    const x_2 = x - EDGE_ARROW_LENGTH / Math.sqrt(1 + tan_2**2);
    const y_2 = y + tan_2 * (x_2 - x);

    const S2_L = O_L + K * x_2 - Q * y_2;
    const S2_T = O_T + Q * x_2 + K * y_2;

    // Let's draw a cute triangle for better artistic impression.
    const arrowPath = `L ${S1_L} ${S1_T} L ${S2_L} ${S2_T} L ${D_L} ${D_T}`;

    return arcPath + ' ' + arrowPath;
}

/**
 * A simplified version of {@link computeEdgeCurvedPath} for delta = 0.
 */
function computeEdgeStraightPath(A: Offset, B: Offset): string {
    const leftDiff = B.left - A.left;
    const topDiff = B.top - A.top;
    const diagonal = Math.sqrt(leftDiff**2 + topDiff**2);

    const K = leftDiff / diagonal;
    const Q = topDiff / diagonal;

    const O_L = (A.left + B.left) / 2;
    const O_T = (A.top + B.top) / 2;

    const x = diagonal / 2 - EDGE_OFFSET;

    const Kx = K * x;
    const Qx = Q * x;

    const D_L = O_L + Kx;
    const D_T = O_T + Qx;

    const arcPath = `M ${O_L - Kx} ${O_T - Qx} L ${D_L} ${D_T}`;

    // Arrowheads. We have: tan_1 = -tan_2 = EDGE_ARROW_TAN, x_2 = x_1, y_2 = -y_1.
    const x_1 = x - EDGE_ARROW_LENGTH / Math.sqrt(1 + EDGE_ARROW_TAN**2);
    const y_1 = EDGE_ARROW_TAN * (x_1 - x);

    const S1_L = O_L + K * x_1 - Q * y_1;
    const S1_T = O_T + Q * x_1 + K * y_1;

    const S2_L = O_L + K * x_1 + Q * y_1;
    const S2_T = O_T + Q * x_1 - K * y_1;

    const arrowPath = `L ${S1_L} ${S1_T} L ${S2_L} ${S2_T} L ${D_L} ${D_T}`;

    return arcPath + ' ' + arrowPath;
}

export function computeSelectionBoxStyle(select: SelectState | undefined, coordinates: Coordinates): CSSProperties {
    if (!select)
        return { display: 'none' };

    const initial = positionToOffset(select.initial, coordinates);
    const current = positionToOffset(select.current, coordinates);

    return {
        display: 'block',
        left: `${Math.min(initial.left, current.left)}px`,
        top: `${Math.min(initial.top, current.top)}px`,
        width: `${Math.abs(initial.left - current.left)}px`,
        height: `${Math.abs(initial.top - current.top)}px`,
    };
}

export type HTMLConnection = {
    ref: HTMLElement;
    cleanup: () => void;
}
