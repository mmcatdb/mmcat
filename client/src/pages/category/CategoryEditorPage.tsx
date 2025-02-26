import { useEffect, useReducer, useRef, useState } from 'react';
import { api } from '@/api';
import { Category, isPositionEqual } from '@/types/schema';
import { SchemaUpdate } from '@/types/schema/SchemaUpdate';
import { type Params, useLoaderData } from 'react-router-dom';
import { EditCategoryGraphDisplay } from '@/components/schema-categories/EditCategoryGraphDisplay';
import { Button } from '@nextui-org/react';
import { FaSpinner, FaTrash, FaXmark } from 'react-icons/fa6';
import { createInitialState, type EditCategoryDispatch, editCategoryReducer, type EditCategoryState } from '@/components/schema-categories/editCategoryReducer';
import { Evocat } from '@/types/evocat/Evocat';
import { deleteObjex, deleteSelectedMorphism, PhasedEditor } from '@/components/schema-categories/PhasedCategoryEditor';
import { onSuccess } from '@/types/api/result';
import { useDeleteHandlers } from '@/components/schema-categories/useDeleteHandlers';
import { cn } from '@/components/utils';
import { TbLayoutSidebarFilled, TbLayoutSidebarRightFilled } from 'react-icons/tb';
import { FaSave } from 'react-icons/fa';
import { toast } from 'react-toastify';

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

    // Ctrl+S to save
    useEffect(() => {
        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.ctrlKey && event.key === 's') {
                event.preventDefault(); // Stop browser save dialog
                document.getElementById('save-button')?.click(); // Trigger save
            }
        };

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, []);

    return (
        <div className='flex flex-col h-screen'>
            {/* Navbar */}
            <div className='h-8 flex items-center justify-between px-4 shadow-md bg-zinc-200 border-b border-zinc-300'>
                <div className='flex items-center gap-3'>
                    {/* Left Sidebar Toggle */}
                    <TbLayoutSidebarFilled
                        className='cursor-pointer text-zinc-500 hover:text-zinc-600'
                        onClick={() => toggleSidebar('left')}
                        title='Toggle Main Editor Sidebar'
                        size={18}
                    />

                    {/* Divider */}
                    <div className='w-px bg-zinc-400 h-5 mx-2'></div>

                    {/* Delete Button */}
                    <FaTrash
                        className='cursor-pointer text-rose-500 hover:text-rose-600'
                        onClick={() => {
                            const singleSelectedNode = (state.selection.nodeIds.size === 1 && state.selection.edgeIds.size === 0)
                                ? state.graph.nodes.find(node => state.selection.nodeIds.has(node.id))
                                : undefined;
                    
                            if (singleSelectedNode) 
                                deleteObjex(state, dispatch, singleSelectedNode);
                            else 
                                deleteSelectedMorphism(state, dispatch);
                            
                        }}
                        title='Delete (Delete)'
                        size={16}
                    />
                </div>

                {/* Save and Right Sidebar Toggle */}
                <div className='flex items-center gap-2'>
                    <SaveButton state={state} dispatch={dispatch} />

                    {/* Divider */}
                    <div className='w-px bg-zinc-400 h-5 mx-2'></div>

                    {/* Right Sidebar Toggle */}
                    <TbLayoutSidebarRightFilled
                        className='cursor-pointer text-zinc-500 hover:text-zinc-600'
                        onClick={() => toggleSidebar('right')}
                        title='Toggle Info Sidebar'
                        size={18}
                    />
                </div>
            </div>

            <div className='flex flex-grow'>
                {/* Left Sidebar */}
                <aside className={cn(`transition-all duration-300 ${sidebarState.left ? 'w-56' : 'w-0'} overflow-hidden bg-gray-100`)}>
                    {sidebarState.left && <PhasedEditor state={state} dispatch={dispatch} />}
                </aside>

                {/* Main Canvas */}
                <main className='flex-grow relative'>
                    <EditCategoryGraphDisplay state={state} dispatch={dispatch} className='w-full h-full' />
                </main>

                {/* Right Sidebar */}
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
            <div className='max-h-[900px] overflow-y-auto'>
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
    const [ hasUnsavedChanges, setHasUnsavedChanges ] = useState(false);

    useEffect(() => {
        const checkForChanges = () => {
            setHasUnsavedChanges(detectUnsavedChanges(state));
        };

        // Check changes initially and every time the component updates
        checkForChanges();

        // Set an interval to check periodically (every 1s)
        const interval = setInterval(checkForChanges, 1000);

        return () => clearInterval(interval);
    }, [ state ]);

    async function save() {
        if (isFetching) 
            return;

        setIsFetching(true);

        try {
            await state.evocat.update(async edit => {
                const response = await api.schemas.updateCategory({ id: state.evocat.category.id }, edit);
                if (!response.status) 
                    throw new Error(typeof response.error === 'string' ? response.error : 'Failed to save changes');
                return onSuccess(response, fromServer => Category.fromServer(fromServer));
            });
            setHasUnsavedChanges(false);
        }
        catch (err) {
            toast.error('Failed to save changes', { autoClose: 5000 });
            console.error('Save Error: Failed to save changes');
        }
        finally {
            setIsFetching(false);
        }
    }

    return (
        <div
            id='save-button' // id for triggering via Ctrl+S
            className='flex items-center gap-1 text-gray-600 hover:text-gray-800 cursor-pointer relative'
            onClick={save}
            title='Save Changes (Ctrl+S)'
        >
            {isFetching ? (
                <FaSpinner className='animate-spin' size={18} />
            ) : (
                <FaSave size={18} />
            )}
            {hasUnsavedChanges && !isFetching && (
                <span className='text-red-500 text-sm absolute -top-2 right-0'>*</span>
            )}
        </div>
    );
}

// Function to detect unsaved changes: node movement, schema updates
function detectUnsavedChanges(state: StateDispatchProps['state']) {
    const evocat = state.evocat;

    const hasSchemaChanges = evocat.uncommitedOperations.hasUnsavedChanges();

    const hasMovedNodes = evocat.category.getObjexes().some(objex => {
        return !isPositionEqual(objex.metadata.position, objex.originalMetadata.position);
    });

    return hasSchemaChanges || hasMovedNodes;
}