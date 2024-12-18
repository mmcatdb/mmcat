import { type Dispatch, type SetStateAction, useMemo } from 'react';
import { cn } from './utils';
import { type Edge, type GraphEngine, type GraphOptions, type GraphValue, isPointInBox, type Node, positionToOffset, type ReactiveGraphState, useGraphEngine } from './useGraphEngine';

type GraphDisplayProps = Readonly<{
    /** The public value that the graph exposes. Its updates are reflected in the graph display. */
    value: GraphValue;
    /** The setter for the public value. Propagates the changes to the parent component. */
    setValue: Dispatch<SetStateAction<GraphValue>>;
    /** User preferences. The graph engine is restarted whenever they change, so make sure they are constant or at least memoized! */
    options?: GraphOptions;
    className?: string;
}>;

export function GraphDisplay({ value, setValue, options, className }: GraphDisplayProps) {
    const [ state, engine ] = useGraphEngine(value, setValue, options);

    return (
        <div
            className={cn('relative bg-slate-400 overflow-hidden', state.drag ? 'cursor-grabbing' : 'cursor-default', className)}
            ref={engine.canvasRef}
            onMouseDown={e => engine.handleCanvasMousedown(e)}
        >
            {value.nodes.map(node => (
                <NodeDisplay key={node.id} node={node} value={value} state={state} engine={engine} />
            ))}
            {value.edges.map(edge => (
                <EdgeDisplay key={edge.id} edge={edge} nodes={value.nodes} state={state} />
            ))}
            <SelectionBox state={state} />
        </div>
    );
}

type NodeDisplayProps = Readonly<{
    node: Node;
    value: GraphValue;
    state: ReactiveGraphState;
    engine: GraphEngine;
}>;

function NodeDisplay({ node, value, state, engine }: NodeDisplayProps) {
    const isDragging = !!state.drag && 'nodeId' in state.drag && state.drag.nodeId === node.id;
    // We want to highlight the node when it's being dragged or hovered, but not when other dragged node is over it.
    // Also, no selection is allowed when dragging.
    const isHightlightAllowed = (!state.drag || isDragging) && !state.select;
    const isInSelecBox = state.select && isPointInBox(node.position, state.select);
    const isSelected = !!value.selectedNodes[node.id];

    return (
        <div className='absolute w-0 h-0 select-none z-10' style={positionToOffset(node.position, state.coordinates)}>
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

/** In pixels */
const EDGE_OFFSET = 20;

type EdgeDisplayProps = Readonly<{
    edge: Edge;
    nodes: Node[];
    state: ReactiveGraphState;
}>;

function EdgeDisplay({ edge, nodes, state }: EdgeDisplayProps) {
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

    const start = positionToOffset(cache.from.position, state.coordinates);
    const end = positionToOffset(cache.to.position, state.coordinates);

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
