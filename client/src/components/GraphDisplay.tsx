import clsx from 'clsx';
import { type Dispatch, type SetStateAction, useEffect, useMemo, useRef, useState, type MouseEvent as ReactMouseEvent } from 'react';

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

    console.log('render: ' + Math.random());

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

    function click(e: ReactMouseEvent<HTMLDivElement>) {
        console.log(e);
    }

    return (
        <div className='absolute w-0 h-0 select-none z-10' style={positionToOffset(node.position, state.coordinates)}>
            <div
                className={clsx('absolute w-8 h-8 -left-4 -top-4 rounded-full border-2 border-slate-700 bg-white hover:bg-green-400 active:bg-green-500',
                    isDragging ? 'cursor-grabbing pointer-events-none' : 'cursor-pointer',
                )}
                onClick={click}
                onMouseDown={e => engine.nodeDown(e, node.id)}
            />
            <div className='w-fit'>
                <span className='relative -left-1/2 -top-10'>
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
};

function computeInitialState({ nodes, edges, width, height }: GraphDisplayProps): GraphState {
    return {
        nodes,
        edges,
        coordinates: computeInitialCoordinates(nodes, width, height),
    };
}

class GraphEngine {
    constructor(
        private readonly setState: Dispatch<SetStateAction<GraphState>>,
        readonly canvasRef: React.RefObject<HTMLDivElement>,
    ) {}

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

    // We don't want to start dragging the node immediately after the mouse down event. We wait for a small movement.
    private dragging?: 'node' | 'canvas';
    private startDragging?: {
        nodeId: string;
        mouseOffset: Offset;
    };

    public canvasDown(event: ReactMouseEvent<HTMLDivElement>) {
        // We are only interested in clicking on the actual canvas, not the nodes or edges. Also, we ignore the right click.
        if (event.target !== this.canvas || !draggable.canvas.includes(event.button))
            return;

        this.dragging = 'canvas';
        this.setState(state => {
            const draggedPoint = getMousePosition(event, this.canvas, state.coordinates);
            return { ...state, drag: { draggedPoint } };
        });
    }

    public nodeDown(event: ReactMouseEvent<HTMLDivElement>, nodeId: string) {
        if (!draggable.node.includes(event.button))
            return;

        event.stopPropagation();
        this.startDragging = {
            nodeId,
            mouseOffset: getMouseOffset(event, this.canvas),
        };
    }

    private globalMove(event: MouseEvent) {
        if (this.startDragging) {
            const { nodeId, mouseOffset } = this.startDragging;
            const currentOffset = getMouseOffset(event, this.canvas);
            if (Math.abs(mouseOffset.left - currentOffset.left) < 1 && Math.abs(mouseOffset.top - currentOffset.top) < 1)
                return;

            this.startDragging = undefined;
            this.dragging = 'node';
            this.setState(state => ({ ...state, drag: { nodeId } }));
            return;
        }

        if (!this.dragging)
            return;

        this.setState(state => {
            if (!state.drag)
                throw new Error('Unexpected state.');

            if ('nodeId' in state.drag) {
                const nodeId = state.drag.nodeId;
                const mousePosition = getMousePosition(event, this.canvas, state.coordinates);
                const nodes = state.nodes.map(node => node.id !== nodeId ? node : { ...node, position: mousePosition });

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

    private globalUp(event: MouseEvent) {
        this.startDragging = undefined;

        if (!this.dragging || !draggable[this.dragging].includes(event.button))
            return;

        event.stopPropagation();
        this.dragging = undefined;
        this.setState(state => ({ ...state, drag: undefined }));
    }
}

const LEFT_BUTTON = 0;
const MIDDLE_BUTTON = 1;

const draggable = {
    canvas: [ MIDDLE_BUTTON ],
    node: [ LEFT_BUTTON ],
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
