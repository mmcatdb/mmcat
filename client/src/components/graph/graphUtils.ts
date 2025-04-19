import { compareStringsAscii } from '@/types/utils/common';
import { type CSSProperties } from 'react';

export type Node = {
    id: string;
    /** Mutable. Will be changed by the engine. */
    x: number;
    /** Mutable. Will be changed by the engine. */
    y: number;
};

export type Edge = {
    id: string;
    from: string;
    to: string;
    get label(): string;
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

    get(id: string): TEdge | undefined {
        return this.idMap.get(id);
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

    private bundledEdgesCache: TEdge[][] | undefined = undefined;

    get bundledEdges(): TEdge[][] {
        if (!this.bundledEdgesCache)
            this.bundledEdgesCache = EdgeMap.bundleEdges(this.idMap.values().toArray());

        return this.bundledEdgesCache;
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
    const xPositions = nodes.length === 0 ? [ 0 ] : nodes.map(node => node.x);
    const yPositions = nodes.length === 0 ? [ 0 ] : nodes.map(node => node.y);

    const minX = Math.min(...xPositions);
    const maxX = Math.max(...xPositions);
    const minY = Math.min(...yPositions);
    const maxY = Math.max(...yPositions);

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
    const offset = positionToOffset(node, coordinates);

    return {
        left: `${offset.left}px`,
        top: `${offset.top}px`,
    };
}

export const EDGE_ARROW_LENGTH = 16;
/** How far from the node center is the edge start / end (with the arrow head). In pixels. */
const EDGE_START_OFFSET = 20;
// The arrow starts at the end of the line, so the line has to be shorter on the end.
const EDGE_END_OFFSET = EDGE_START_OFFSET + EDGE_ARROW_LENGTH;
/** Half the height of the label line. */
const EDGE_LABEL_BASELINE_OFFSET = 6;
/** The distance between the middle points of edges between the two same nodes, divided by the distance of these nodes. */
const EDGE_DELTA_RATIO = 0.15;
/** The maximum between the middle points of edges between the two same nodes. In pixels. */
const EDGE_DELTA_MAX = 30;

type EdgeSvg = {
    path: string;
    label: {
        transform: string;
    } | undefined;
}

export function computeEdgeSvg(from: Node, to: Node, label: string, degree: number, coordinates: Coordinates): EdgeSvg {
    const start = positionToOffset(from, coordinates);
    const end = positionToOffset(to, coordinates);
    // Some heuristic. Not ideal tho.
    // TODO Replace by something like https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/measureText.
    const labelLength = label.length * 7 + 12;

    return degree === 0
        ? computeEdgeStraightPath(start, end, labelLength)
        : computeEdgeCurvedPath(start, end, labelLength, degree);
}

/**
 * A simplified version of {@link computeEdgeCurvedPath} for delta = 0.
 */
function computeEdgeStraightPath(A: Offset, B: Offset, labelLength: number): EdgeSvg {
    const tform = computeCommonTform(A, B);
    const { a_x, labelAngle } = tform;

    const { e_x, f_x, g_x, h_x, labelCenter_x, isTooSmall } = computeCommonPoints(labelLength, a_x);

    if (isTooSmall) {
        return {
            path: `M ${xToSvg(e_x, tform)} L ${xToSvg(h_x, tform)}`,
            label: undefined,
        };
    }

    const labelCenter = xToLeftTop(labelCenter_x, tform);

    return {
        path: `M ${xToSvg(e_x, tform)} L ${xToSvg(f_x, tform)} M ${xToSvg(g_x, tform)} L ${xToSvg(h_x, tform)}`,
        label: {
            transform: createLabelTransform(labelCenter, labelAngle),
        },
    };
}

function computeCommonPoints(labelLength: number, a_x: number) {
    const e_x = a_x - EDGE_START_OFFSET;
    const h_x = EDGE_END_OFFSET - a_x;

    const labelCenter_x = EDGE_ARROW_LENGTH / 2;

    const f_x = labelCenter_x + labelLength / 2;
    const g_x = labelCenter_x - labelLength / 2;

    const isTooSmall = (f_x > e_x) || (h_x > g_x);

    return { e_x, f_x, g_x, h_x, labelCenter_x, isTooSmall };
}

function createLabelTransform(center: Offset, angle: number): string {
    return `translate(${round(center.left)} ${round(center.top)}) rotate(${round(angle)}) translate(0 ${EDGE_LABEL_BASELINE_OFFSET})`;
}

type XTformConstants = { O_L: number, O_T: number, K: number, Q: number };

function xToSvg(x: number, c: XTformConstants): string {
    const { left, top } = xToLeftTop(x, c);
    return `${round(left)} ${round(top)}`;
}

function xToLeftTop(x: number, c: XTformConstants): Offset {
    return {
        left: c.O_L + c.K * x,
        top: c.O_T + c.Q * x,
    };
}

/**
 * Computes a curved SVG path from `A` to `B` that has a `degree` relative distance (in the middle) from a linear path between these two points. If `degree` is negative, the curve will be on the other side.
 * Uses circle arc.
 */
function computeEdgeCurvedPath(A: Offset, B: Offset, labelLength: number, degree: number): EdgeSvg {
    const { O_L, O_T, K, Q, a_x, labelAngle } = computeCommonTform(A, B);

    /** The distance between the highest point of the arc (H) and the diagonal. In pixels. Might be negative. */
    const delta = Math.min(2 * EDGE_DELTA_RATIO * a_x, EDGE_DELTA_MAX) * degree;
    // First, let's find the radius of the arc. We know that a_x^2 + (r - delta)^2 = r^2, where r is the radius of the arc. This gives us:
    const radius = (a_x**2 + delta**2) / (2 * delta);
    // Again, the radius might be negative (if delta is negative).

    // Lastly, we do a simple transformation - we move the origin to the point (0, radius - delta). The new coordinates will be called (x', y'). This simplifies the calculations to:
    // (x', y') = radius * (cos(phi), sin(phi))
    // L = M + K * x' - Q * y'
    // T = N + Q * x' + K * y'
    // Where:
    const M = O_L - Q * (delta - radius);
    const N = O_T + K * (delta - radius);

    const tform = { M, N, K, Q, radius } satisfies PhiTformConstants;

    // The path consists of two arc parts. The first one goes from e to f, the second one from g to h.
    // Both of them lie on the same arc, as well as points a, b and (0, ?). So, the points on the on arc follow as: a, e, f, (0, ?), g, h, b.
    // The arc distance from a to f is equal to start offset, the arc distance from g to b is equal to end offset.
    // The distance from f to the label center is the same as the distance from g to the label center.
    // The label center isn't at the point (0, ?), but it's shifted by the half of the arrow head length in the x direction.
    // This is because we want both lines (without the arrow head) to be the same length.

    // Any point on the arc can be computed as (r * cos(p), delta + r * (sin(p) - 1)), where t is the angle (measured in the positive direction starting in the 1st quadrant).
    // Therefore, the angle of the point a is:
    const a_phi = Math.acos(a_x / radius);
    const e_phi =           a_phi + EDGE_START_OFFSET / radius;
    const h_phi = Math.PI - a_phi - EDGE_END_OFFSET / radius;

    const labelCenter_phi = Math.PI / 2 - EDGE_ARROW_LENGTH / 2 / radius;

    const f_phi = labelCenter_phi - labelLength / 2 / radius;
    const g_phi = labelCenter_phi + labelLength / 2 / radius;

    const roundedRadius = Math.round(radius).toString();
    const sweepFlag = degree < 0 ? 0 : 1;
    const arcPrefix = `${roundedRadius} ${roundedRadius} 0 0 ${sweepFlag}`;

    // We use the same condition here as for the straight path. We want all labels to disappear at the same edge length.
    const { isTooSmall } = computeCommonPoints(labelLength, a_x);
    if (isTooSmall) {
        return {
            path: `M ${phiToSvg(e_phi, tform)} A ${arcPrefix} ${phiToSvg(h_phi, tform)}`,
            label: undefined,
        };
    }

    const labelCenter = phiToLeftTop(labelCenter_phi, tform);

    return {
        path: `M ${phiToSvg(e_phi, tform)} A ${arcPrefix} ${phiToSvg(f_phi, tform)} M ${phiToSvg(g_phi, tform)} A ${arcPrefix} ${phiToSvg(h_phi, tform)}`,
        label: {
            transform: createLabelTransform(labelCenter, labelAngle),
        },
    };
}

/**
 * The top-left coordinates of any point in the xy plane can be computed as:
 * L = O_L + K * x - Q * y
 * T = O_T + Q * x + K * y
 */
function computeCommonTform(A: Offset, B: Offset) {
    // Let's transform the coordinates to an xy plane (not to be confused with the relative Position units). We will use lowercased letters for the xy plane and capitals for the left-top coordinates.
    // The start and end points (A, B) are at the points a = (a_x, 0) and b = (-a_x, 0) respectively.
    const leftDiff = B.left - A.left;
    const topDiff = B.top - A.top;
    const diagonal = Math.sqrt(leftDiff**2 + topDiff**2);

    // This should be obvious.
    const a_x = diagonal / 2;

    // Any vector (x, y) can be transformed to the left-top coordinates (L, T) by the following formulas (where X_*, Y_* are now unknown coefficients):
    // L = X_L * x + Y_L * y
    // T = X_T * x + Y_T * y
    // In order to find the coefficients, we first transform the vector AB. This gives us:
    // X_L = (B_L - A_L) / (-2 * a_x)
    // X_T = (B_T - A_T) / (-2 * a_x)
    // Let's substitute K = X_L and Q = X_T for simplicity.
    const K = -leftDiff / diagonal;
    const Q = -topDiff / diagonal;

    // Now, we want to transform the vector OH, where O is the origin of our new coordinates (0, 0). We need any linearly independent vector (with AB) and this one should be the easiest.
    // Let's say the point H is at (0, y_0), where y_0 = delta * sign(degree). Through some geometry, we can find that:
    // O_L = (A_L + B_L) / 2
    // O_T = (A_T + B_T) / 2
    // OH_L =  (B_T - A_T) * delta / diagonal = Y_L * y_0
    // OH_T = -(B_L - A_L) * delta / diagonal = Y_T * y_0

    // Which gives us:
    // Y_L =  (B_T - A_T) / diagonal * sign = -Q * sign
    // Y_T = -(B_L - A_L) / diagonal * sign =  K * sign

    // Note that this holds only for vectors, not for points. In order to transform a point, we have to create a vector from some well-known point (e.g., the origin) to the point, transform the vector, and then add the result to the well-known point in the left-top coordinates.
    const O_L = (A.left + B.left) / 2;
    const O_T = (A.top + B.top) / 2;

    // Some goodies we will need later.
    const rawAngle = Math.atan2(topDiff, leftDiff) * 180 / Math.PI;
    const labelAngle = rawAngle + (Math.abs(rawAngle) > 90 ? 180 : 0);

    return { O_L, O_T, K, Q, a_x, labelAngle };
}

type PhiTformConstants = { M: number, N: number, K: number, Q: number, radius: number };

function phiToSvg(phi: number, c: PhiTformConstants): string {
    const { left, top } = phiToLeftTop(phi, c);
    return `${round(left)} ${round(top)}`;
}

function phiToLeftTop(phi: number, c: PhiTformConstants): Offset {
    const x = c.radius * Math.cos(phi);
    const y = c.radius * Math.sin(phi);

    return {
        left: c.M + c.K * x - c.Q * y,
        top: c.N + c.Q * x + c.K * y,
    };
}

function round(value: number): string {
    return value.toFixed(2);
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
