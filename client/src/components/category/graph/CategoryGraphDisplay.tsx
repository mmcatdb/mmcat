import { type ReactNode, type MouseEvent, useRef, type Dispatch, useCallback } from 'react';
import { type CategoryGraph, type CategoryEdge, type CategoryNode } from './categoryGraph';
import { type GraphMoveEvent, type GraphEvent, type GraphOptions } from '@/components/graph/graphEngine';
import { GraphProvider } from '@/components/graph/GraphProvider';
import { useCanvas, useEdge, useNode, useSelectionBox } from '@/components/graph/graphHooks';
import { EDGE_ARROW_LENGTH, getEdgeDegree, type Node } from '@/components/graph/graphUtils';
import { FreeSelection, type CategoryGraphSelection, PathCount, PathSelection, SequenceSelection, computePathToNode, computePathWithEdge } from '@/components/category/graph/selection';
import { usePreferences } from '@/components/PreferencesProvider';
import { cn } from '@/components/utils';
import { type GraphHighlights } from './highlights';

type MapppingEditorGraphProps = {
    graph: CategoryGraph;
    selection?: CategoryGraphSelection;
    dispatch: Dispatch<GraphMoveEvent | SelectionAction>;
    /** Optional graph rendering options. */
    options?: GraphOptions;
    highlights?: GraphHighlights;
    /** Optional CSS class for styling the canvas. */
    className?: string;
};

type SelectionAction = {
    type: 'selection';
    selection: CategoryGraphSelection | undefined;
};

/**
 * Renders a graph-based UI for editing mappings, including nodes, edges, and selection.
 */
