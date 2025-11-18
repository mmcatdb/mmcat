import { createContext, type MouseEvent, useCallback, useContext, useMemo, useRef } from 'react';
import { type ReactiveGraphState, type GraphEngine } from './graphEngine';
import { computeEdgeSvg, computeNodeStyle, computeSelectionBoxStyle, type Node, type Edge } from './graphUtils';

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

    // Show the grabbing cursor when dragging a canvas *or a node*.
    // When a node is dragged, it has turned off all pointer events. So we need to show the grabbing cursor on the canvas.
    const isDragging = !!state.drag;

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
        if (ref.current === element)
            return;

        ref.current = element;
        engine.setNodeRef(nodeId, element);
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

export function useEdge(edge: Edge, degree: number) {
    const { state, engine } = useGraphContext();
    const pathRef = useRef<SVGPathElement | null>(null);
    const labelRef = useRef<SVGTextElement | null>(null);

    const edgeId = edge.id;
    const setEdgeRef = useMemo(() => ({
        path: (element: SVGPathElement | null) => {
            if (pathRef.current === element)
                return;

            pathRef.current = element;
            engine.setEdgeRef(edgeId, computeEdgeRef(element, labelRef.current));
        },
        label: (element: SVGTextElement | null) => {
            if (labelRef.current === element)
                return;

            labelRef.current = element;
            engine.setEdgeRef(edgeId, computeEdgeRef(pathRef.current, element));
        },
    }), [ engine, edgeId ]);

    const { from, to } = engine.getEdgeNodes(edge);
    const isHoverAllowed = !state.drag && !state.select;

    return {
        setEdgeRef,
        svg: computeEdgeSvg(from, to, edge.label, degree, state.coordinates),
        isHoverAllowed,
    };
}

function computeEdgeRef(path: SVGPathElement | null, label: SVGTextElement | null) {
    if (!path && !label)
        return null;

    return {
        path: path ?? undefined,
        label: label ?? undefined,
    };
}

export function useSelectionBox() {
    const { state, engine } = useGraphContext();
    const ref = useRef<HTMLElement | null>(null);

    const setSelectionBoxRef = useCallback((element: HTMLElement | null) => {
        if (ref.current === element)
            return;

        ref.current = element;
        engine.setSelectionBoxRef(element);
    }, [ engine ]);

    return {
        setSelectionBoxRef,
        style: computeSelectionBoxStyle(state.select, state.coordinates),
    };
}
