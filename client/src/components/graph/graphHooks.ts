import { createContext, type MouseEvent, useCallback, useContext, useMemo, useRef } from 'react';
import { type ReactiveGraphState, type GraphEngine, type GraphValue } from './graphEngine';
import { computeEdgeStyle, computeNodeStyle, computeSelectionBoxStyle, type Node, type Edge } from './graphUtils';

type GraphContext = {
    engine: GraphEngine;
    state: ReactiveGraphState;
};

export const graphContext = createContext<GraphContext | undefined>(undefined);

function useGraphContext(): GraphContext {
    const context = useContext(graphContext);
    if (context === undefined)
        throw new Error('Graph context must be used within an GraphProvider');

    return context;
}

export function useCanvas() {
    const { state, engine } = useGraphContext();
    const ref = useRef<HTMLElement | null>(null);

    const setCanvasRef = useCallback((element: HTMLElement | null) => {
        if (ref.current !== element)
            engine.setCanvasRef(element);

        ref.current = element;
    }, [ engine ]);

    const onMouseDown = useCallback((e: MouseEvent<HTMLElement>) => engine.handleCanvasMousedown(e), [ engine ]);

    const isDragging = state.drag?.type === 'canvas';

    return {
        setCanvasRef,
        onMouseDown,
        isDragging,
    };
}

export function useNode(node: Node) {
    const { state, engine } = useGraphContext();
    const ref = useRef<HTMLElement | null>(null);

    const nodeId = node.id;
    const setNodeRef = useCallback((element: HTMLElement | null) => {
        if (ref.current !== element)
            engine.setNodeRef(nodeId, element);

        ref.current = element;
    }, [ engine, nodeId ]);

    const onMouseDown = useCallback((e: MouseEvent<HTMLElement>) => engine.handleNodeMousedown(e, nodeId), [ engine, nodeId ]);

    const isDragging = state.drag?.type === 'node' && state.drag.nodeId === node.id;
    // We want to highlight the node when it's being dragged or hovered, but not when other dragged node is over it.
    // Also, no selection is allowed when dragging.
    const isHoverAllowed = (!state.drag || isDragging) && !state.select;

    // Currently not working because the selection box state isn't being propagated. Maybe we add this later.
    // const isInSelectBox = state.select && isPointInBox(node.position, state.select);

    return {
        setNodeRef,
        onMouseDown,
        style: computeNodeStyle(node, state.coordinates),
        isDragging,
        isHoverAllowed,
    };
}

export function useEdge(edge: Edge, graph: GraphValue) {
    const { state, engine } = useGraphContext();
    const ref = useRef<HTMLElement | null>(null);

    const edgeId = edge.id;
    const setEdgeRef = useCallback((element: HTMLElement | null) => {
        if (ref.current !== element)
            engine.setEdgeRef(edgeId, element);

        ref.current = element;
    }, [ engine, edgeId ]);

    const nodes = graph.nodes;
    const cache = useMemo(() => ({
        from: nodes.find(node => node.id === edge.from)!,
        to: nodes.find(node => node.id === edge.to)!,
    }), [ edge, nodes ]);

    return {
        setEdgeRef,
        style: computeEdgeStyle(cache.from, cache.to, state.coordinates),
    };
}

export function useSelectionBox() {
    const { state, engine } = useGraphContext();
    const ref = useRef<HTMLElement | null>(null);

    const setSelectionBoxRef = useCallback((element: HTMLElement | null) => {
        if (ref.current !== element)
            engine.setSelectionBoxRef(element);

        ref.current = element;
    }, [ engine ]);

    return {
        setSelectionBoxRef,
        style: computeSelectionBoxStyle(state.select, state.coordinates),
    };
}
