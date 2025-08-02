import { type ReactNode, type MouseEvent, useCallback, useMemo } from 'react';
import { type GraphEvent, type GraphOptions } from '../graph/graphEngine';
import { GraphProvider } from '../graph/GraphProvider';
import { useCanvas, useEdge, useNode, useSelectionBox } from '../graph/graphHooks';
import { EditorPhase, type MappingEditorDispatch, type MappingEditorState } from './useMappingEditor';
import { type CategoryEdge, type CategoryNode } from '../category/categoryGraph';
import { EDGE_ARROW_LENGTH, getEdgeDegree } from '../graph/graphUtils';
import { computePathsFromObjex, computePathToNode, computePathWithEdge, PathCount, type PathGraph } from '@/types/schema/PathMarker';
import { FreeSelection, PathSelection, SequenceSelection } from '../graph/graphSelection';
import { usePreferences } from '../PreferencesProvider';
import { twJoin, twMerge } from 'tailwind-merge';

type MapppingEditorGraphProps = {
    /** The current state of the mapping editor. */
    state: MappingEditorState;
    /** Dispatch function for updating the editor state. */
    dispatch: MappingEditorDispatch;
    /** Optional graph rendering options. */
    options?: GraphOptions;
    /** Optional CSS class for styling the canvas. */
    className?: string;
};

/**
 * Renders a graph-based UI for editing mappings, including nodes, edges, and selection.
 */
export function MappingEditorGraph({ state, dispatch, options, className }: MapppingEditorGraphProps) {
    // Memoize graph dispatch to forward graph events to the reducer
    const graphDispatch = useCallback((event: GraphEvent) => dispatch({ type: 'graph', event }), [ dispatch ]);

    const { selection } = state;

    // Compute path graph for path-based selections
    const pathGraph = useMemo(() => {
        if (!(selection instanceof PathSelection) || selection.isEmpty)
            return undefined;

        // TODO This ...
        const sourceObjex = state.category.getObjex(state.graph.nodes.get(selection.lastNodeId)!.schema.key);
        return computePathsFromObjex(sourceObjex);
    }, [ selection ]);

    return (
        <GraphProvider dispatch={graphDispatch} graph={state.graph} options={options}>
            <CanvasDisplay className={className}>
                {state.graph.nodes.values().toArray().map(node => (
                    <NodeDisplay key={node.id} node={node} state={state} dispatch={dispatch} pathGraph={pathGraph} />
                ))}

                {/* SVG layer for rendering edges with arrow markers */}
                <svg fill='none' xmlns='http://www.w3.org/2000/svg' className='w-full h-full pointer-events-none'>
                    <defs>
                        {/* Define arrow marker for edge ends */}
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

                    {/* Render edges with arrow markers */}
                    {state.graph.edges.bundledEdges.flatMap(bundle => bundle.map((edge, index) => (
                        <EdgeDisplay
                            key={edge.id}
                            edge={edge}
                            degree={getEdgeDegree(edge, index, bundle.length)}
                            state={state}
                            dispatch={dispatch}
                            pathGraph={pathGraph}
                        />
                    )))}
                </svg>

                <SelectionBox />
            </CanvasDisplay>
        </GraphProvider>
    );
}

type CanvasDisplayProps = {
    /** The content to render inside the canvas. */
    children: ReactNode;
    className?: string;
};

/**
 * Renders the graph canvas with drag-and-drop support.
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

type NodeDisplayProps = {
    /** The node to render. */
    node: CategoryNode;
    /** The current editor state. */
    state: MappingEditorState;
    /** Dispatch function for updating the editor state. */
    dispatch: MappingEditorDispatch;
    /** Optional path graph for path-based selections. */
    pathGraph: PathGraph | undefined;
};

/**
 * Renders a single node with selection and drag behavior.
 */
