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
import { PhasedEditor } from '@/components/schema-categories/PhasedEditor';

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

    // TODO undo/redo has to be done through some event system, not directly in the reducer.
    // The reason is that the reducer has to be a pure function, only the event system can handle side effects.

    return (
        <div>
            <SchemaCategoryContext category={evocatRef.current.category} />
            <h1>Schema category {evocatRef.current.category.label} overview</h1>
            <p>
                Some text.
            </p>
            <p>
                updates: {evocatRef.current.updates.length}
            </p>

            <div className='relative'>
                <EditorGraphDisplay state={state} dispatch={dispatch} className='w-full min-h-[600px]' />
                {state.selectedNodeIds.size > 0 && (
                    <div className='z-20 absolute top-2 right-2'>
                        <SelectedNodesCard evocat={evocatRef.current} state={state} dispatch={dispatch} />
                    </div>
                )}
            </div>

            <PhasedEditor evocat={evocatRef.current} state={state} dispatch={dispatch} className='mt-3 w-80'/>
        </div>
    );
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

type SelectedNodesCardProps = Readonly<{
    evocat: Evocat;
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

function SelectedNodesCard({ evocat, state, dispatch }: SelectedNodesCardProps) {
    function unselectNode(nodeId: string) {
        dispatch({ type: 'selectNode', nodeId, operation: 'remove' });
    }

    return (
        <div className='min-w-[200px] p-3 rounded-lg bg-black'>
            <div className='flex items-center justify-between pb-1'>
                <h3 className='font-semibold'>Selected objects</h3>
                <Button isIconOnly variant='light' size='sm' onClick={() => dispatch({ type: 'selectNode', operation: 'clear' })}>
                    <FaXmark />
                </Button>
            </div>
            <div className='flex flex-col'>
                {[ ...state.selectedNodeIds.values() ].map(id => {
                    const node = state.graph.nodes.find(node => node.id === id)!;
                    const objex = evocat.category.getObjex(node.schema.key);

                    return (
                        <div key={node.id} className='flex items-center gap-2'>
                            <span className='text-primary font-semibold'>{objex.key.toString()}</span>
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
    );
}
