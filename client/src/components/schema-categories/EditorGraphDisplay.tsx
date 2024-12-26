import { type ReactNode, type MouseEvent } from 'react';
import { cn } from '../utils';
import { type GraphOptions } from '../graph/graphEngine';
import { GraphProvider } from '../graph/GraphProvider';
import { useCanvas, useEdge, useNode, useSelectionBox } from '../graph/graphHooks';
import { type EditCategoryDispatch, type EditCategoryState } from './editCategoryReducer';
import { type CategoryEdge, type CategoryNode } from './categoryGraph';

type EditorGraphDisplayProps = Readonly<{
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
    options?: GraphOptions;
    className?: string;
}>;

export function EditorGraphDisplay({ state, dispatch, options, className }: EditorGraphDisplayProps) {
    return (
        <GraphProvider dispatch={dispatch} graph={state.graph} options={options}>
            <CanvasDisplay className={className}>
                {state.graph.nodes.map(node => (
                    <NodeDisplay key={node.id} node={node} state={state} dispatch={dispatch} />
                ))}
                {state.graph.edges.map(edge => (
                    <EdgeDisplay key={edge.id} edge={edge} state={state} dispatch={dispatch} />
                ))}
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
            className={cn('relative bg-slate-400 overflow-hidden focus:bg-red-300 focus-visible:bg-green-300', isDragging ? 'cursor-grabbing' : 'cursor-default', className)}
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

    const isSelected = state.selectedNodeIds.has(node.id);

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
                <span className='relative -left-1/2 -top-10 font-medium'>
                    {node.metadata.label}
                </span>
            </div>
        </div>
    );
}

type EdgeDisplayProps = Readonly<{
    edge: CategoryEdge;
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

function EdgeDisplay({ edge, state, dispatch }: EdgeDisplayProps) {
    const { setEdgeRef, style, isHoverAllowed } = useEdge(edge, state.graph);

    const isSelected = state.selectedEdgeIds.has(edge.id);

    function onClick(event: MouseEvent<HTMLElement>) {
        event.stopPropagation();
        const isSpecialKey = event.ctrlKey || event.ctrlKey;
        dispatch({ type: 'select', edgeId: edge.id, operation: isSpecialKey ? 'toggle' : 'set' });
    }

    // FIXME display multiple edges ...

    return (
        <div
            ref={setEdgeRef}
            className={cn('absolute h-1 bg-slate-700 rounded-full select-none',
                isHoverAllowed && 'cursor-pointer hover:shadow-[0_0_20px_0_rgba(0,0,0,0.3)] hover:shadow-cyan-300',
                isSelected && 'bg-cyan-600',
            )}
            style={style}
            onClick={onClick}
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
