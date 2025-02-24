import { type Dispatch, type MouseEvent as ReactMouseEvent } from 'react';
import { computeCoordinates, computeEdgePath, computeNodeStyle, computeSelectionBoxStyle, type Coordinates, type DragState, type Edge, EdgeMap, getEdgeDegree, getMouseOffset, getMousePosition, type HTMLConnection, isEdgeInBox, isPointInBox, type Node, offsetToPosition, type Position, type SelectState, throttle } from './graphUtils';

export type Graph = {
    nodes: Node[];
    edges: Edge[];
};

/** User preferences. */
type FullGraphOptions = {
    /** If true, the nodes can be moved to only discrete coordinates. */
    snapToGrid: boolean;
    /** All positions will be rounded to multiples of this size. In the relative units. */
    gridSize: number;
    /** Over how many relative units we have to drag the node to start the dragging. */
    nodeDraggingThreshold: number | null;
};

// TODO Use some global user preferences for this.
export type GraphOptions = Partial<FullGraphOptions>;

export const defaultGraphOptions: FullGraphOptions = {
    snapToGrid: false,
    gridSize: 20,
    // At least some threshold is needed to prevent accidental dragging.
    // There is no such thing for selection, because we want to start selecting immediately. This allows us to clear selection by clicking on the canvas.
    nodeDraggingThreshold: 2,
};

/** Common type for all events the graph can emit. */
export type GraphEvent = GraphMoveEvent | GraphSelectEvent;

export type GraphMoveEvent = {
    type: 'move';
    nodeId: string;
    position: Position;
};

export type GraphSelectEvent = {
    type: 'select';
    nodeIds: string[];
    edgeIds: string[];
    /** A special key like ctrl or shift was held during the event. */
    isSpecialKey: boolean;
}

/** The internal state of the graph engine that is propagated to the UI. */
export type ReactiveGraphState = {
    coordinates: Coordinates;
    drag?: DragState;
    select?: SelectState;
};

export function createInitialGraphState(graph: Graph): ReactiveGraphState {
    return {
        // These values really don't matter, because they will be recomputed after the first render.
        // We just want to select something that won't break the svg path computations.
        coordinates: computeCoordinates(graph.nodes, 1000, 1000),
    };
}

export class GraphEngine {
    private nodeMap: Map<string, Node>;
    private edgeMap: EdgeMap;

    constructor(
        input: Graph,
        private readonly dispatch: Dispatch<GraphEvent>,
        /** A local copy of the state. Contains only those properties that should be reactive (i.e., the UI should change when they change). */
        private state: ReactiveGraphState,
        /** Any change to the state should be immediatelly propagated up. */
        private readonly propagateState: Dispatch<ReactiveGraphState>,
        private readonly options: FullGraphOptions,
    ) {
        console.log('CREATE Graph Engine');

        this.nodeMap = new Map(input.nodes.map(node => [ node.id, { ...node } ]));
        this.edgeMap = new EdgeMap(input.edges.map(edge => ({ ...edge })));
    }

    /**
     * Updates the inner state and propagates it to the parent component.
     */
    private updateState(edit: Partial<ReactiveGraphState>) {
        this.state = { ...this.state, ...edit };
        this.propagateState(this.state);
    }

    /** Use this for the engine to start listening to events. Returns a cleanup function. */
    setup(): () => void {
        console.log('SETUP graph engine');

        // The event is throttled because we don't need to update the state that often.
        const mousemove = throttle((e: MouseEvent) => this.handleGlobalMousemove(e));
        document.addEventListener('mousemove', mousemove);

        const mouseup = (e: MouseEvent) => this.handleGlobalMouseup(e);
        document.addEventListener('mouseup', mouseup);

        return () => {
            console.log('CLEANUP graph engine');

            document.removeEventListener('mousemove', mousemove);
            document.removeEventListener('mouseup', mouseup);
        };
    }

    update(input: Graph) {
        console.log('UPDATE graph engine');

        this.nodeMap = new Map(input.nodes.map(node => [ node.id, { ...node } ]));
        this.edgeMap = new EdgeMap(input.edges.map(edge => ({ ...edge })));
    }

    private canvasConnection?: HTMLConnection;
    private isCanvasMeasured = false;

    get canvas(): HTMLElement {
        return this.canvasConnection!.ref;
    }

