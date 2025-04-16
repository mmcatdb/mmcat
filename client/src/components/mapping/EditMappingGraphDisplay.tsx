import { type ReactNode, type MouseEvent, useCallback, useMemo } from 'react';
import { cn } from '../utils';
import { type GraphEvent, type GraphOptions } from '../graph/graphEngine';
import { GraphProvider } from '../graph/GraphProvider';
import { useCanvas, useEdge, useNode, useSelectionBox } from '../graph/graphHooks';
import { EditorPhase, type EditMappingDispatch, type EditMappingState } from './editMappingReducer';
import { type CategoryEdge, type CategoryNode } from '../category/categoryGraph';
import { getEdgeDegree } from '../graph/graphUtils';
import { computePathsFromObjex, computePathToNode, computePathWithEdge, PathCount, type PathGraph } from '@/types/schema/PathMarker';
import { FreeSelection, PathSelection, SelectionType, SequenceSelection } from '../graph/graphSelection';

/**
 * Props for the EditMappingGraphDisplay component.
 *
 * @property state - The current state of the mapping editor.
 * @property dispatch - Dispatch function for updating the editor state.
 * @property options - Optional graph rendering options.
 * @property className - Optional CSS class for styling the canvas.
 */
type EditMappingGraphDisplayProps = Readonly<{
    state: EditMappingState;
    dispatch: EditMappingDispatch;
    options?: GraphOptions;
    className?: string;
}>;

/**
 * Renders a graph-based UI for editing mappings, including nodes, edges, and selection.
 */
export function EditMappingGraphDisplay({ state, dispatch, options, className }: EditMappingGraphDisplayProps) {
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
                <svg fill='none' xmlns='http://www.w3.org/2000/svg' className='absolute w-full h-full pointer-events-none'>
                    <defs>
                        {/* Define arrow marker for edge ends */}
                        <marker id='arrow' viewBox='0 0 12 12' refX='8' refY='5' markerWidth='6' markerHeight='6' orient='auto-start-reverse'>
                            <path d='M 0 1 L 10 5 L 0 9 z' stroke='context-stroke' fill='context-stroke' pointerEvents='auto' />
                        </marker>
                    </defs>

                    {/* Render edges with arrow markers */}
                    {state.graph.edges.bundledEdges.flatMap(bundle => bundle.map((edge, index) => (
                        <EdgeDisplay key={edge.id} edge={edge} degree={getEdgeDegree(edge, index, bundle.length)} state={state} dispatch={dispatch} pathGraph={pathGraph} />
                    )))}
                </svg>

                <SelectionBox />
            </CanvasDisplay>
        </GraphProvider>
    );
}

/**
 * Props for the CanvasDisplay component.
 *
 * @interface CanvasDisplayProps
 * @property children - Content to render inside the canvas.
 * @property className - Optional CSS class for styling.
 */
type CanvasDisplayProps = Readonly<{
    children: ReactNode;
    className?: string;
}>;

/**
 * Renders the graph canvas with drag-and-drop support.
 */
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

/**
 * Props for the NodeDisplay component.
 *
 * @interface NodeDisplayProps
 * @property node - The node to render.
 * @property state - The current editor state.
 * @property dispatch - Dispatch function for state updates.
 * @property pathGraph - Optional path graph for path-based selections.
 */
type NodeDisplayProps = Readonly<{
    node: CategoryNode;
    state: EditMappingState;
    dispatch: EditMappingDispatch;
    pathGraph: PathGraph | undefined;
}>;

/**
 * Renders a single node with selection and drag behavior.
 */
function NodeDisplay({ node, state, dispatch, pathGraph }: NodeDisplayProps) {
    const { setNodeRef, onMouseDown, style, isHoverAllowed, isDragging } = useNode(node);

    const { selection } = state;
    const isSelected = isNodeSelected(state, node);
    const isSelectionAllowed = isNodeSelectionAllowed(state, node, pathGraph);

    const pathNode = pathGraph?.nodes.get(node.id);

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
            className={cn('absolute w-0 h-0 select-none z-10', isDragging && 'z-20')}
        >
            <div
                className={cn('absolute w-8 h-8 -left-4 -top-4 rounded-full border-2 border-slate-700 bg-white',
                    isHoverAllowed && isSelectionAllowed && 'cursor-pointer hover:shadow-[0_0_20px_0_rgba(0,0,0,0.3)] hover:shadow-cyan-300 active:bg-cyan-300',
                    isDragging && 'pointer-events-none shadow-[3px_7px_10px_3px_rgba(0,0,0,0.5)]',
                    isSelected && 'bg-cyan-200',
                    pathNode && pathClasses[pathNode.pathCount],
                )}
                onClick={onClick}
                onMouseDown={onMouseDown}
            />

            <div className='w-fit h-0'>
                <span className='relative -left-1/2 -top-10 font-medium pointer-events-none whitespace-nowrap inline-block truncate max-w-[150px]'>
                    {node.metadata.label}
                </span>
            </div>
        </div>
    );
}

