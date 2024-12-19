import { type Dispatch, useCallback, useMemo, useRef } from 'react';
import { cn } from './utils';
import { computeEdgeStyle, type Edge, type GraphAction, type GraphEngine, type GraphOptions, type GraphValue, isPointInBox, type Node, positionToOffset, type ReactiveGraphState, useGraphEngine } from './useGraphEngine';

type GraphDisplayProps = Readonly<{
    dispatch: Dispatch<GraphAction>;
    /** The public graph that the graph exposes. Its updates are reflected in the graph display. */
    graph: GraphValue;
    /** User preferences. The graph engine is restarted whenever they change, so make sure they are constant or at least memoized! */
    options?: GraphOptions;
    className?: string;
}>;

export function GraphDisplay({ dispatch, graph, options, className }: GraphDisplayProps) {
    const [ state, engine ] = useGraphEngine(dispatch, graph, options);

    return (
        <div
            className={cn('relative bg-slate-400 overflow-hidden', state.drag ? 'cursor-grabbing' : 'cursor-default', className)}
            ref={engine.canvasRef}
            onMouseDown={e => engine.handleCanvasMousedown(e)}
        >
            {graph.nodes.map(node => (
                <NodeDisplay key={node.id} node={node} graph={graph} state={state} engine={engine} />
            ))}
            {graph.edges.map(edge => (
                <EdgeDisplay key={edge.id} edge={edge} nodes={graph.nodes} state={state} engine={engine} />
            ))}
            <SelectionBox state={state} />
        </div>
    );
}

type NodeDisplayProps = Readonly<{
    node: Node;
    graph: GraphValue;
    state: ReactiveGraphState;
    engine: GraphEngine;
}>;

function NodeDisplay({ node, graph, state, engine }: NodeDisplayProps) {
    const ref = useNodeRef(engine, node.id);

    const isDragging = !!state.drag && 'nodeId' in state.drag && state.drag.nodeId === node.id;
    // We want to highlight the node when it's being dragged or hovered, but not when other dragged node is over it.
    // Also, no selection is allowed when dragging.
    const isHightlightAllowed = (!state.drag || isDragging) && !state.select;
    const isInSelecBox = state.select && isPointInBox(node.position, state.select);
    const isSelected = !!graph.selectedNodes[node.id];

    return (
        <div ref={ref} className='absolute w-0 h-0 select-none z-10' style={positionToOffset(node.position, state.coordinates)}>
            <div
                className={cn('absolute w-8 h-8 -left-4 -top-4 rounded-full border-2 border-slate-700 bg-white active:bg-cyan-300',
                    isHightlightAllowed && 'hover:shadow-[0_0_20px_0_rgba(0,0,0,0.3)] hover:shadow-cyan-300',
                    isDragging ? 'cursor-grabbing pointer-events-none' : 'cursor-pointer',
                    isInSelecBox && 'shadow-[0_0_20px_0_rgba(0,0,0,0.3)] shadow-cyan-300',
                    isSelected && 'bg-cyan-200',
                )}
                onClick={e => engine.handleNodeClick(e, node.id)}
                onMouseDown={e => engine.handleNodeMousedown(e, node.id)}
            />
            <div className='w-fit'>
                <span className='relative -left-1/2 -top-10 font-medium'>
                    {node.label}
                </span>
            </div>
        </div>
    );
}

function useNodeRef(engine: GraphEngine, id: string) {
    const ref = useRef<HTMLElement | null>(null);

    const setNodeRef = useCallback((element: HTMLElement | null) => {
        if (ref.current !== element)
            engine.setNodeRef(id, element);

        ref.current = element;
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return setNodeRef;
}

type EdgeDisplayProps = Readonly<{
    edge: Edge;
    nodes: Node[];
    state: ReactiveGraphState;
    engine: GraphEngine;
}>;

function EdgeDisplay({ edge, nodes, state, engine }: EdgeDisplayProps) {
    const ref = useEdgeRef(engine, edge.id);

    const cache = useMemo(() => {
        const from = nodes.find(node => node.id === edge.from);
        const to = nodes.find(node => node.id === edge.to);

        return (from && to) ? { from, to } : undefined;
    }, [ edge, nodes ]);

    if (!cache) {
        // This might happen if the node was deleted.
        console.warn(`EdgeDisplay: Node not found in edge ${edge.id}.`);
        return null;
    }

    const { left, top, width, transform } = computeEdgeStyle(cache.from, cache.to, state.coordinates);

    return (
        <div ref={ref} className='absolute h-1 bg-slate-700 rounded-full select-none' style={{ left, top, width, transform }} />
    );
}

function useEdgeRef(engine: GraphEngine, id: string) {
    const ref = useRef<HTMLElement | null>(null);

    const setEdgeRef = useCallback((element: HTMLElement | null) => {
        if (ref.current !== element)
            engine.setEdgeRef(id, element);

        ref.current = element;
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return setEdgeRef;
}

type SelectionBoxProps = Readonly<{
    state: ReactiveGraphState;
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
