import { type ReactNode, type MouseEvent, useCallback } from 'react';
import { type GraphEvent, type GraphOptions } from '../graph/graphEngine';
import { GraphProvider } from '../graph/GraphProvider';
import { useCanvas, useEdge, useNode, useSelectionBox } from '../graph/graphHooks';
import { type EditCategoryDispatch, type EditCategoryState, LeftPanelMode } from './editCategoryReducer';
import { type CategoryEdge, type CategoryNode } from './categoryGraph';
import { EDGE_ARROW_LENGTH, getEdgeDegree } from '../graph/graphUtils';
import { usePreferences } from '../PreferencesProvider';
import { twJoin, twMerge } from 'tailwind-merge';

type EditCategoryGraphDisplayProps = {
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
    options?: GraphOptions;
    className?: string;
};

/**
 * Renders an interactive graph for editing a category.
 * Manages nodes, edges, and selection box rendering within a canvas.
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
                    {state.graph.edges.bundledEdges.flatMap(bundle => bundle.map((edge, index) => (
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

type CanvasDisplayProps = {
    children: ReactNode;
    className?: string;
};

/**
 * Renders the canvas for the graph, handling mouse interactions and styling.
 */
function CanvasDisplay({ children, className }: CanvasDisplayProps) {
    const { setCanvasRef, onMouseDown, isDragging } = useCanvas();

    return (
        <div
            ref={setCanvasRef}
            className={twMerge('relative bg-canvas overflow-hidden',
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
type NodeDisplayProps = {
    node: CategoryNode;
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
};

/**
 * Renders a single node in the graph with interactive selection and drag behavior.
 */
function NodeDisplay({ node, state, dispatch }: NodeDisplayProps) {
    const { setNodeRef, onMouseDown, style, isHoverAllowed, isDragging } = useNode(node);
    const isSelected = state.selection.nodeIds.has(node.id);
    const { theme } = usePreferences().preferences;

    function onClick(event: MouseEvent<HTMLElement>) {
        event.stopPropagation();
        if (state.leftPanelMode === LeftPanelMode.createMorphism) {
            // Toggle selection without Ctrl in createMorphism mode
            dispatch({
                type: 'select',
                nodeId: node.id,
                operation: 'toggle',
            });
        }
        else {
            // Use Ctrl for toggle in other modes
            const isSpecialKey = event.ctrlKey;
            dispatch({
                type: 'select',
                nodeId: node.id,
                operation: isSpecialKey ? 'toggle' : 'set',
            });
        }
    }

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={twJoin('absolute w-0 h-0 select-none', isDragging ? 'z-20' : 'z-10')}
        >
            <div
                className={twMerge('absolute w-8 h-8 -left-4 -top-4 rounded-full border-2 border-default-600 bg-background',
                    isHoverAllowed && 'cursor-pointer hover:shadow-md hover:shadow-primary-200/50 hover:scale-110 active:bg-primary-200 active:border-primary-400',
                    isDragging && 'pointer-events-none shadow-primary-300/50 scale-110',
                    isSelected && 'bg-primary-200 border-primary-500',
                    theme === 'dark' && !isSelected && 'bg-default-50 border-default-900',
                    theme === 'dark' && isSelected && 'bg-primary-400 border-primary-600',
                    theme === 'dark' && isHoverAllowed && 'active:bg-primary-500 active:border-default-900',
                )}
                onClick={onClick}
                onMouseDown={onMouseDown}
            />

            {/* Node label with truncation for long text */}
            <div className='w-fit h-0'>
                <span className='relative -left-1/2 -top-10 font-medium pointer-events-none whitespace-nowrap inline-block truncate max-w-[150px] text-default-700'>
                    {node.metadata.label}
                </span>
            </div>
        </div>
    );
}

type EdgeDisplayProps = {
    /** The edge data, including schema and metadata. */
    edge: CategoryEdge;
    /** The curvature factor for bundled edges to avoid overlap. */
    degree: number;
    /** The current state of the category graph for selection and rendering. */
    state: EditCategoryState;
    /** The dispatch function to handle edge interactions like selection. */
    dispatch: EditCategoryDispatch;
};

/**
 * Renders a single edge in the graph with interactive selection and curvature.
 */
function EdgeDisplay({ edge, degree, state, dispatch }: EdgeDisplayProps) {
    const { setEdgeRef, svg, isHoverAllowed } = useEdge(edge, degree, state.graph);
    const isSelected = state.selection.edgeIds.has(edge.id);

    // Handle edge selection with support for toggle (Ctrl key)
    function onClick(event: MouseEvent<SVGElement>) {
        event.stopPropagation();
        const isSpecialKey = event.ctrlKey;
        dispatch({
            type: 'select',
            edgeId: edge.id,
            operation: isSpecialKey ? 'toggle' : 'set',
        });
    }

    return (<>
        <path
            ref={setEdgeRef.path}
            onClick={onClick}
            d={svg.path}
            stroke={isSelected ? 'hsl(var(--heroui-primary))' : 'hsl(var(--heroui-default-500))'}
            strokeWidth='4'
            className={isHoverAllowed ? 'cursor-pointer pointer-events-auto hover:drop-shadow-[0_0_4px_rgba(0,176,255,0.5)]' : undefined}
            markerEnd='url(#arrow)'
        />

        <text
            ref={setEdgeRef.label}
            transform={svg.label?.transform}
            className={twJoin('font-medium', !svg.label && 'hidden')}
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
            className='absolute border-2 border-primary-500 border-dashed pointer-events-none bg-primary-100/20'
            style={style}
        />
    );
}
