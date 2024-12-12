import clsx from 'clsx';
import { type Dispatch, type SetStateAction, useEffect, useMemo, useRef, useState, type MouseEvent as ReactMouseEvent } from 'react';
import { cn } from './utils';

type Node = {
    id: string;
    label: string;
    position: Position;
};

type Edge = {
    id: string;
    label: string;
    from: string;
    to: string;
};

type GraphDisplayProps = Readonly<{
    nodes: Node[];
    edges: Edge[];
    width: number;
    height: number;
}>;

export function GraphDisplay({ nodes, edges, width, height }: GraphDisplayProps) {
    const [ state, engine ] = useGraphEngine({ nodes, edges, width, height });

    return (
        <div
            style={{ width, height }}
            className={clsx('relative bg-slate-400 overflow-hidden', state.drag ? 'cursor-grabbing' : 'cursor-default')}
            ref={engine.canvasRef}
            onMouseDown={e => engine.canvasDown(e)}
        >
            {state.nodes.map(node => (
                <NodeDisplay key={node.id} node={node} state={state} engine={engine} />
            ))}
            {edges.map(edge => (
                <EdgeDisplay key={edge.id} edge={edge} nodes={state.nodes} coordinates={state.coordinates} />
            ))}
            <SelectionBox state={state} />
        </div>
    );
}

type NodeDisplayProps = Readonly<{
    node: Node;
    state: GraphState;
    engine: GraphEngine;
}>;

function NodeDisplay({ node, state, engine }: NodeDisplayProps) {
    const isDragging = !!state.drag && 'nodeId' in state.drag && state.drag.nodeId === node.id;
    // We want to highlight the node when it's being dragged or hovered, but not when other dragged node is over it.
    // Also, no selection is allowed when dragging.
    const isHightlightAllowed = (!state.drag || isDragging) && !state.select;
    const isInSelecBox = state.select && isPointInBox(node.position, state.select);
    const isSelected = state.selectedNodes?.includes(node.id);
    if (isSelected)
        console.log(node);

    return (
        <div className='absolute w-0 h-0 select-none z-10' style={positionToOffset(node.position, state.coordinates)}>
            <div
                className={cn('absolute w-8 h-8 -left-4 -top-4 rounded-full border-2 border-slate-700 bg-white active:bg-cyan-300',
                    isHightlightAllowed && 'hover:shadow-[0_0_20px_0_rgba(0,0,0,0.3)] hover:shadow-cyan-300',
                    isDragging ? 'cursor-grabbing pointer-events-none' : 'cursor-pointer',
                    isInSelecBox && 'shadow-[0_0_20px_0_rgba(0,0,0,0.3)] shadow-cyan-300',
                    isSelected && 'bg-cyan-200',
                )}
                onClick={e => engine.nodeClick(e, node.id)}
                onMouseDown={e => engine.nodeDown(e, node.id)}
            />
            <div className='w-fit'>
                <span className='relative -left-1/2 -top-10 font-medium'>
                    {node.label}
                </span>
            </div>
        </div>
    );
}

/** In pixels */
const EDGE_OFFSET = 20;

type EdgeDisplayProps = Readonly<{
    edge: Edge;
    nodes: Node[];
    coordinates: Coordinates;
}>;

function EdgeDisplay({ edge, nodes, coordinates }: EdgeDisplayProps) {
    const cache = useMemo(() => ({
        from: nodes.find(node => node.id === edge.from)!,
        to: nodes.find(node => node.id === edge.to)!,
    }), [ edge, nodes ]);

    const start = positionToOffset(cache.from.position, coordinates);
    const end = positionToOffset(cache.to.position, coordinates);

    const center = {
        left: (start.left + end.left) / 2,
        top: (start.top + end.top) / 2,
    };
    const x = end.left - start.left;
    const y = end.top - start.top;
    const angle = Math.atan2(y, x);
    const width = Math.sqrt(x * x + y * y) - 2 * EDGE_OFFSET;

    return (
        <div className='absolute w-0 h-0 select-none' style={center}>
            <div className='absolute h-1 bg-slate-700 rounded-full' style={{ width, transform: `translateX(-50%) rotate(${angle}rad)` }}>

            </div>
        </div>
    );
}

type SelectionBoxProps = Readonly<{
    state: GraphState;
}>;

function SelectionBox({ state }: SelectionBoxProps) {
    if (!state.select)
        return null;

    const initial = positionToOffset(state.select.initial, state.coordinates);
    const current = positionToOffset(state.select.current, state.coordinates);

    const left = Math.min(initial.left, current.left);
    const top = Math.min(initial.top, current.top);
    const width = Math.abs(initial.left - current.left);
    const height = Math.abs(initial.top - current.top);

    return (
        <div
            className='absolute border-2 border-slate-700 border-dotted'
            style={{ left, top, width, height }}
        />
    );
}