function NodeDisplay({ node, state, dispatch, pathGraph }: NodeDisplayProps) {
    const { setNodeRef, onMouseDown, style, isHoverAllowed, isDragging } = useNode(node);

    const { selection } = state;
    const isSelected = isNodeSelected(state, node);
    const isSelectionAllowed = isNodeSelectionAllowed(state, node, pathGraph);

    const pathNode = pathGraph?.nodes.get(node.id);
    const isRoot = node.id === state.form.rootObjexKey?.toString();

    const { theme } = usePreferences().preferences;

    function onClick(event: MouseEvent<HTMLElement>) {
        event.stopPropagation();
        if (!isSelectionAllowed)
            return;

        if (selection instanceof FreeSelection) {
            const isSpecialKey = event.ctrlKey || event.ctrlKey;
            dispatch({ type: 'select', nodeId: node.id, operation: isSpecialKey ? 'toggle' : 'set' });
            return;
        }

        if (selection instanceof SequenceSelection) {
            dispatch({ type: 'sequence', operation: 'toggle', nodeId: node.id });
            return;
        }

        if (!pathGraph) {
            // No path yet, we can start a new one.
            dispatch({ type: 'path', operation: 'start', nodeId: node.id });
            return;
        }

        if (pathNode!.id === selection.lastNodeId) {
            // The last node is clicked, we can remove it.
            dispatch({ type: 'path', operation: 'remove' });
            return;
        }

        const { nodeIds, edgeIds } = computePathToNode(pathNode!);
        dispatch({ type: 'path', operation: 'add', nodeIds, edgeIds });
    }

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={twJoin('absolute w-0 h-0 select-none', isDragging ? 'z-20' : 'z-10')}
        >
            <div
                className={twMerge(
                    'absolute size-8 -left-4 -top-4 rounded-full border-2',
                    // Root node styling.
                    isRoot && 'bg-success border-success-700',
                    // Normal styling only applied if not root.
                    !isRoot && [
                        'border-default-600 bg-background',
                        isHoverAllowed && isSelectionAllowed && 'cursor-pointer hover:shadow-md hover:shadow-primary-200/50 hover:scale-110 active:bg-primary-200 active:border-primary-400',
                        isDragging && 'pointer-events-none shadow-primary-300/50 scale-110',
                        isSelected && 'bg-primary-200 border-primary-500',
                        theme === 'dark' && [
                            !isSelected && 'bg-default-200 border-default-900',
                            isSelected && 'bg-primary-400 border-primary-600',
                            isHoverAllowed && isSelectionAllowed && 'active:bg-primary-500 active:border-default-900',
                        ],
                        pathNode && pathClasses[pathNode.pathCount],
                    ],
                )}
                onClick={onClick}
                onMouseDown={onMouseDown}
            />

            <div className='w-fit h-0'>
                <span className={twMerge('relative -left-1/2 -top-10 font-medium pointer-events-none whitespace-nowrap inline-block truncate max-w-[150px]',
                    isRoot && 'text-success-600 font-bold',
                )}>
                    {isRoot && 'root:'} {node.metadata.label}
                </span>
            </div>
        </div>
    );
}

/**
 * Checks if a node is selected based on the editor state.
 */
function isNodeSelected({ selection }: MappingEditorState, node: CategoryNode): boolean {
    if (selection instanceof FreeSelection)
        return selection.nodeIds.has(node.id);
    if (selection instanceof SequenceSelection)
        return selection.has(node.id);

    return selection.lastNodeId === node.id;
}

/**
 * Determines if a node can be selected based on the editor state and path graph.
 */
function isNodeSelectionAllowed({ selection, editorPhase }: MappingEditorState, node: CategoryNode, pathGraph: PathGraph | undefined): boolean {
    if (selection instanceof FreeSelection && editorPhase === EditorPhase.SelectRoot)
        return true;

    if (selection instanceof PathSelection && editorPhase === EditorPhase.BuildPath) {
        if (!pathGraph)
            return true;
        const pathNode = pathGraph.nodes.get(node.id);
        if (!pathNode)
            return false;
        return pathNode.pathCount === PathCount.One || pathNode.id === selection.lastNodeId;
    }

    return false;
}

/**
 * CSS classes for path indicators on nodes.
 */