    setCanvasRef(ref: HTMLElement | null) {
        if (!ref) {
            console.log('DELETE canvas ref');
            this.canvasConnection?.cleanup();
            this.canvasConnection = undefined;
        }
        else {
            console.log('CREATE canvas ref');

            const wheel = (e: WheelEvent) => this.handleCanvasWheel(e);
            ref.addEventListener('wheel', wheel, { passive: false });

            const cleanup = () => ref.removeEventListener('wheel', wheel);
            this.canvasConnection = { ref, cleanup };

            if (!this.isCanvasMeasured) {
                this.isCanvasMeasured = true;
                const { width, height } = ref.getBoundingClientRect();
                this.updateState({ coordinates: computeCoordinates([ ...this.nodeMap.values() ], width, height) });
            }
        }
    }

    private readonly nodeRefs = new Map<string, HTMLElement>();

    setNodeRef(nodeId: string, ref: HTMLElement | null) {
        if (!ref) {
            console.log('DELETE node ref');
            this.nodeRefs.delete(nodeId);
        }
        else {
            console.log('CREATE node ref');
            this.nodeRefs.set(nodeId, ref);
        }
    }

    private propagateNode(node: Node) {
        const ref = this.nodeRefs.get(node.id);
        if (!ref) {
            console.warn(`Node ref ${node.id} not found in propagateNode.`);
            return;
        }

        Object.assign(ref.style, computeNodeStyle(node, this.state.coordinates));

        const { from, to } = this.edgeMap.getEdgesForNode(node.id);
        EdgeMap.bundleEdges([ ...from, ...to ]).forEach(bundle => bundle.forEach((edge, index) => this.propagateEdge(edge, index, bundle.length)));
    }

    private readonly edgeRefs = new Map<string, SVGPathElement>();

    setEdgeRef(edgeId: string, ref: SVGPathElement | null) {
        if (!ref) {
            console.log('DELETE edge ref');
            this.edgeRefs.delete(edgeId);
        }
        else {
            console.log('CREATE edge ref');
            this.edgeRefs.set(edgeId, ref);
        }
    }

    private propagateEdge(edge: Edge, index: number, total: number) {
        const ref = this.edgeRefs.get(edge.id);
        if (!ref) {
            console.warn(`Edge ref ${edge.id} not found in propagateEdge.`);
            return;
        }

        const from = this.nodeMap.get(edge.from)!;
        const to = this.nodeMap.get(edge.to)!;

        const path = computeEdgePath(from, to, getEdgeDegree(edge, index, total), this.state.coordinates);
        ref.setAttribute('d', path);
    }

    private selectionBoxRef?: HTMLElement;

    setSelectionBoxRef(ref: HTMLElement | null) {
        if (!ref) {
            console.log('DELETE selection box ref');
            this.selectionBoxRef = undefined;
        }
        else {
            console.log('CREATE selection box ref');
            this.selectionBoxRef = ref;
        }
    }

    private propagateSelectionBox(select: SelectState | undefined) {
        const ref = this.selectionBoxRef;
        if (!ref) {
            console.warn('Selection box ref not found in propagateSelectionBox.');
            return;
        }

        Object.assign(ref.style, computeSelectionBoxStyle(select, this.state.coordinates));
    }

    private handleCanvasWheel(event: WheelEvent) {
        // Prevent default actions like zooming in/out the whole page or scrolling.
        event.preventDefault();

        if (event.ctrlKey && event.shiftKey)
            return;
        if (event.ctrlKey)
            this.zoom(event);
        else
            this.move(event, event.shiftKey);
    }