function useGraphEngine(props: GraphDisplayProps) {
    const [ state, setState ] = useState(() => computeInitialState(props));
    const canvasRef = useRef<HTMLDivElement>(null);
    const engine = useMemo(() => new GraphEngine(setState, canvasRef), []);

    useEffect(() => {
        return engine.start();
    }, [ engine ]);

    return [ state, engine ] as const;
}

type GraphState = {
    nodes: Node[];
    edges: Edge[];
    coordinates: Coordinates;
    drag?: {
        /** The whole canvas is being dragged. */
        draggedPoint: Position;
    } | {
        /** Just the note is being dragged. */
        nodeId: string;
    };
    select?: {
        initial: Position;
        current: Position;
    };
    selectedNodes?: string[];
};

function computeInitialState({ nodes, edges, width, height }: GraphDisplayProps): GraphState {
    return {
        nodes,
        edges,
        coordinates: computeInitialCoordinates(nodes, width, height),
    };
}

type FullGraphOptions = {
    snapToGrid: boolean;
    /** All positions will be rounded to multiples of this size. In the relative units. */
    gridSize: number;
    /** Over how relative units we have to drag the node to start the dragging. */
    nodeDraggingThreshold: number | null;
};

type GraphOptions = Partial<FullGraphOptions>;

const defaultGraphOptions: FullGraphOptions = {
    snapToGrid: true,
    /** All positions will be rounded to multiples of this size. In the relative units. */
    gridSize: 20,
    /** Over how relative units we have to drag the node to start the dragging. */
    nodeDraggingThreshold: 20,
};

class GraphEngine {
    private readonly options: FullGraphOptions;

    constructor(
        private readonly setState: Dispatch<SetStateAction<GraphState>>,
        readonly canvasRef: React.RefObject<HTMLDivElement>,
        options: GraphOptions = {},
    ) {
        this.options = { ...defaultGraphOptions, ...options };
    }

    get canvas(): HTMLDivElement {
        return this.canvasRef.current!;
    }

    /** Returns the abort function */
    public start(): () => void {
        // The event is throttled because we don't need to update the state that often.
        const mousemove = throttle((e: MouseEvent) => this.globalMove(e));
        document.addEventListener('mousemove', mousemove);

        const mouseup = (e: MouseEvent) => this.globalUp(e);
        document.addEventListener('mouseup', mouseup);

        const wheel = (e: WheelEvent) => this.wheel(e);
        this.canvas.addEventListener('wheel', wheel, { passive: false });

        return () => {
            document.removeEventListener('mousemove', mousemove);
            document.removeEventListener('mouseup', mouseup);
            document.removeEventListener('wheel', wheel);
        };
    }

    private wheel(event: WheelEvent) {
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
        this.setState(state => {
            const coordinates = state.coordinates;
            const origin = { ...coordinates.origin };
            origin[direction] -= event.deltaY / 5;

            return { ...state, coordinates: { ...coordinates, origin } };
        });
    }

    private zoom(event: WheelEvent) {
        // We want to transform the coordinates in such a way that the mouse point will be on the same canvas position as before.
        // Therefore, it must hold origin.left + scale * mousePosition.x = newOrigin.left + newScale * mousePosition.x and the same for top and y.
        this.setState(state => {
            const coordinates = state.coordinates;
            const mousePosition = getMousePosition(event, this.canvas, coordinates);
            const scale = coordinates.scale * (1 - event.deltaY / 1000);
            const origin = {
                left: coordinates.origin.left + (coordinates.scale - scale) * mousePosition.x,
                top: coordinates.origin.top + (coordinates.scale - scale) * mousePosition.y,
            };

            return { ...state, coordinates: { origin, scale } };
        });
    }

    private dragging?: {
        type: 'node' | 'canvas';
        /** The difference between node position and mouse position. It should be kept constant during whole dragging process. */
        mouseDelta: Position;
    };
    // We don't want to start dragging the node immediately after the mouse down event. We wait for a small movement.
    private startDragging?: {
        nodeId: string;
        initialMouseOffset: Offset;
    };

    private isSelecting = false;

    public canvasDown(event: ReactMouseEvent<HTMLDivElement>) {
        // We are only interested in clicking on the actual canvas, not the nodes or edges. Also, we ignore the right click.
        if (event.target !== this.canvas)
            return;

        if (actions.drag.canvas === event.button) {
            this.dragging = {
                type: 'canvas',
                mouseDelta: { x: 0, y: 0 },
            };
            this.setState(state => {
                const draggedPoint = getMousePosition(event, this.canvas, state.coordinates);
                return { ...state, drag: { draggedPoint } };
            });
        }
        else if (actions.select.canvas === event.button) {
            this.isSelecting = true;
            this.setState(state => {
                const initial = getMousePosition(event, this.canvas, state.coordinates);
                return { ...state, select: { initial, current: initial } };
            });
        }
    }

    public nodeDown(event: ReactMouseEvent<HTMLDivElement>, nodeId: string) {
        if (actions.drag.node !== event.button)
            return;

        event.stopPropagation();
        this.startDragging = {
            nodeId,
            initialMouseOffset: getMouseOffset(event, this.canvas),
        };
    }

