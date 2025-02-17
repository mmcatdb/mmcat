import { useReducer, useRef } from 'react';
import { api } from '@/api';
import { Category } from '@/types/schema';
import { SchemaUpdate } from '@/types/schema/SchemaUpdate';
import { type Params, useLoaderData } from 'react-router-dom';
import { Portal, portals } from '@/components/common';
import { logicalModelsFromServer } from '@/types/datasource';
import { EditorGraphDisplay } from '@/components/schema-categories/EditorGraphDisplay';
import { Button } from '@nextui-org/react';
import { FaXmark } from 'react-icons/fa6';
import { createInitialState, type EditCategoryDispatch, editCategoryReducer, type EditCategoryState } from '@/components/schema-categories/editCategoryReducer';
import { Evocat } from '@/types/evocat/Evocat';
import { PhasedEditor } from '@/components/schema-categories/PhasedCategoryEditor';

export function SchemaCategoryEditor() {
    const loaderData = useLoaderData() as Awaited<ReturnType<typeof evocatLoader>>;

    // A non-reactive reference to the Evocat instance. It's used for handling events. None of its properties should be used in React directly!
    const evocatRef = useRef<Evocat>();
    if (!evocatRef.current) {
        const updates = loaderData.updates.map(SchemaUpdate.fromServer);
        const logicalModels = logicalModelsFromServer(loaderData.datasources, loaderData.mappings);
        const category = Category.fromServer(loaderData.category, logicalModels);

        evocatRef.current = new Evocat(category, updates);
    }

    const [ state, dispatch ] = useReducer(editCategoryReducer, evocatRef.current, createInitialState);

    return (<>
        <EditorGraphDisplay state={state} dispatch={dispatch} className='w-full h-full flex-grow' />

        {(state.selectedNodeIds.size > 0 || state.selectedEdgeIds.size > 0) && (
            <div className='z-20 absolute top-2 right-2'>
                <SelectionCard evocat={evocatRef.current} state={state} dispatch={dispatch} />
            </div>
        )}
        
        <PhasedEditor evocat={evocatRef.current} state={state} dispatch={dispatch} className='w-80 z-20 absolute bottom-2 left-2'/>
    </>);
}

export async function evocatLoader({ params: { categoryId } }: { params: Params<'categoryId'> }) {
    if (!categoryId)
        throw new Error('Category ID is required');

    const [ categoryResponse, updatesResponse, datasourcesResponse, mappingsResponse ] = await Promise.all([
        api.schemas.getCategory({ id: categoryId }),
        api.schemas.getCategoryUpdates({ id: categoryId }),
        api.datasources.getAllDatasources({}, { categoryId: categoryId }),
        api.mappings.getAllMappingsInCategory({}, { categoryId: categoryId }),
    ]);

    if (!categoryResponse.status || !updatesResponse.status || !datasourcesResponse.status || !mappingsResponse.status)
        throw new Error('Failed to load schema category');

    return {
        category: categoryResponse.data,
        updates: updatesResponse.data,
        datasources: datasourcesResponse.data,
        mappings: mappingsResponse.data,
    };
}

type SchemaCategoryContextProps = Readonly<{
    category: Category;
}>;

function SchemaCategoryContext({ category }: SchemaCategoryContextProps) {
    return (
        <Portal to={portals.context}>
            <div className='p-2'>
                Context for: {category.label}
            </div>
        </Portal>
    );
}

type SelectionCardProps = Readonly<{
    evocat: Evocat;
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

function SelectionCard({ state, dispatch }: SelectionCardProps) {
    function unselectNode(nodeId: string) {
        dispatch({ type: 'select', nodeId, operation: 'remove' });
    }

    function unselectEdge(edgeId: string) {
        dispatch({ type: 'select', edgeId, operation: 'remove' });
    }

    return (
        <div className='min-w-[200px] p-3 rounded-lg bg-background space-y-3'>
            {state.selectedNodeIds.size > 0 && (
                <div>
                    <div className='flex items-center justify-between pb-1'>
                        <h3 className='font-semibold'>Selected objects</h3>
                        <Button isIconOnly variant='light' size='sm' onClick={() => dispatch({ type: 'select', operation: 'clear', range: 'nodes' })}>
                            <FaXmark />
                        </Button>
                    </div>

                    <div className='flex flex-col'>
                        {[ ...state.selectedNodeIds.values() ].map(id => {
                            const node = state.graph.nodes.find(node => node.id === id)!;

                            return (
                                <div key={node.id} className='flex items-center gap-2'>
                                    <span className='text-primary font-semibold'>{node.schema.key.toString()}</span>
                                    {node.metadata.label}
                                    <div className='grow' />
                                    <Button isIconOnly variant='light' size='sm' onClick={() => unselectNode(node.id)}>
                                        <FaXmark />
                                    </Button>
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}

            {state.selectedEdgeIds.size > 0 && (
                <div>
                    <div className='flex items-center justify-between pb-1'>
                        <h3 className='font-semibold'>Selected morphisms</h3>
                        <Button isIconOnly variant='light' size='sm' onClick={() => dispatch({ type: 'select', operation: 'clear', range: 'edges' })}>
                            <FaXmark />
                        </Button>
                    </div>

                    <div className='flex flex-col'>
                        {[ ...state.selectedEdgeIds.values() ].map(id => {
                            const edge = state.graph.edges.find(edge => edge.id === id)!;

                            return (
                                <div key={edge.id} className='flex items-center gap-2'>
                                    <span className='text-primary font-semibold'>{edge.schema.signature.toString()}</span>
                                    {edge.metadata.label}
                                    <div className='grow' />
                                    <Button isIconOnly variant='light' size='sm' onClick={() => unselectEdge(edge.id)}>
                                        <FaXmark />
                                    </Button>
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}
        </div>
    );
}
