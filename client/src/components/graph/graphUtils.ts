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
export class EdgeMap {
    private readonly fromMap = new Map<string, Edge[]>();
    private readonly toMap = new Map<string, Edge[]>();

    constructor(edges: Edge[]) {
        edges.forEach(edge => {
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

    getEdgesForNode(nodeId: string): { from: Edge[], to: Edge[] } {
        return {
            from: this.fromMap.get(nodeId) ?? [],
            to: this.toMap.get(nodeId) ?? [],
        };
    }
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

/** In pixels */
const EDGE_OFFSET = 20;

export function computeEdgeStyle(from: Node, to: Node, coordinates: Coordinates): CSSProperties {
    const start = positionToOffset(from.position, coordinates);
    const end = positionToOffset(to.position, coordinates);

    const left = (start.left + end.left) / 2;
    const top = (start.top + end.top) / 2;
    const x = end.left - start.left;
    const y = end.top - start.top;
    const width = Math.sqrt(x * x + y * y) - 2 * EDGE_OFFSET;
    const angle = Math.atan2(y, x);

    return {
        left: `${left}px`,
        top: `${top}px`,
        width: `${width}px`,
        transform: `translateX(-50%) rotate(${angle}rad)`,
    };

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
