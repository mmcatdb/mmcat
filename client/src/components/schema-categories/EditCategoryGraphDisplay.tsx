import { type ReactNode, type MouseEvent, useMemo, useCallback } from 'react';
import { cn } from '../utils';
import { type GraphEvent, type GraphOptions } from '../graph/graphEngine';
import { GraphProvider } from '../graph/GraphProvider';
import { useCanvas, useEdge, useNode, useSelectionBox } from '../graph/graphHooks';
import { type EditCategoryDispatch, type EditCategoryState } from './editCategoryReducer';
import { type CategoryEdge, type CategoryNode } from './categoryGraph';
import { EdgeMap, getEdgeDegree } from '../graph/graphUtils';

type EditCategoryGraphDisplayProps = Readonly<{
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
    options?: GraphOptions;
    className?: string;
}>;

export function EditCategoryGraphDisplay({ state, dispatch, options, className }: EditCategoryGraphDisplayProps) {
    const bundledEdges = useMemo(() => EdgeMap.bundleEdges([ ...(new EdgeMap(state.graph.edges)).values() ]), [ state.graph.edges ]);

    const graphDispatch = useCallback((event: GraphEvent) => dispatch({ type: 'graph', event }), [ dispatch ]);

    return (
        <GraphProvider dispatch={graphDispatch} graph={state.graph} options={options}>
            <CanvasDisplay className={className}>
                {state.graph.nodes.map(node => (
                    <NodeDisplay key={node.id} node={node} state={state} dispatch={dispatch} />
                ))}

                <svg fill='none' xmlns='http://www.w3.org/2000/svg' className='absolute w-full h-full pointer-events-none'>
                    {bundledEdges.flatMap(bundle => bundle.map((edge, index) => (
                        <EdgeDisplay key={edge.id} edge={edge} degree={getEdgeDegree(edge, index, bundle.length)} state={state} dispatch={dispatch} />
                    )))}
                </svg>

                <SelectionBox />
            </CanvasDisplay>
        </GraphProvider>
    );
}

type CanvasDisplayProps = Readonly<{
    children: ReactNode;
    className?: string;
}>;

function CanvasDisplay({ children, className }: CanvasDisplayProps) {
    const { setCanvasRef, onMouseDown, isDragging } = useCanvas();

    return (
        <div
            ref={setCanvasRef}
            className={cn('relative bg-slate-400 overflow-hidden', isDragging ? 'cursor-grabbing' : 'cursor-default', className)}
            onMouseDown={onMouseDown}
        >
            {children}
        </div>
    );
}

type NodeDisplayProps = Readonly<{
    node: CategoryNode;
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

function NodeDisplay({ node, state, dispatch }: NodeDisplayProps) {
    const { setNodeRef, onMouseDown, style, isHoverAllowed, isDragging } = useNode(node);

    const isSelected = state.selection.nodeIds.has(node.id);

    function onClick(event: MouseEvent<HTMLElement>) {
        event.stopPropagation();
        const isSpecialKey = event.ctrlKey || event.ctrlKey;
        dispatch({ type: 'select', nodeId: node.id, operation: isSpecialKey ? 'toggle' : 'set' });
    }

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={cn('absolute w-0 h-0 select-none z-10', isDragging && 'z-20')}
        >
            <div
                className={cn('absolute w-8 h-8 -left-4 -top-4 rounded-full border-2 border-slate-700 bg-white active:bg-cyan-300',
                    isHoverAllowed && 'cursor-pointer hover:shadow-[0_0_20px_0_rgba(0,0,0,0.3)] hover:shadow-cyan-300',
                    isDragging && 'pointer-events-none shadow-[3px_7px_10px_3px_rgba(0,0,0,0.5)]',
                    // isInSelectBox && 'shadow-[0_0_20px_0_rgba(0,0,0,0.3)] shadow-cyan-300',
                    isSelected && 'bg-cyan-200',
                )}
                onClick={onClick}
                onMouseDown={onMouseDown}
            />

            <div className='w-fit h-0'>
                <span className='relative -left-1/2 -top-10 font-medium pointer-events-none'>
                    {node.metadata.label}
                </span>
            </div>
        </div>
    );
}

type EdgeDisplayProps = Readonly<{
    edge: CategoryEdge;
    degree: number;
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

function EdgeDisplay({ edge, degree, state, dispatch }: EdgeDisplayProps) {
    const { setEdgeRef, path, isHoverAllowed } = useEdge(edge, degree, state.graph);

    const isSelected = state.selection.edgeIds.has(edge.id);

    function onClick(event: MouseEvent<SVGElement>) {
        event.stopPropagation();
        const isSpecialKey = event.ctrlKey || event.ctrlKey;
        dispatch({ type: 'select', edgeId: edge.id, operation: isSpecialKey ? 'toggle' : 'set' });
    }

    return (
        <path
            ref={setEdgeRef}
            onClick={onClick}
            d={path}
            stroke='currentColor'
            strokeWidth='4'
            strokeLinejoin='round'
            className={cn('text-slate-600',
                isHoverAllowed && 'cursor-pointer hover:shadow-[0_0_20px_0_rgba(0,0,0,0.3)] hover:shadow-cyan-300 pointer-events-auto path-shadow',
                isSelected && 'text-cyan-600',
            )}
        />
    );
}

function SelectionBox() {
    const { setSelectionBoxRef, style } = useSelectionBox();

    return (
        <div
            ref={setSelectionBoxRef}
            className='absolute border-2 border-slate-700 border-dotted pointer-events-none'
            style={style}
        />
    );
}
