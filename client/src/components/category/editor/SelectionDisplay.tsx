import { Button } from '@heroui/react';
import { FaXmark } from 'react-icons/fa6';
import { type CategoryEditorDispatch, type CategoryEditorState } from './useCategoryEditor';
import { type CategoryEdge, type CategoryNode } from '../graph/categoryGraph';
import { PencilIcon } from '@heroicons/react/24/solid';
import { TrashIcon } from '@heroicons/react/24/outline';
import { Cardinality } from '@/types/schema';
import { ArrowLongRightIcon } from '@heroicons/react/20/solid';
import { FreeSelection } from '../graph/selection';

type StateDispatchProps = {
    state: CategoryEditorState;
    dispatch: CategoryEditorDispatch;
};

export function SelectionDisplay({ state, dispatch }: StateDispatchProps) {
    if (!(state.selection instanceof FreeSelection))
        return null;

    if (state.selection.isEmpty) {
        return (
            <div className='py-1 text-default-500 text-center'>
                <h3 className='text-lg font-semibold'>No Selection</h3>
                <p>Select an object or morphism to edit.</p>
            </div>
        );
    }

    if (state.selection.nodeIds.size === 1 && state.selection.edgeIds.size === 0) {
        const node = state.graph.nodes.get(state.selection.firstNodeId!)!;
        return (
            <SelectedNode state={state} dispatch={dispatch} node={node} />
        );
    }

    if (state.selection.nodeIds.size === 0 && state.selection.edgeIds.size === 1) {
        const edge = state.graph.edges.get(state.selection.firstEdgeId!)!;
        return (
            <SelectedEdge state={state} dispatch={dispatch} edge={edge} />
        );
    }

    return (
        <MixedSelection state={state} dispatch={dispatch} />
    );
}

function SelectedNode({ state, dispatch, node }: StateDispatchProps & { node: CategoryNode }) {
    const key = node.schema.key;

    function deleteNode() {
        state.evocat.deleteObjex(key);
        dispatch({ type: 'deleteElements' });
    }

    return (<>
        <h3 className='text-lg font-semibold'>Selected Object</h3>

        <div>
            <div className='text-sm font-semibold text-foreground-400'>Key:</div>
            <div>{key.toString()}</div>
        </div>

        <div>
            <div className='text-sm font-semibold text-foreground-400'>Label:</div>
            <div>{node.metadata.label}</div>
        </div>

        <div>
            <div className='text-sm font-semibold text-foreground-400'>Position:</div>
            <div>({Math.round(node.x)}, {Math.round(node.y)})</div>
        </div>

        <div>
            <div className='text-sm font-semibold text-foreground-400'>Ids:</div>
            {node.schema.ids.isEmpty ? (
                <span className='italic'>no ids</span>
            ) : (
                <ul className='list-disc pl-6'>
                    {node.schema.ids.signatureIds.map(id => (
                        <li key={id.toString()}>{id.toString()}</li>
                    ))}
                </ul>
            )}
        </div>

        <div className='mt-1 flex gap-2'>
            <Button color='default' isIconOnly onPress={() => dispatch({ type: 'form', operation: 'objex', key })}>
                <PencilIcon className='size-6' />
            </Button>

            <Button color='danger' isIconOnly onPress={deleteNode}>
                <TrashIcon className='size-6' />
            </Button>
        </div>
    </>);
}

function SelectedEdge({ state, dispatch, edge }: StateDispatchProps & { edge: CategoryEdge }) {
    const signature = edge.schema.signature;

    function deleteEdge() {
        state.evocat.deleteMorphism(signature);
        dispatch({ type: 'deleteElements' });
    }

    return (<>
        <h3 className='text-lg font-semibold'>Selected Morphism</h3>

        <div>
            <div className='text-sm font-semibold text-foreground-400'>Signature:</div>
            <div>{signature.toString()}</div>
        </div>
        <div>
            <div className='text-sm font-semibold text-foreground-400'>Dom, cod:</div>
            <div>
                {edge.schema.domKey.toString()}
                <ArrowLongRightIcon className='mx-1 inline size-5' />
                {edge.schema.codKey.toString()}
            </div>
        </div>
        <div>
            <div className='text-sm font-semibold text-foreground-400'>Label:</div>
            <div>
                {edge.metadata.label || (<span className='italic'>no label</span>)}
            </div>
        </div>
        <div>
            <div className='text-sm font-semibold text-foreground-400'>Minimum cardinality:</div>
            <div>{edge.schema.min === Cardinality.Zero ? 0 : 1}</div>
        </div>

        <div className='mt-1 flex gap-2'>
            <Button color='default' isIconOnly onPress={() => dispatch({ type: 'form', operation: 'morphism', signature })}>
                <PencilIcon className='size-6' />
            </Button>

            <Button color='danger' isIconOnly onPress={deleteEdge}>
                <TrashIcon className='size-6' />
            </Button>
        </div>
    </>);
}


export function MixedSelection({ state, dispatch }: StateDispatchProps) {
    const selection = state.selection as FreeSelection;
    const { nodeIds, edgeIds } = selection;

    return (<>
        {nodeIds.size > 0 && (
            <div>
                <div className='flex items-center justify-between pb-1'>
                    <h3 className='font-semibold'>Selected objects</h3>
                    <Button isIconOnly variant='light' size='sm' onPress={() => dispatch({ type: 'selection', selection: selection.update({ operation: 'clear', range: 'nodes' }) })}>
                        <FaXmark />
                    </Button>
                </div>

                <div className='flex flex-col'>
                    {[ ...nodeIds.values() ].map(id => renderNode(id, state, dispatch))}
                </div>
            </div>
        )}

        {edgeIds.size > 0 && (
            <div>
                <div className='flex items-center justify-between pb-1'>
                    <h3 className='font-semibold'>Selected morphisms</h3>
                    <Button isIconOnly variant='light' size='sm' onPress={() => dispatch({ type: 'selection', selection: selection.update({ operation: 'clear', range: 'edges' }) })}>
                        <FaXmark />
                    </Button>
                </div>

                <div className='flex flex-col'>
                    {[ ...edgeIds.values() ].map(id => renderEdge(id, state, dispatch))}
                </div>
            </div>
        )}
    </>);
}

function renderNode(nodeId: string, state: CategoryEditorState, dispatch: CategoryEditorDispatch) {
    const selection = state.selection as FreeSelection;
    const node = state.graph.nodes.get(nodeId)!;

    return (
        <div key={node.id} className='flex items-center gap-2'>
            <span className='text-primary font-semibold'>{node.schema.key.toString()}</span>
            <span className='truncate block'>
                {node.metadata.label}
            </span>
            <div className='grow' />
            <Button isIconOnly variant='light' size='sm' onPress={() => dispatch({ type: 'selection', selection: selection.update({ operation: 'remove', nodeId }) })}>
                <FaXmark />
            </Button>
        </div>
    );
}

function renderEdge(edgeId: string, state: CategoryEditorState, dispatch: CategoryEditorDispatch) {
    const selection = state.selection as FreeSelection;
    const edge = state.graph.edges.get(edgeId)!;

    return (
        <div key={edge.id} className='flex items-center gap-2'>
            <span className='text-primary font-semibold'>{edge.schema.signature.toString()}</span>
            {edge.metadata.label}
            <div className='grow' />
            <Button isIconOnly variant='light' size='sm' onPress={() => dispatch({ type: 'selection', selection: selection.update({ operation: 'remove', edgeId }) })}>
                <FaXmark />
            </Button>
        </div>
    );
}
