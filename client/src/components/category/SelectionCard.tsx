import { type CategoryGraph } from './categoryGraph';
import { type FreeSelectionAction, type FreeSelection } from '../graph/graphSelection';
import { type Dispatch } from 'react';
import { Button } from '@heroui/react';
import { FaXmark } from 'react-icons/fa6';

type SelectionCardProps = {
    /** The current selection state of nodes and edges. */
    selection: FreeSelection;
    /** The graph containing node and edge data. */
    graph: CategoryGraph;
    /** Dispatch function for updating selection state. */
    dispatch: Dispatch<FreeSelectionAction>;
};

export function SelectionCard({ selection, graph, dispatch }: SelectionCardProps) {
    const { nodeIds, edgeIds } = selection;

    return (
        <div className='min-w-[200px] pl-3 rounded-lg'>
            <div className='max-h-[900px] overflow-y-auto'>
                {nodeIds.size > 0 && (
                    <div>
                        <div className='flex items-center justify-between pb-1'>
                            <h3 className='font-semibold'>Selected objects</h3>
                            <Button isIconOnly variant='light' size='sm' onPress={() => dispatch({ operation: 'clear', range: 'nodes' })}>
                                <FaXmark />
                            </Button>
                        </div>

                        <div className='flex flex-col'>
                            {[ ...nodeIds.values() ].map(id => renderNode(id, graph, dispatch))}
                        </div>
                    </div>
                )}

                {edgeIds.size > 0 && (
                    <div>
                        <div className='flex items-center justify-between pb-1'>
                            <h3 className='font-semibold'>Selected morphisms</h3>
                            <Button isIconOnly variant='light' size='sm' onPress={() => dispatch({ operation: 'clear', range: 'edges' })}>
                                <FaXmark />
                            </Button>
                        </div>

                        <div className='flex flex-col'>
                            {[ ...edgeIds.values() ].map(id => renderEdge(id, graph, dispatch))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

function renderNode(nodeId: string, graph: CategoryGraph, dispatch: Dispatch<FreeSelectionAction>) {
    const node = graph.nodes.get(nodeId)!;

    return (
        <div key={node.id} className='flex items-center gap-2'>
            <span className='text-primary font-semibold'>{node.schema.key.toString()}</span>
            <span className='truncate block'>
                {node.metadata.label}
            </span>
            <div className='grow' />
            <Button isIconOnly variant='light' size='sm' onPress={() => dispatch({ nodeId, operation: 'remove' })}>
                <FaXmark />
            </Button>
        </div>
    );
}

function renderEdge(edgeId: string, graph: CategoryGraph, dispatch: Dispatch<FreeSelectionAction>) {
    const edge = graph.edges.get(edgeId)!;

    return (
        <div key={edge.id} className='flex items-center gap-2'>
            <span className='text-primary font-semibold'>{edge.schema.signature.toString()}</span>
            {edge.metadata.label}
            <div className='grow' />
            <Button isIconOnly variant='light' size='sm' onPress={() => dispatch({ edgeId, operation: 'remove' })}>
                <FaXmark />
            </Button>
        </div>
    );
}
