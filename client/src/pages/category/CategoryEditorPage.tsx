import { useReducer, useRef, useState } from 'react';
import { api } from '@/api';
import { Category } from '@/types/schema';
import { SchemaUpdate } from '@/types/schema/SchemaUpdate';
import { type Params, useLoaderData } from 'react-router-dom';
import { EditCategoryGraphDisplay } from '@/components/schema-categories/EditCategoryGraphDisplay';
import { Button } from '@nextui-org/react';
import { FaXmark } from 'react-icons/fa6';
import { createInitialState, type EditCategoryDispatch, editCategoryReducer, type EditCategoryState } from '@/components/schema-categories/editCategoryReducer';
import { Evocat } from '@/types/evocat/Evocat';
import { PhasedEditor } from '@/components/schema-categories/PhasedCategoryEditor';
import { onSuccess } from '@/types/api/result';
import { useDeleteHandlers } from '@/components/schema-categories/useDeleteHandlers';
import { cn } from '@/components/utils';
import { TbLayoutSidebar, TbLayoutSidebarFilled, TbLayoutSidebarRight, TbLayoutSidebarRightFilled } from 'react-icons/tb';

type EditorSidebarState = {
    left: boolean;
    right: boolean;
};

export function CategoryEditorPage() {
    const loaderData = useLoaderData() as Awaited<ReturnType<typeof categoryEditorLoader>>;

    // TODO Use this to display logical models.
    // const logicalModels = useMemo(() => logicalModelsFromServer(loaderData.datasources, loaderData.mappings), [ loaderData.datasources, loaderData.mappings ]);

    // A non-reactive reference to the Evocat instance. It's used for handling events. None of its properties should be used in React directly!
    const evocatRef = useRef<Evocat>();
    if (!evocatRef.current) {
        const updates = loaderData.updates.map(SchemaUpdate.fromServer);
        const category = Category.fromServer(loaderData.category);

        evocatRef.current = new Evocat(category, updates);
    }

    const [ state, dispatch ] = useReducer(editCategoryReducer, evocatRef.current, createInitialState);
    useDeleteHandlers(state, dispatch);

    const [ sidebarState, setSidebarState ] = useState<EditorSidebarState>({
        left: true,
        right: true,
    });
    
    const toggleSidebar = (side: keyof EditorSidebarState) => {
        setSidebarState(prev => ({
            ...prev,
            [side]: !prev[side], // toggle the specified sidebar
        }));
    };

    return (
        <div className='flex flex-col h-screen'>
            {/* Navbar */}
            <div className='h-8 flex items-center justify-between px-4 shadow-md bg-zinc-200'>
                <div className='flex items-center gap-3'>
                    <Button isIconOnly variant='light' onClick={() => toggleSidebar('left')}>
                        {sidebarState.left ? <TbLayoutSidebarFilled /> : <TbLayoutSidebar />}
                    </Button>
                </div>

                {/* Save and Delete Actions */}
                <div className='flex items-center gap-2'>
                    <SaveButton state={state} dispatch={dispatch} />
                    {/* <Button color='danger' startContent={<FaTrash />} onClick={() => dispatch({ type: 'deleteCategory' })}>
                        Delete
                    </Button> */}
                    <Button isIconOnly variant='light' onClick={() => toggleSidebar('right')}>
                        {sidebarState.right ? <TbLayoutSidebarRightFilled /> : <TbLayoutSidebarRight />}
                    </Button>
                </div>
            </div>

            <div className='flex flex-grow'>
                {/* Left Sidebar (Phased Editor) */}
                <aside className={cn(`transition-all duration-300 ${sidebarState.left ? 'w-56' : 'w-0'} overflow-hidden bg-gray-100`)}>
                    {sidebarState.left && <PhasedEditor state={state} dispatch={dispatch} />}
                </aside>

                {/* Main Canvas */}
                <main className='flex-grow relative'>
                    <EditCategoryGraphDisplay state={state} dispatch={dispatch} className='w-full h-full' />

                    {/* Floating Selection Card */}
                    {/* {(state.selection.nodeIds.size > 0 || state.selection.edgeIds.size > 0) && (
                        <div className='absolute top-2 right-2 z-20'>
                            <SelectionCard state={state} dispatch={dispatch} />
                        </div>
                    )} */}
                </main>

                {/* Right Sidebar (Info about selected object) */}
                <aside className={`transition-all duration-300 ${sidebarState.right ? 'w-60' : 'w-0'} overflow-hidden bg-gray-100`}>
                    {sidebarState.right && <SelectionCard state={state} dispatch={dispatch} />}
                </aside>
            </div>
        </div>
    );
}

CategoryEditorPage.loader = categoryEditorLoader;

async function categoryEditorLoader({ params: { categoryId } }: { params: Params<'categoryId'> }) {
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

type StateDispatchProps = Readonly<{
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

function SelectionCard({ state, dispatch }: StateDispatchProps) {
    function unselectNode(nodeId: string) {
        dispatch({ type: 'select', nodeId, operation: 'remove' });
    }

    function unselectEdge(edgeId: string) {
        dispatch({ type: 'select', edgeId, operation: 'remove' });
    }

    const { nodeIds, edgeIds } = state.selection;

    return (
        <div className='min-w-[200px] pl-3 rounded-lg bg-background '>
            <div className='max-h-[600px] overflow-y-auto'>
                {nodeIds.size > 0 && (
                    <div>
                        <div className='flex items-center justify-between pb-1'>
                            <h3 className='font-semibold'>Selected objects</h3>
                            <Button isIconOnly variant='light' size='sm' onClick={() => dispatch({ type: 'select', operation: 'clear', range: 'nodes' })}>
                                <FaXmark />
                            </Button>
                        </div>

                        <div className='flex flex-col'>
                            {[ ...nodeIds.values() ].map(id => {
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

                {edgeIds.size > 0 && (
                    <div>
                        <div className='flex items-center justify-between pb-1'>
                            <h3 className='font-semibold'>Selected morphisms</h3>
                            <Button isIconOnly variant='light' size='sm' onClick={() => dispatch({ type: 'select', operation: 'clear', range: 'edges' })}>
                                <FaXmark />
                            </Button>
                        </div>

                        <div className='flex flex-col'>
                            {[ ...edgeIds.values() ].map(id => {
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
        </div>
    );
}

function SaveButton({ state }: StateDispatchProps) {
    const [ isFetching, setIsFetching ] = useState(false);

    async function save() {
        setIsFetching(true);

        await state.evocat.update(async edit => {
            const response = await api.schemas.updateCategory({ id: state.evocat.category.id }, edit);
            return onSuccess(response, fromServer => Category.fromServer(fromServer));
        });

        setIsFetching(false);
    }

    return (
        <Button color='default' onClick={save} isLoading={isFetching} size='sm'>
            Save
        </Button>
    );
}