    private move(event: WheelEvent, switchAxis: boolean) {
        const coordinates = this.state.coordinates;
        const origin = { ...coordinates.origin };
        origin.top -= (switchAxis ? event.deltaX : event.deltaY) / 5;
        origin.left -= (switchAxis ? event.deltaY : event.deltaX) / 5;

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

    handleCanvasMousedown(event: ReactMouseEvent<HTMLElement>) {
        // We are only interested in clicking on the actual canvas, not the nodes or edges. Also, we ignore the right click.
        if (event.target !== this.canvas)
            return;

        if (actionButtons.drag.canvas === event.button) {
            const draggedPoint = getMousePosition(event, this.canvas, this.state.coordinates);
            this.updateState({ drag: { type: 'canvas', draggedPoint } });
        }
        else if (actionButtons.select.canvas === event.button) {
            const initial = getMousePosition(event, this.canvas, this.state.coordinates);
            this.updateState({ select: { initial, current: initial } });
        }
    }

    // We don't want to start dragging the node immediately after the mouse down event. We wait for a small movement.
    private startDragging?: {
        nodeId: string;
        initial: Position;
        mouseDelta: Position;
    };

    handleNodeMousedown(event: ReactMouseEvent<HTMLElement>, nodeId: string) {
        if (actionButtons.drag.node !== event.button)
            return;

        event.stopPropagation();

        const mouseOffset = getMouseOffset(event, this.canvas);
        const initial = offsetToPosition(mouseOffset, this.state.coordinates);

        const node = this.nodeMap.get(nodeId)!;

        const nodePosition = node.position;
        const mouseDelta = this.options.snapToGrid
        // When snapping, we want to keep the mouse in the center of the node. However, this might change in the future if we introduce larger notes for which it would be unintuitive.
            ? { x: 0, y: 0 }
            : { x: nodePosition.x - initial.x, y: nodePosition.y - initial.y };

        if (this.options.nodeDraggingThreshold)
            this.startDragging = { nodeId, initial, mouseDelta };
        else
            this.updateState({ drag: { type: 'node', nodeId, mouseDelta } });
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
    private moveDragStart(event: MouseEvent, { nodeId, initial, mouseDelta }: { nodeId: string, initial: Position, mouseDelta: Position }) {
        // We need to precisely calculate the initial drag threshold, because the node might not be grabbed by its center.
        const mousePosition = getMousePosition(event, this.canvas, this.state.coordinates);

        if (
            this.options.nodeDraggingThreshold !== null &&
            Math.abs(initial.x - mousePosition.x) < this.options.nodeDraggingThreshold &&
            Math.abs(initial.y - mousePosition.y) < this.options.nodeDraggingThreshold
        )
            // If there is threshold, we have to wait until it's reached.
            return;

        // After the threshold is reached, the node starts being dragged.
        this.startDragging = undefined;
        this.updateState({ drag: { type: 'node', nodeId, mouseDelta } });

        const node = this.nodeMap.get(nodeId)!;
        node.position = this.calculateNewNodePosition(mousePosition, mouseDelta);
        this.propagateNode(node);
    }

    /**
     * Handle a mouse move while in the node or canvas dragging state.
     */
    private moveDrag(event: MouseEvent, drag: DragState) {
        if (drag.type === 'node') {
            const nodeId = drag.nodeId;
            const mousePosition = getMousePosition(event, this.canvas, this.state.coordinates);
            const mouseDelta = drag.mouseDelta;

            const node = this.nodeMap.get(nodeId)!;
            node.position = this.calculateNewNodePosition(mousePosition, mouseDelta);
            this.propagateNode(node);

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

    /**
     * Handle a mouse move while in the selection state.
     */
    private moveSelect(event: MouseEvent, select: SelectState) {
        const current = getMousePosition(event, this.canvas, this.state.coordinates);
        this.state.select = { ...select, current };
        this.propagateSelectionBox(this.state.select);
    }

    private handleGlobalMouseup(event: MouseEvent) {
        this.startDragging = undefined;

        const { drag, select } = this.state;
        if (drag) {
            if (actionButtons.drag[drag.type] !== event.button)
                return;

            event.stopPropagation();
            if (drag.type === 'node') {
                const node = this.nodeMap.get(drag.nodeId)!;
                this.dispatch({ type: 'move', nodeId: node.id, position: node.position });
            }

            this.updateState({ drag: undefined });
            return;
        }

        if (select) {
            if (actionButtons.select.canvas !== event.button)
                return;

            event.stopPropagation();
            this.updateState({ select: undefined });

            const nodeIds = [ ...this.nodeMap.values() ]
                .filter(node => isPointInBox(node.position, select.initial, select.current))
                .map(node => node.id);


            const edgeIds = [ ...this.edgeMap.values() ]
                .filter(edge => {
                    const from = this.nodeMap.get(edge.from)!;
                    const to = this.nodeMap.get(edge.to)!;
                    return isEdgeInBox(from.position, to.position, select.initial, select.current);
                })
                .map(edge => edge.id);

            const isSpecialKey = event.shiftKey || event.ctrlKey;

            this.dispatch({ type: 'select', nodeIds, edgeIds, isSpecialKey });
        }
    }
}

const LEFT_BUTTON = 0;
const MIDDLE_BUTTON = 1;

/** Which buttons can be used for which actions. */
const actionButtons = {
    drag: {
        canvas: MIDDLE_BUTTON,
        node: LEFT_BUTTON,
    },
    select: {
        canvas: LEFT_BUTTON,
    },
};
