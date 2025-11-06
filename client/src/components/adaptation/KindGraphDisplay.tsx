import { type ReactNode, type MouseEvent } from 'react';
import { type Graph, type GraphOptions } from '../graph/graphEngine';
import { GraphProvider } from '../graph/GraphProvider';
import { useCanvas, useEdge, useNode, useSelectionBox } from '../graph/graphHooks';
import { EDGE_ARROW_LENGTH } from '../graph/graphUtils';
import { cn } from '@/components/utils';
import { emptyFunction } from '@/types/utils/common';
import { type KindEdge, type KindNode, type KindGraph } from './kindGraph';
import { DATASOURCE_MODELS } from '@/types/Datasource';
import { DatasourceIcon } from '../datasources/DatasourceBadge';

type KindGraphDisplayProps = {
    graph: KindGraph;
    options?: GraphOptions;
    className?: string;
};

export function KindGraphDisplay({ graph, options, className }: KindGraphDisplayProps) {
    return (
        <GraphProvider dispatch={emptyFunction} graph={graph} options={options}>
            <CanvasDisplay className={className}>
                {graph.nodes.values().toArray().map(node => (
                    <NodeDisplay key={node.id} node={node} />
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

                    {graph.edges.bundledEdges.flatMap(bundle => bundle.map(edge => (
                        <EdgeDisplay
                            key={edge.id}
                            edge={edge}
                            graph={graph}
                        />
                    )))}
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
    node: KindNode;
};

function NodeDisplay({ node }: NodeDisplayProps) {
    const { setNodeRef, onMouseDown, style, isHoverAllowed, isDragging } = useNode(node);
    // const { theme } = usePreferences().preferences;

    function onClick(event: MouseEvent<HTMLElement>) {
        event.stopPropagation();
    }

    const model = DATASOURCE_MODELS[node.datasource.type];

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={cn('absolute w-0 h-0 select-none', isDragging ? 'z-20' : 'z-10')}
        >
            <div
                className={cn('absolute size-8 -left-4 -top-4 flex items-center justify-center rounded-full border-2',
                    isHoverAllowed && 'cursor-pointer hover:shadow-md hover:shadow-primary-200/50 hover:scale-110',
                    isDragging && 'pointer-events-none shadow-primary-300/50 scale-110',
                )}
                onClick={onClick}
                onMouseDown={onMouseDown}
                style={{ backgroundColor: `var(--mm-${model}-light)`, borderColor: `var(--mm-${model}-dark)` }}
            >
                <DatasourceIcon type={node.datasource.type} className='text-black' />
            </div>

            {/* Node label with truncation for long text */}
            <div className='w-fit h-0'>
                <span className='relative -left-1/2 -top-10 font-medium pointer-events-none whitespace-nowrap inline-block truncate max-w-[150px] text-default-700'>
                    {node.label}
                </span>
            </div>
        </div>
    );
}

type EdgeDisplayProps = {
    edge: KindEdge;
    graph: Graph;
};

function EdgeDisplay({ edge, graph }: EdgeDisplayProps) {
    const { setEdgeRef, svg, isHoverAllowed } = useEdge(edge, 0, graph);

    function onClick(event: MouseEvent<SVGElement>) {
        event.stopPropagation();
    }

    return (
        <path
            ref={setEdgeRef.path}
            onClick={onClick}
            d={svg.path}
            stroke='hsl(var(--heroui-default-500))'
            strokeWidth='4'
            className={isHoverAllowed ? 'cursor-pointer pointer-events-auto hover:drop-shadow-[0_0_4px_rgba(0,176,255,0.5)]' : undefined}
            markerEnd='url(#arrow)'
        />
    );
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
