import { type Dispatch, type SetStateAction, useEffect, useMemo, useRef, useState, type MouseEvent as ReactMouseEvent } from 'react';

export type Node = {
    id: string;
    label: string;
    position: Position;
};

export type Edge = {
    id: string;
    label: string;
    from: string;
    to: string;
};

export type GraphValue = {
    nodes: Node[];
    edges: Edge[];
    /** Map of { nodeId: Node }. */
    selectedNodes: Record<string, Node>;
    // TODO
    // selectedEdges: Record<string, Edge>;
};

/**
 * User preferences.
 */
type FullGraphOptions = {
    snapToGrid: boolean;
    /** All positions will be rounded to multiples of this size. In the relative units. */
    gridSize: number;
    /** Over how relative units we have to drag the node to start the dragging. */
    nodeDraggingThreshold: number | null;
    /** In px. Used for determining the initial coordinates. */
    initialWidth: number;
    /** In px. Used for determining the initial coordinates. */
    initialHeight: number;
};

// TODO Use some global user preferences for this.
export type GraphOptions = Partial<FullGraphOptions>;

const defaultGraphOptions: FullGraphOptions = {
    snapToGrid: false,
    gridSize: 20,
    // At least some threshold is needed to prevent accidental dragging.
    // TODO find a better way (maybe with shift/ctrl or something).
    nodeDraggingThreshold: 2,
    initialWidth: 1200,
    initialHeight: 600,
};

export function useGraphEngine(value: GraphValue, setValue: Dispatch<SetStateAction<GraphValue>>, options: GraphOptions = {}) {
    const [ state, setState ] = useState<ReactiveGraphState>(() => createInitialGraphState(value, options));
    const canvasRef = useRef<HTMLDivElement>(null);
    const engine = useMemo(() => new GraphEngine(setValue, state, setState, canvasRef, { ...defaultGraphOptions, ...options }), [ setValue, options ]);

    useEffect(() => {
        return engine.setup();
    }, [ engine ]);

    return [ state, engine ] as const;
}

/**
 * The internal state of the graph engine that is propagated to the UI.
 */
export type ReactiveGraphState = {
    coordinates: Coordinates;
    drag?: DragState;
    select?: SelectState;
};

function createInitialGraphState(value: GraphValue, options: GraphOptions): ReactiveGraphState {
    const fullOptions = { ...defaultGraphOptions, ...options };
    const coordinates = computeCoordinates(value.nodes, fullOptions.initialWidth, fullOptions.initialHeight);

    return { coordinates };
}