/**
 * Checks if a node is selected based on the editor state.
 */
function isNodeSelected({ selection, selectionType }: EditMappingState, node: CategoryNode): boolean {
    if (selectionType === SelectionType.None)
        return false;

    if (selection instanceof FreeSelection)
        return selection.nodeIds.has(node.id);
    if (selection instanceof SequenceSelection)
        return selection.has(node.id);

    return selection.lastNodeId === node.id;
}

/**
 * Determines if a node can be selected based on the editor state and path graph.
 */
function isNodeSelectionAllowed({ selection, selectionType, editorPhase }: EditMappingState, node: CategoryNode, pathGraph: PathGraph | undefined): boolean {
    if (selectionType === SelectionType.None)
        return false;

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

/**
 * Props for the EdgeDisplay component.
 *
 * @interface EdgeDisplayProps
 * @property edge - The edge to render.
 * @property degree - The degree offset for bundled edges.
 * @property state - The current editor state.
 * @property dispatch - Dispatch function for state updates.
 * @property pathGraph - Optional path graph for path-based selections.
 */
type EdgeDisplayProps = Readonly<{
    edge: CategoryEdge;
    degree: number;
    state: EditMappingState;
    dispatch: EditMappingDispatch;
    pathGraph: PathGraph | undefined;
}>;

/**
 * Renders a single edge with selection and path-based styling.
 */
function EdgeDisplay({ edge, degree, state, dispatch, pathGraph }: EdgeDisplayProps) {
    // Hook for edge rendering and interactions
    const { setEdgeRef, path, isHoverAllowed } = useEdge(edge, degree, state.graph);

    const isSelected = isEdgeSelected(state, edge);
    const isSelectionAllowed = isEdgeSelectionAllowed(state, edge, pathGraph);

    const pathEdge = pathGraph?.edges.get(edge.id);

    function onClick(event: MouseEvent<SVGElement>) {
        event.stopPropagation();
        if (!isSelectionAllowed)
            return;

        if (state.selectionType === SelectionType.Free) {
            const isSpecialKey = event.ctrlKey || event.ctrlKey;
            dispatch({ type: 'select', edgeId: edge.id, operation: isSpecialKey ? 'toggle' : 'set' });
            return;
        }

        const { nodeIds, edgeIds } = computePathWithEdge(pathEdge!, pathGraph!);
        dispatch({ type: 'path', operation: 'add', nodeIds, edgeIds });
    }

    return (
        <path
            ref={setEdgeRef}
            onClick={onClick}
            d={path}
            stroke={isSelected ? 'rgb(8, 145, 178)' : 'rgb(71, 85, 105)'}
            strokeWidth='4'
            className={cn('text-slate-600',
                isHoverAllowed && isSelectionAllowed && 'cursor-pointer pointer-events-auto path-shadow',
                pathEdge && isSelectionAllowed && 'path-shadow-green',
            )}
            markerEnd='url(#arrow)'
        />
    );
}

/**
 * Checks if an edge is selected based on the editor state.
 */
function isEdgeSelected(state: EditMappingState, edge: CategoryEdge): boolean {
    if (state.selectionType === SelectionType.None || state.selectionType === SelectionType.Path)
        return false;

    if (state.selection instanceof FreeSelection)
        return state.selection.edgeIds.has(edge.id);

    return false;
}

/**
 * Determines if an edge can be selected based on the editor state and path graph.
 */
function isEdgeSelectionAllowed({ selection, selectionType }: EditMappingState, edge: CategoryEdge, pathGraph: PathGraph | undefined): boolean {
    if (selectionType === SelectionType.None)
        return false;

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
            className='absolute border-2 border-slate-700 border-dotted pointer-events-none'
            style={style}
        />
    );
}
