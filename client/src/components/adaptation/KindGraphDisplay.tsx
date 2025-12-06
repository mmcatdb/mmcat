import { type ReactNode, type MouseEvent, type Dispatch, useCallback, useRef } from 'react';
import { type GraphEvent, type GraphOptions } from '../graph/graphEngine';
import { GraphProvider } from '../graph/GraphProvider';
import { useCanvas, useEdge, useNode, useSelectionBox } from '../graph/graphHooks';
import { EDGE_ARROW_LENGTH, getEdgeDegree, type Node } from '../graph/graphUtils';
import { cn } from '@/components/common/utils';
import { type KindEdge, type KindNode, type KindGraph } from './kindGraph';
import { DATASOURCE_MODELS } from '@/types/Datasource';
import { DatasourceIcon } from '../datasource/DatasourceBadge';
import { FreeSelection } from '../graph/selection';
import { type UseKindGraphDispatch } from './useKindGraph';
import { usePreferences } from '../context/PreferencesProvider';

// TODO
const USE_SELECTION_BOX = false;

type KindGraphDisplayProps = {
    graph: KindGraph;
    selection: FreeSelection;
    dispatch: UseKindGraphDispatch;
    options?: GraphOptions;
    className?: string;
};

export function KindGraphDisplay({ graph, selection, dispatch, options, className }: KindGraphDisplayProps) {
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

    const onSelection = useCallback((selection: FreeSelection) => {
        dispatch({ type: 'selection', selection });
    }, [ dispatch ]);

    return (
        <GraphProvider dispatch={graphDispatch} graph={graph} options={options}>
            <CanvasDisplay className={className} onSelection={onSelection}>
                {graph.nodes.values().toArray().map(node => (
                    <NodeDisplay
                        key={node.id}
                        node={node}
                        selection={selection}
                        onSelection={onSelection}
                    />
                ))}

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

                {USE_SELECTION_BOX && (
                    <SelectionBox />
                )}
            </CanvasDisplay>
        </GraphProvider>
    );
}

type CanvasDisplayProps = {
    children: ReactNode;
    className?: string;
    onSelection: (selection: FreeSelection) => void;
};

/**
 * Renders the canvas for the graph, handling mouse interactions and styling.
 */
function CanvasDisplay({ children, className, onSelection }: CanvasDisplayProps) {
    const { setCanvasRef, onMouseDown, isDragging } = useCanvas();

    return (
        <div
            ref={setCanvasRef}
            className={cn('relative bg-canvas overflow-hidden',
                isDragging ? 'cursor-grabbing' : 'cursor-default',
                className,
            )}
            onMouseDown={onMouseDown}
            // Clear selection when clicking on empty canvas area.
            // TODO Remove this if the selection box is enabled.
            onClick={() => onSelection(FreeSelection.create())}
        >
            {children}
        </div>
    );
}

type NodeDisplayProps = {
    node: KindNode;
    selection: FreeSelection;
    onSelection: Dispatch<FreeSelection>;
};

function NodeDisplay({ node, selection, onSelection }: NodeDisplayProps) {
    const { theme } = usePreferences().preferences;
    const { setNodeRef, onMouseDown, style, isHoverAllowed, isDragged } = useNode(node);

    const isSelected = isNodeSelected(selection, node);
    const isClickable = isHoverAllowed;

    function onClick(event: MouseEvent<HTMLElement>) {
        event.stopPropagation();
        if (!isClickable)
            return;

        const isSpecialKey = event.ctrlKey || event.shiftKey;
        onSelection(selection.update({ nodeId: node.id, operation: isSpecialKey ? 'toggle' : 'set' }));
        return;
    }

    const model = node.datasource && DATASOURCE_MODELS[node.datasource.type];

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={cn('absolute w-0 h-0 select-none', isDragged ? 'z-20' : 'z-10')}
        >
            <div
                className={cn('absolute size-8 -left-4 -top-4 flex items-center justify-center rounded-full border-2',
                    isClickable && 'cursor-pointer hover:scale-110',
                    isDragged && 'pointer-events-none scale-110',
                    isSelected && 'shadow-[0_0_20px_0_rgba(0,0,0,0.3)]',
                    'active:shadow-[0_0_20px_0_rgba(0,0,0,0.3)]',
                    theme === 'light' ? [
                        'bg-background border-default-600',
                        isSelected && 'shadow-primary-500',
                        isClickable && 'active:shadow-primary-600',
                    ] : [
                        'bg-default-200 border-default-900',
                        isSelected && 'shadow-primary-400',
                        isClickable && 'active:shadow-primary-300',
                    ],
                )}
                onClick={onClick}
                onMouseDown={onMouseDown}
                style={model && { backgroundColor: `var(--mm-${model}-light)`, borderColor: `var(--mm-${model}-dark)` }}
            >
                {node.datasource && (
                    <DatasourceIcon type={node.datasource.type} className='text-black' />
                )}
            </div>

            {/* Node label with truncation for long text */}
            <div className='w-fit h-0'>
                <span className='relative -left-1/2 -top-10 font-medium pointer-events-none whitespace-nowrap inline-block truncate max-w-[150px] text-default-700'>
                    {node.objex.metadata.label}
                </span>
            </div>
        </div>
    );
}

function isNodeSelected(selection: FreeSelection, node: Node): boolean {
    return selection.nodeIds.has(node.id);
}

type EdgeDisplayProps = {
    edge: KindEdge;
    degree: number;
    selection: FreeSelection;
    onSelection: Dispatch<FreeSelection>;
};

function EdgeDisplay({ edge, degree, selection, onSelection }: EdgeDisplayProps) {
    const { setEdgeRef, svg, isHoverAllowed } = useEdge(edge, degree);

    const isSelected = isEdgeSelected(selection, edge);

    function onClick(event: MouseEvent<SVGElement>) {
        event.stopPropagation();

        const isSpecialKey = event.ctrlKey || event.shiftKey;
        onSelection(selection.update({ edgeId: edge.id, operation: isSpecialKey ? 'toggle' : 'set' }));
    }

    return (
        <path
            ref={setEdgeRef.path}
            onClick={onClick}
            d={svg.path}
            stroke={isSelected ? 'hsl(var(--heroui-primary))' : 'hsl(var(--heroui-default-500))'}
            strokeWidth='4'
            className={isHoverAllowed ? 'cursor-pointer pointer-events-auto hover:drop-shadow-[0_0_4px_rgba(0,176,255,0.5)]' : undefined}
            markerEnd='url(#arrow)'
        />
    );
}

function isEdgeSelected(selection: FreeSelection, edge: KindEdge): boolean {
    return selection.edgeIds.has(edge.id);
}

/**
 * Enables multi-node/edge selection.
 */
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