const pathClasses: Record<PathCount, string | undefined> = {
    [PathCount.None]: undefined,
    [PathCount.One]: 'shadow-[0_0_20px_0_rgba(0,0,0,0.3)] shadow-green-400',
    [PathCount.Many]: 'shadow-[0_0_20px_0_rgba(0,0,0,0.3)] shadow-red-400',
};

type EdgeDisplayProps = {
    /** The edge to render. */
    edge: CategoryEdge;
    /** The degree offset for bundled edges. */
    degree: number;
    /** The current editor state. */
    state: MappingEditorState;
    /** Dispatch function for updating the editor state. */
    dispatch: MappingEditorDispatch;
    /** Optional path graph for path-based selections. */
    pathGraph: PathGraph | undefined;
};

/**
 * Renders a single edge with selection and path-based styling.
 */
function EdgeDisplay({ edge, degree, state, dispatch, pathGraph }: EdgeDisplayProps) {
    const { setEdgeRef, svg, isHoverAllowed } = useEdge(edge, degree, state.graph);

    const isSelected = isEdgeSelected(state, edge);
    const isSelectionAllowed = isEdgeSelectionAllowed(state, edge, pathGraph);

    const pathEdge = pathGraph?.edges.get(edge.id);

    function onClick(event: MouseEvent<SVGElement>) {
        event.stopPropagation();
        if (!isSelectionAllowed)
            return;

        if (state.selection instanceof FreeSelection) {
            const isSpecialKey = event.ctrlKey || event.ctrlKey;
            dispatch({ type: 'select', edgeId: edge.id, operation: isSpecialKey ? 'toggle' : 'set' });
            return;
        }

        const { nodeIds, edgeIds } = computePathWithEdge(pathEdge!, pathGraph!);
        dispatch({ type: 'path', operation: 'add', nodeIds, edgeIds });
    }

    return (<>
        <path
            ref={setEdgeRef.path}
            onClick={onClick}
            d={svg.path}
            stroke={isSelected ? 'hsl(var(--heroui-primary))' : 'hsl(var(--heroui-default-500))'}
            strokeWidth='4'
            className={twMerge('text-zinc-600',
                isSelectionAllowed && [
                    isHoverAllowed && 'cursor-pointer pointer-events-auto hover:drop-shadow-[0_0_4px_rgba(0,176,255,0.5)]',
                    pathEdge && 'path-shadow-green',
                ],
            )}
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

/**
 * Checks if an edge is selected based on the editor state.
 */
function isEdgeSelected(state: MappingEditorState, edge: CategoryEdge): boolean {
    if (state.selection instanceof PathSelection)
        return false;

    if (state.selection instanceof FreeSelection)
        return state.selection.edgeIds.has(edge.id);

    return false;
}

/**
 * Determines if an edge can be selected based on the editor state and path graph.
 */
function isEdgeSelectionAllowed({ selection }: MappingEditorState, edge: CategoryEdge, pathGraph: PathGraph | undefined): boolean {
    if (selection instanceof FreeSelection)
        return true;
    if (selection instanceof SequenceSelection)
        return false;

    if (!pathGraph)
        return false;

    const pathEdge = pathGraph.edges.get(edge.id);
    if (pathEdge?.traversableDirection === undefined)
        return false;

    const fromNode = pathGraph.nodes.get(pathEdge.traversableDirection ? edge.from : edge.to);
    if (fromNode?.pathCount !== PathCount.One)
        return false;

    // We only allow edges to the ambiguous nodes (so that the graph doesn't look like a coloring book).
    const toNode = pathGraph.nodes.get(pathEdge.traversableDirection ? edge.to : edge.from);
    if (toNode?.pathCount !== PathCount.Many)
        return false;

    return true;
}

/**
 * Renders a selection box for multi-node/edge selection.
 */
function SelectionBox() {
    const { setSelectionBoxRef, style } = useSelectionBox();

    return (
        <div
            ref={setSelectionBoxRef}
            className='absolute border-2 border-zinc-700 border-dotted pointer-events-none'
            style={style}
        />
    );
}