type DragState = {
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

type SelectState = {
    initial: Position;
    current: Position;
};

export class GraphEngine {
    constructor(
        private readonly setValue: Dispatch<SetStateAction<GraphValue>>,
        /** A local copy of the state. Contains only those properties that should be reactive (i.e., the UI should change when they change). */
        private state: ReactiveGraphState,
        /** Any change to the state should be immediatelly propagated up. */
        private readonly propagateState: Dispatch<ReactiveGraphState>,
        readonly canvasRef: React.RefObject<HTMLDivElement>,
        private readonly options: FullGraphOptions,
    ) {}

    get canvas(): HTMLDivElement {
        return this.canvasRef.current!;
    }

    /**
     * Updates the inner state and propagates it to the parent component.
     */
    private updateState(edit: Partial<ReactiveGraphState>) {
        this.state = { ...this.state, ...edit };
        this.propagateState(this.state);
    }

    /** Returns the abort function */
    public setup(): () => void {
        // The event is throttled because we don't need to update the state that often.
        const mousemove = throttle((e: MouseEvent) => this.handleGlobalMousemove(e));
        document.addEventListener('mousemove', mousemove);

        const mouseup = (e: MouseEvent) => this.handleGlobalMouseup(e);
        document.addEventListener('mouseup', mouseup);

        const wheel = (e: WheelEvent) => this.handleCanvasWheel(e);
        this.canvas.addEventListener('wheel', wheel, { passive: false });

        return () => {
            document.removeEventListener('mousemove', mousemove);
            document.removeEventListener('mouseup', mouseup);
            document.removeEventListener('wheel', wheel);
        };
    }

    private handleCanvasWheel(event: WheelEvent) {
        // Prevent default actions like zooming in/out the whole page or scrolling.
        event.preventDefault();

        if (event.ctrlKey && event.shiftKey)
            return;
        if (event.ctrlKey)
            this.zoom(event);
        else
            this.move(event, event.shiftKey ? 'left' : 'top');
    }

    private move(event: WheelEvent, direction: 'top' | 'left') {
        const coordinates = this.state.coordinates;
        const origin = { ...coordinates.origin };
        origin[direction] -= event.deltaY / 5;

        this.updateState({ coordinates: { ...coordinates, origin } });
    }

    private zoom(event: WheelEvent) {
        // We want to transform the coordinates in such a way that the mouse point will be on the same canvas position as before.
        // Therefore, it must hold origin.left + scale * mousePosition.x = newOrigin.left + newScale * mousePosition.x and the same for top and y.
        const coordinates = this.state.coordinates;
        const mousePosition = getMousePosition(event, this.canvas, coordinates);
        const scale = coordinates.scale * (1 - event.deltaY / 1000);
        const origin = {
            left: coordinates.origin.left + (coordinates.scale - scale) * mousePosition.x,
            top: coordinates.origin.top + (coordinates.scale - scale) * mousePosition.y,
        };

        this.updateState({ coordinates: { origin, scale } });
    }

    // We don't want to start dragging the node immediately after the mouse down event. We wait for a small movement.
    private startDragging?: {
        nodeId: string;
        initialMousePosition: Position;
        mouseDelta: Position;
    };

    public handleCanvasMousedown(event: ReactMouseEvent<HTMLDivElement>) {
        // We are only interested in clicking on the actual canvas, not the nodes or edges. Also, we ignore the right click.
        if (event.target !== this.canvas)
            return;

        if (actions.drag.canvas === event.button) {
            const draggedPoint = getMousePosition(event, this.canvas, this.state.coordinates);
            this.updateState({ drag: { type: 'canvas', draggedPoint } });
        }
        else if (actions.select.canvas === event.button) {
            const initial = getMousePosition(event, this.canvas, this.state.coordinates);
            this.updateState({ select: { initial, current: initial } });
        }
    }

    public handleNodeMousedown(event: ReactMouseEvent<HTMLDivElement>, nodeId: string) {
        if (actions.drag.node !== event.button)
            return;

        event.stopPropagation();

        this.setValue(value => {
            const mouseOffset = getMouseOffset(event, this.canvas);
            const initialMousePosition = offsetToPosition(mouseOffset, this.state.coordinates);

            const node = value.nodes.find(node => node.id === nodeId);
            if (!node) {
                // This might happen if the node was deleted.
                console.warn(`Node ${nodeId} not found in moveDragStart.`);
                return value;
            }

            const nodePosition = node.position;
            const mouseDelta = this.options.snapToGrid
                // When snapping, we want to keep the mouse in the center of the node. However, this might change in the future if we introduce larger notes for which it would be unintuitive.
                ? { x: 0, y: 0 }
                : { x: nodePosition.x - initialMousePosition.x, y: nodePosition.y - initialMousePosition.y };

            this.startDragging = {
                nodeId,
                initialMousePosition,
                mouseDelta,
            };

            return value;
        });
    }

    private handleGlobalMousemove(event: MouseEvent) {
        if (this.startDragging)
            this.moveDragStart(event, this.startDragging);
        else if (this.state.drag)
            this.moveDrag(event, this.state.drag);
        else if (this.state.select)
            this.moveSelect(event, this.state.select);
    }

    /**
     * Handle the first mouse move after the node was grabbed.
     */
    private moveDragStart(event: MouseEvent, { nodeId, initialMousePosition, mouseDelta }: { nodeId: string, initialMousePosition: Position, mouseDelta: Position }) {
        // We want to precisely calculate the initial drag threshold, because the node might not be grabbed by its center.

        const mousePosition = getMousePosition(event, this.canvas, this.state.coordinates);

        if (
            this.options.nodeDraggingThreshold !== null &&
            Math.abs(initialMousePosition.x - mousePosition.x) < this.options.nodeDraggingThreshold &&
            Math.abs(initialMousePosition.y - mousePosition.y) < this.options.nodeDraggingThreshold
        )
            // If there is threshold, we have to wait until it's reached.
            return;

        // After the threshold is reached, the node starts being dragged.
        this.startDragging = undefined;
        this.updateState({ drag: { type: 'node', nodeId, mouseDelta } });

        this.setValue(value => {
            const nodes = value.nodes.map(node => node.id !== nodeId ? node : { ...node, position: this.calculateNewNodePosition(mousePosition, mouseDelta) });
            return { ...value, nodes };
        });
    }

    /**
     * Handle a mouse move while in the node-dragging state.
     */
    private moveDrag(event: MouseEvent, drag: DragState) {
        if ('nodeId' in drag) {
            const nodeId = drag.nodeId;
            const mousePosition = getMousePosition(event, this.canvas, this.state.coordinates);
            const mouseDelta = drag.mouseDelta;

            this.setValue(value => {
                const nodes = value.nodes.map(node => node.id !== nodeId ? node : { ...node, position: this.calculateNewNodePosition(mousePosition, mouseDelta) });
                return { ...value, nodes };
            });
            return;
        }

        // The whole canvas is being dragged. We have to change the origin so that the dragged point is on the mouse offset.
        const mouseOffset = getMouseOffset(event, this.canvas);
        const coordinates = {
            ...this.state.coordinates,
            origin: {
                left: mouseOffset.left - this.state.coordinates.scale * drag.draggedPoint.x,
                top: mouseOffset.top - this.state.coordinates.scale * drag.draggedPoint.y,
            },
        };

        this.updateState({ coordinates });
    }

    private calculateNewNodePosition(mousePosition: Position, mouseDelta: Position): Position {
        const newNodePosition: Position = {
            x: mousePosition.x + mouseDelta.x,
            y: mousePosition.y + mouseDelta.y,
        };

        return this.options.snapToGrid ? this.roundPositionToGrid(newNodePosition) : newNodePosition;
    }

    private roundPositionToGrid(position: Position): Position {
        return {
            x: Math.round(position.x / this.options.gridSize) * this.options.gridSize,
            y: Math.round(position.y / this.options.gridSize) * this.options.gridSize,
        };
    }

    private moveSelect(event: MouseEvent, select: SelectState) {
        const current = getMousePosition(event, this.canvas, this.state.coordinates);
        this.updateState({ select: { ...select, current } });
    }

    private handleGlobalMouseup(event: MouseEvent) {
        this.startDragging = undefined;

        if (this.state.drag) {
            if (actions.drag[this.state.drag.type] !== event.button)
                return;

            event.stopPropagation();
            this.updateState({ drag: undefined });
            return;
        }

        if (this.state.select) {
            if (actions.select.canvas !== event.button)
                return;

            const select = this.state.select;
            event.stopPropagation();
            this.updateState({ select: undefined });

            this.setValue(value => {
                const selectedNodes: Record<string, Node> = {};
                value.nodes
                    .filter(node => {
                        const isInBox = isPointInBox(node.position, select);
                        return (event.shiftKey || event.ctrlKey)
                            // If the special key is pressed, we toggle the selection.
                            ? (isInBox && !value.selectedNodes[node.id] || !isInBox && value.selectedNodes[node.id])
                            // Otherwise, we simply select only the nodes in the box.
                            : isInBox;
                    })
                    .forEach(node => selectedNodes[node.id] = node);

                return { ...value, selectedNodes };
            });
            this.updateState({ select: undefined });
        }
    }

    public handleNodeClick(event: ReactMouseEvent<HTMLDivElement>, nodeId: string) {
        this.setValue(value => {
            if (value.selectedNodes[nodeId]) {
                // If the node was selected, we deselect it.
                const selectedNodes: Record<string, Node> = { ...value.selectedNodes };
                delete selectedNodes[nodeId];
                return { ...value, selectedNodes };
            }

            // The node wasn't selected.
            const node = value.nodes.find(node => node.id === nodeId);
            if (!node) {
                // Kinda unexpected but probably might happen if the node array changes.
                console.warn(`Node ${nodeId} not found in handleNodeClick.`);
                return value;
            }

            const selectedNodes = (event.shiftKey || event.ctrlKey)
                // If the shift or ctrl key is pressed, we add the node to the selection.
                ? { ...value.selectedNodes, [node.id]: node }
                // Otherwise, we select only this node.
                : { [node.id]: node };

            return { ...value, selectedNodes };
        });
    }
}

const LEFT_BUTTON = 0;
const MIDDLE_BUTTON = 1;

const actions = {
    drag: {
        canvas: MIDDLE_BUTTON,
        node: LEFT_BUTTON,
    },
    select: {
        canvas: LEFT_BUTTON,
    },
};

// Math

/** Internal position of the nodes. In some relative units. */
type Position = {
    x: number;
    y: number;
};

/** Distance from the top-left corner of the canvas. In pixels. */
type Offset = {
    left: number;
    top: number;
};

type Coordinates = {
    /** Where the position { x: 0; y: 0 } is on the canvas. */
    origin: Offset;
    /** Distance in pixels = scale * distance in relative units. */
    scale: number;
}

function offsetToPosition(offset: Offset, coordinates: Coordinates): Position {
    return {
        x: (offset.left - coordinates.origin.left) / coordinates.scale,
        y: (offset.top - coordinates.origin.top) / coordinates.scale,
    };
}

export function positionToOffset(position: Position, coordinates: Coordinates): Offset {
    return {
        left: coordinates.origin.left + coordinates.scale * position.x,
        top: coordinates.origin.top + coordinates.scale * position.y,
    };
}

function getMouseOffset(event: { clientX: number, clientY: number }, canvas: HTMLDivElement): Offset {
    const rect = canvas.getBoundingClientRect();
    return {
        left: event.clientX - rect.left,
        top: event.clientY - rect.top,
    };
}

function getMousePosition(event: { clientX: number, clientY: number }, canvas: HTMLDivElement, coordinates: Coordinates): Position {
    return offsetToPosition(getMouseOffset(event, canvas), coordinates);
}

function computeCoordinates(nodes: Node[], width: number, height: number): Coordinates {
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

export function isPointInBox(point: Position, box: SelectState): boolean {
    return Math.min(box.initial.x, box.current.x) < point.x
        && Math.max(box.initial.x, box.current.x) > point.x
        && Math.min(box.initial.y, box.current.y) < point.y
        && Math.max(box.initial.y, box.current.y) > point.y;
}

const THROTTLE_DURATION_MS = 20;
function throttle<T extends(...args: Parameters<T>) => void>(callback: T): T {
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