    private globalMove(event: MouseEvent) {
        if (this.startDragging)
            this.moveDragStart(event);
        else if (this.dragging)
            this.moveDrag(event);
        else if (this.isSelecting)
            this.moveSelect(event);
    }

    /**
     * Handle the first mouse move after the node was grabbed.
     */
    private moveDragStart(event: MouseEvent) {
        const { nodeId, initialMouseOffset } = this.startDragging!;

        this.setState(state => {
            // We want to precisely calculate the initial drag threshold, because the node might not be grabbed by its center.
            const initialMousePosition = offsetToPosition(initialMouseOffset, state.coordinates);
            const mousePosition = getMousePosition(event, this.canvas, state.coordinates);

            if (
                this.options.nodeDraggingThreshold !== null &&
                Math.abs(initialMousePosition.x - mousePosition.x) < this.options.nodeDraggingThreshold &&
                Math.abs(initialMousePosition.y - mousePosition.y) < this.options.nodeDraggingThreshold
            )
                // If there is threshold, we have to wait until it's reached.
                return state;

            // After the threshold is reached, the node starts being dragged.
            this.startDragging = undefined;
            const initialNodePosition = state.nodes.find(node => node.id === nodeId)!.position;
            const mouseDelta = this.options.snapToGrid
                // When snapping, we want to keep the mouse in the center of the node. However, this might change in the future if we introduce larger notes for which it would be unintuitive.
                ? { x: 0, y: 0 }
                : { x: initialNodePosition.x - initialMousePosition.x, y: initialNodePosition.y - initialMousePosition.y };

            this.dragging = { type: 'node', mouseDelta };

            const nodes = state.nodes.map(node => node.id !== nodeId ? node : { ...node, position: this.calculateNewNodePosition(mousePosition) });
            return { ...state, nodes, drag: { nodeId } };
        });
    }

    /**
     * Handle a mouse move while in the node-dragging state.
     */
    private moveDrag(event: MouseEvent) {
        this.setState(state => {
            if (!state.drag || !this.dragging)
                throw new Error('Unexpected state.');

            if ('nodeId' in state.drag) {
                const nodeId = state.drag.nodeId;
                const mousePosition = getMousePosition(event, this.canvas, state.coordinates);
                const nodes = state.nodes.map(node => node.id !== nodeId ? node : { ...node, position: this.calculateNewNodePosition(mousePosition) });
                return { ...state, nodes };
            }

            // The whole canvas is being dragged. We have to change the origin so that the dragged point is on the mouse offset.
            const mouseOffset = getMouseOffset(event, this.canvas);
            const coordinates = {
                ...state.coordinates,
                origin: {
                    left: mouseOffset.left - state.coordinates.scale * state.drag.draggedPoint.x,
                    top: mouseOffset.top - state.coordinates.scale * state.drag.draggedPoint.y,
                },
            };

            return { ...state, coordinates };
        });
    }

    /** Use this only when dragging! */
    private calculateNewNodePosition(mousePosition: Position): Position {
        const mouseDelta = this.dragging!.mouseDelta;
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

    private moveSelect(event: MouseEvent) {
        this.setState(state => {
            if (!state.select)
                throw new Error('Unexpected state.');

            const current = getMousePosition(event, this.canvas, state.coordinates);
            return { ...state, select: { ...state.select, current } };
        });
    }

    private globalUp(event: MouseEvent) {
        this.startDragging = undefined;

        if (this.dragging) {
            if (actions.drag[this.dragging.type] !== event.button)
                return;

            event.stopPropagation();
            this.dragging = undefined;
            this.setState(state => ({ ...state, drag: undefined }));
        }
        else if (this.isSelecting) {
            if (actions.select.canvas !== event.button)
                return;

            event.stopPropagation();
            this.isSelecting = false;
            this.setState(state => {
                const selectedNodes = state.nodes.filter(node => isPointInBox(node.position, state.select!)).map(node => node.id);
                return { ...state, select: undefined, selectedNodes };
            });
        }
    }

    public nodeClick(event: ReactMouseEvent<HTMLDivElement>, nodeId: string) {
        this.setState(state => {
            const originalSelectedNodes = state.selectedNodes ?? [];
            const withoutNode = originalSelectedNodes.filter(id => id !== nodeId);
            // If the node was selected, we deselect it.
            if (withoutNode.length !== originalSelectedNodes.length)
                return { ...state, selectedNodes: withoutNode };

            // The node wasn't selected. If the shift or ctrl key is pressed, we add the node to the selection. Otherwise, we select only this node.
            const selectedNodes = (event.shiftKey || event.ctrlKey) ? [ ...originalSelectedNodes, nodeId ] : [ nodeId ];
            return { ...state, selectedNodes };
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

function positionToOffset(position: Position, coordinates: Coordinates): Offset {
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

function computeInitialCoordinates(nodes: Node[], width: number, height: number): Coordinates {
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

function isPointInBox(point: Position, box: { initial: Position, current: Position }): boolean {
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
