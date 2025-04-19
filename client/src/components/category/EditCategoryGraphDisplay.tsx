import { type ReactNode, type MouseEvent, useCallback } from 'react';
import { cn } from '../utils';
import { type GraphEvent, type GraphOptions } from '../graph/graphEngine';
import { GraphProvider } from '../graph/GraphProvider';
import { useCanvas, useEdge, useNode, useSelectionBox } from '../graph/graphHooks';
import { type EditCategoryDispatch, type EditCategoryState } from './editCategoryReducer';
import { type CategoryEdge, type CategoryNode } from './categoryGraph';
import { EDGE_ARROW_LENGTH, getEdgeDegree } from '../graph/graphUtils';
import clsx from 'clsx';

/**
 * Props for the EditCategoryGraphDisplay component.
 *
 * @interface EditCategoryGraphDisplayProps
 */
type EditCategoryGraphDisplayProps = Readonly<{
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
    options?: GraphOptions;
    className?: string;
}>;

/**
 * Renders an interactive graph for editing a category.
 * Manages nodes, edges, and selection box rendering within a canvas.
 *
 * @param props - The component props.
 * @returns A React component rendering the category graph.
 */
export function EditCategoryGraphDisplay({ state, dispatch, options, className }: EditCategoryGraphDisplayProps) {
    // Memoize dispatch to prevent unnecessary re-renders
    const graphDispatch = useCallback((event: GraphEvent) => dispatch({ type: 'graph', event }), [ dispatch ]);

    return (
        <GraphProvider dispatch={graphDispatch} graph={state.graph} options={options}>
            <CanvasDisplay className={className}>
                {/* Render all nodes */}
                {state.graph.nodes.values().toArray().map(node => (
                    <NodeDisplay key={node.id} node={node} state={state} dispatch={dispatch} />
                ))}

                {/* SVG layer for edges to ensure proper rendering */}
                <svg fill='none' xmlns='http://www.w3.org/2000/svg' className='w-full h-full pointer-events-none select-none'>
                    <defs>
                        <marker
                            id='arrow'
                            viewBox='0 0 12 12'
                            refX='0'
                            refY='6'
                            markerWidth={EDGE_ARROW_LENGTH}
                            markerHeight={EDGE_ARROW_LENGTH}
                            orient='auto-start-reverse'
                            markerUnits='userSpaceOnUse'
                        >
                            <path d='M0 1 11 6 0 11z' stroke='context-stroke' fill='context-stroke' pointerEvents='auto' />
                        </marker>
                    </defs>

                    {/* Render bundled edges with calculated degrees for curvature */}
                    {state.graph.edges.bundledEdges.flatMap((bundle: CategoryEdge[]) =>
                        bundle.map((edge: CategoryEdge, index: number) => (
                            <EdgeDisplay
                                key={edge.id}
                                edge={edge}
                                degree={getEdgeDegree(edge, index, bundle.length)}
                                state={state}
                                dispatch={dispatch}
                            />
                        )),
                    )}
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

/**
 * Renders the canvas for the graph, handling mouse interactions and styling.
 *
 * @param props - The component props.
 * @returns A React component rendering the graph canvas.
 */
function CanvasDisplay({ children, className }: CanvasDisplayProps) {
    const { setCanvasRef, onMouseDown, isDragging } = useCanvas();

    return (
        <div
            ref={setCanvasRef}
            className={cn(
                'relative bg-slate-400 overflow-hidden',
                isDragging ? 'cursor-grabbing' : 'cursor-default',
                className,
            )}
            onMouseDown={onMouseDown}
        >
            {children}
        </div>
    );
}

/**
 * Props for the NodeDisplay component, responsible for rendering
 * an individual category node within the graph editor.
 *
 * Includes the: node data, current editor state, and dispatch
 * function for performing updates.
 */
type NodeDisplayProps = Readonly<{
    node: CategoryNode;
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

/**
 * Renders a single node in the graph with interactive selection and drag behavior.
 */
function NodeDisplay({ node, state, dispatch }: NodeDisplayProps) {
    const { setNodeRef, onMouseDown, style, isHoverAllowed, isDragging } = useNode(node);
    const isSelected = state.selection.nodeIds.has(node.id);

    // Handle node selection with support for toggle (Ctrl key)
    function onClick(event: MouseEvent<HTMLElement>) {
        event.stopPropagation();
        const isSpecialKey = event.ctrlKey;
        dispatch({ type: 'select', nodeId: node.id, operation: isSpecialKey ? 'toggle' : 'set' });
    }

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={cn('absolute w-0 h-0 select-none z-10', isDragging && 'z-20')}
        >
            <div
                className={cn(
                    'absolute w-8 h-8 -left-4 -top-4 rounded-full border-2 border-slate-700 bg-white',
                    isHoverAllowed &&
                        'cursor-pointer hover:shadow-[0_0_20px_0_rgba(0,0,0,0.3)] hover:shadow-cyan-300 active:bg-cyan-300',
                    isDragging && 'pointer-events-none shadow-[3px_7px_10px_3px_rgba(0,0,0,0.5)]',
                    isSelected && 'bg-cyan-200',
                )}
                onClick={onClick}
                onMouseDown={onMouseDown}
            />

            {/* Node label with truncation for long text */}
            <div className='w-fit h-0'>
                <span className='relative -left-1/2 -top-10 font-medium pointer-events-none whitespace-nowrap inline-block truncate max-w-[150px]'>
                    {node.metadata.label}
                </span>
            </div>
        </div>
    );
}

/**
 * Props for rendering an interactive edge in the graph editor.
 *
 * @interface EdgeDisplayProps
 * @property edge - The edge data, including schema and metadata.
 * @property degree - The curvature factor for bundled edges to avoid overlap.
 * @property state - The current state of the category graph for selection and rendering.
 * @property dispatch - The dispatch function to handle edge interactions like selection.
 */
type EdgeDisplayProps = Readonly<{
    edge: CategoryEdge;
    degree: number;
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

/**
 * Renders a single edge in the graph with interactive selection and curvature.
 *
 * @param props - The component props.
 * @returns A React component rendering a graph edge.
 */
function EdgeDisplay({ edge, degree, state, dispatch }: EdgeDisplayProps) {
    const { setEdgeRef, svg, isHoverAllowed } = useEdge(edge, degree, state.graph);
    const isSelected = state.selection.edgeIds.has(edge.id);

    // Handle edge selection with support for toggle (Ctrl key)
    function onClick(event: MouseEvent<SVGElement>) {
        event.stopPropagation();
        const isSpecialKey = event.ctrlKey;
        dispatch({ type: 'select', edgeId: edge.id, operation: isSpecialKey ? 'toggle' : 'set' });
    }

    return (<>
        <path
            ref={setEdgeRef.path}
            onClick={onClick}
            d={svg.path}
            stroke={isSelected ? 'rgb(8, 145, 178)' : 'rgb(71, 85, 105)'}
            strokeWidth='4'
            className={cn(isHoverAllowed && 'cursor-pointer pointer-events-auto path-shadow')}
            markerEnd='url(#arrow)'
        />

        <text
            ref={setEdgeRef.label}
            transform={svg.label?.transform}
            className={clsx('font-medium', !svg.label && 'hidden')}
            fill='currentColor'
            textAnchor='middle'
        >
            {edge.label}
        </text>
    </>);
}

/** Renders a selection box for multi-select interactions in the graph. */
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