export function CategoryGraphDisplay({ graph, selection, dispatch, highlights, options, className }: MapppingEditorGraphProps) {
    // Not ideal but we don't use it for rendering so it should be ok.
    const selectionRef = useRef(selection);
    selectionRef.current = selection;

    const graphDispatch = useCallback((event: GraphEvent) => {
        if (event.type === 'move') {
            dispatch(event);
            return;
        }

        const selection = selectionRef.current;
        if (selection instanceof FreeSelection)
            dispatch({ type: 'selection', selection: selection.updateFromGraphEvent(event) });

        // Other types of selection don't make sense with the box selection.
    }, [ dispatch ]);

    const onSelection = useCallback((selection: CategoryGraphSelection | undefined) => {
        dispatch({ type: 'selection', selection });
    }, [ dispatch ]);

    return (
        <GraphProvider dispatch={graphDispatch} graph={graph} options={options}>
            <CanvasDisplay className={className}>
                {graph.nodes.values().toArray().map(node => (
                    <NodeDisplay
                        key={node.id}
                        node={node}
                        selection={selection}
                        onSelection={onSelection}
                        highlights={highlights}
                    />
                ))}

                {/* SVG layer for rendering edges with arrow markers */}
                <svg fill='none' xmlns='http://www.w3.org/2000/svg' className='w-full h-full pointer-events-none select-none'>
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
                    {graph.edges.bundledEdges.flatMap(bundle => bundle.map((edge, index) => (
                        <EdgeDisplay
                            key={edge.id}
                            edge={edge}
                            degree={getEdgeDegree(edge, index, bundle.length)}
                            selection={selection}
                            onSelection={onSelection}
                        />
                    )))}
                </svg>

                <SelectionBox selection={selection} />
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
            className={cn('relative bg-canvas overflow-hidden',
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
    node: CategoryNode;
    selection: CategoryGraphSelection | undefined;
    onSelection: Dispatch<CategoryGraphSelection | undefined>;
    highlights: GraphHighlights | undefined;
};

function NodeDisplay({ node, selection, onSelection, highlights }: NodeDisplayProps) {
    const { theme } = usePreferences().preferences;
    const { setNodeRef, onMouseDown, style, isHoverAllowed, isDragged } = useNode(node);

    const isSelected = selection && isNodeSelected(selection, node);
    const isSelectionAllowed = selection && isNodeSelectionAllowed(selection, node);
    const isClickable = isHoverAllowed && isSelectionAllowed;
    const isHighlighted = highlights?.nodeIds.has(node.id);

    function onClick(event: MouseEvent<HTMLElement>) {
        event.stopPropagation();
        if (!isClickable)
            return;

        if (selection instanceof FreeSelection) {
            const isSpecialKey = event.ctrlKey || event.shiftKey;
            onSelection(selection.update({ nodeId: node.id, operation: isSpecialKey ? 'toggle' : 'set' }));
            return;
        }

        if (selection instanceof SequenceSelection) {
            onSelection(selection.update({ operation: 'toggle', nodeId: node.id }));
            return;
        }

        if (selection instanceof PathSelection) {
            if (node.id === selection.lastNodeId) {
                // The last node is clicked, we can remove it (unless the path is empty).
                if (!selection.isEmpty)
                    onSelection(selection.update({ operation: 'remove' }));

                return;
            }

            const pathNode = selection.pathGraph.nodes.get(node.id);
            if (pathNode) {
                const { nodeIds, edgeIds } = computePathToNode(pathNode);
                onSelection(selection.update({ operation: 'add', nodeIds, edgeIds }));
            }
        }
    }

    const pathNode = selection instanceof PathSelection && selection.pathGraph.nodes.get(node.id);

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={cn('absolute w-0 h-0 select-none', isDragged ? 'z-20' : 'z-10')}
        >
            <div
                className={cn(
                    'absolute size-8 -left-4 -top-4 rounded-full border-2',
                    isClickable && 'cursor-pointer hover:scale-110',
                    isDragged && 'pointer-events-none scale-110',
                    theme === 'light' ? [
                        !isSelected && 'bg-background border-default-600',
                        isHighlighted && 'bg-success',
                        isSelected && 'bg-primary-200 border-primary-500',
                        isClickable && 'active:bg-primary-200 active:border-primary-400',
                    ] : [
                        !isSelected && 'bg-default-200 border-default-900',
                        isHighlighted && 'bg-success',
                        isSelected && 'bg-primary-400 border-primary-600',
                        isClickable && 'active:bg-primary-500 active:border-default-900',
                    ],
                    pathNode && pathClasses[pathNode.pathCount],
                )}
                onClick={onClick}
                onMouseDown={onMouseDown}
            />

            <div className='w-fit h-0'>
                <span className={cn('relative -left-1/2 -top-10 font-medium pointer-events-none whitespace-nowrap inline-block truncate max-w-[150px] text-default-700',
                )}>
                    {node.metadata.label}
                </span>
            </div>
        </div>
    );
}

function isNodeSelected(selection: CategoryGraphSelection, node: Node): boolean {
    if (selection instanceof FreeSelection)
        return selection.nodeIds.has(node.id);
    if (selection instanceof SequenceSelection)
        return selection.has(node.id);
    if (selection instanceof PathSelection)
        return selection.lastNodeId === node.id;
    return false;
}

function isNodeSelectionAllowed(selection: CategoryGraphSelection, node: Node): boolean {
    if (selection instanceof FreeSelection)
        return true;

    if (selection instanceof SequenceSelection)
        return true;

    if (selection instanceof PathSelection) {
        const pathNode = selection.pathGraph.nodes.get(node.id);
        if (!pathNode)
            return false;

        // Can't remove the root node of the path.
        if (selection.isEmpty && pathNode.id === selection.lastNodeId)
            return false;

        // There is only one path to the node, or we are removing the last node of the selection.
        return pathNode.pathCount === PathCount.One || pathNode.id === selection.lastNodeId;
    }

    return false;
}

const pathClasses: Record<PathCount, string | undefined> = {
    [PathCount.None]: undefined,
    [PathCount.One]: 'shadow-[0_0_20px_0_rgba(0,0,0,0.3)] shadow-green-400',
    [PathCount.Many]: 'shadow-[0_0_20px_0_rgba(0,0,0,0.3)] shadow-red-400',
};

type EdgeDisplayProps = {
    edge: CategoryEdge;
    /** The degree offset for bundled edges. */
    degree: number;
    selection: CategoryGraphSelection | undefined;
    onSelection: Dispatch<CategoryGraphSelection | undefined>;
    // highlights: GraphHighlights | undefined;
};

function EdgeDisplay({ edge, degree, selection, onSelection }: EdgeDisplayProps) {
    const { setEdgeRef, svg, isHoverAllowed } = useEdge(edge, degree);

    const isSelected = selection && isEdgeSelected(selection, edge);
    const isSelectionAllowed = selection && isEdgeSelectionAllowed(selection, edge);
    // TODO
    // const isHighlighted = cache.highlights?.edgeIds.has(edge.id);

    function onClick(event: MouseEvent<SVGElement>) {
        event.stopPropagation();
        if (!isSelectionAllowed)
            return;

        if (selection instanceof FreeSelection) {
            const isSpecialKey = event.ctrlKey || event.shiftKey;
            onSelection(selection.update({ edgeId: edge.id, operation: isSpecialKey ? 'toggle' : 'set' }));
            return;
        }

        if (selection instanceof PathSelection) {
            const pathEdge = selection.pathGraph.edges.get(edge.id);
            const { nodeIds, edgeIds } = computePathWithEdge(pathEdge!, selection.pathGraph);
            onSelection(selection.update({ operation: 'add', nodeIds, edgeIds }));
        }
    }

    const pathEdge = selection instanceof PathSelection && selection.pathGraph.edges.get(edge.id);

    return (<>
        <path
            ref={setEdgeRef.path}
            onClick={onClick}
            d={svg.path}
            stroke={isSelected ? 'hsl(var(--heroui-primary))' : 'hsl(var(--heroui-default-500))'}
            strokeWidth='4'
            className={cn('text-zinc-600',
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
            className={cn('font-medium', !svg.label && 'hidden')}
            fill='currentColor'
            textAnchor='middle'
        >
            {edge.label}
        </text>
    </>);
}

function isEdgeSelected(selection: CategoryGraphSelection, edge: CategoryEdge): boolean {
    if (selection instanceof FreeSelection)
        return selection.edgeIds.has(edge.id);

    return false;
}

function isEdgeSelectionAllowed(selection: CategoryGraphSelection, edge: CategoryEdge): boolean {
    if (selection instanceof FreeSelection)
        return true;

    if (selection instanceof SequenceSelection)
        return false;

    if (selection instanceof PathSelection) {
        const { edges, nodes } = selection.pathGraph;

        const pathEdge = edges.get(edge.id);
        if (pathEdge?.traversableDirection === undefined)
            return false;

        const fromNode = nodes.get(pathEdge.traversableDirection ? edge.from : edge.to);
        if (fromNode?.pathCount !== PathCount.One)
            return false;

        // We only allow edges to the ambiguous nodes (so that the graph doesn't look like a coloring book).
        const toNode = nodes.get(pathEdge.traversableDirection ? edge.to : edge.from);
        if (toNode?.pathCount !== PathCount.Many)
            return false;

        return true;
    }

    return false;
}

/**
 * Enables multi-node/edge selection.
 */
function SelectionBox({ selection }: { selection: CategoryGraphSelection | undefined }) {
    const { setSelectionBoxRef, style } = useSelectionBox();

    if (!(selection instanceof FreeSelection))
        return null;

    return (
        <div
            ref={setSelectionBoxRef}
            className='absolute border-2 border-primary-500 border-dashed pointer-events-none bg-primary-100/20'
            style={style}
        />
    );
}
