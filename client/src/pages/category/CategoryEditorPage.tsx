import { useCallback, useEffect, useReducer, useRef, useState } from 'react';
import { api } from '@/api';
import { Category, isPositionEqual } from '@/types/schema';
import { SchemaUpdate } from '@/types/schema/SchemaUpdate';
import { type Params, useLoaderData } from 'react-router-dom';
import { EditCategoryGraphDisplay } from '@/components/category/EditCategoryGraphDisplay';
import { FaSpinner, FaTrash } from 'react-icons/fa6';
import { createInitialState, type EditCategoryDispatch, editCategoryReducer, EditorPhase, type EditCategoryState } from '@/components/category/editCategoryReducer';
import { Evocat } from '@/types/evocat/Evocat';
import { PhasedEditor } from '@/components/category/PhasedCategoryEditor';
import { onSuccess } from '@/types/api/result';
import { cn } from '@/components/utils';
import { TbLayoutSidebarFilled, TbLayoutSidebarRightFilled } from 'react-icons/tb';
import { FaSave } from 'react-icons/fa';
import { toast } from 'react-toastify';
import { type FreeSelectionAction } from '@/components/graph/graphSelection';
import { SelectionCard } from '@/components/category/SelectionCard';
import { useDeleteHandlers } from '@/components/category/useDeleteHandlers';
import { type Signature } from '@/types/identifiers';
import { categoryToGraph } from '@/components/category/categoryGraph';

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

    const userSelectionDispatch = useCallback((action: FreeSelectionAction) => dispatch({ type: 'select', ...action }), [ dispatch ]);

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
            <div className='h-8 flex items-center justify-between px-4 shadow-md bg-default-100 border-b border-default-200'>
                <div className='flex items-center gap-3'>
                    {/* Left Sidebar Toggle */}
                    <TbLayoutSidebarFilled
                        className='cursor-pointer text-default-600 hover:text-default-700'
                        onClick={() => toggleSidebar('left')}
                        title='Toggle Main Editor Sidebar'
                        size={18}
                    />
                </div>

                {/* Save and Right Sidebar Toggle */}
                <div className='flex items-center gap-2'>
                    {/* Delete Button */}
                    <FaTrash
                        className={`text-danger-400 ${state.selection.nodeIds.size === 0 && state.selection.edgeIds.size === 0 
                            ? 'opacity-50 cursor-auto' 
                            : 'cursor-pointer hover:text-danger-500'}`}
                        onClick={() => {
                            if (state.selection.nodeIds.size > 0 || state.selection.edgeIds.size > 0) 
                                deleteSelectedElements(state, dispatch);
                        }}
                        title='Delete (Delete)'
                        size={16}
                    />

                    {/* Divider */}
                    <div className='w-px bg-default-400 h-5 mx-2'></div>

                    <SaveButton state={state} />

                    {/* Divider */}
                    <div className='w-px bg-default-400 h-5 mx-2'></div>

                    {/* Right Sidebar Toggle */}
                    <TbLayoutSidebarRightFilled
                        className='cursor-pointer text-default-600 hover:text-default-700'
                        onClick={() => toggleSidebar('right')}
                        title='Toggle Info Sidebar'
                        size={18}
                    />
                </div>
            </div>

            <div className='flex flex-grow'>
                {/* Left Sidebar */}
                <div className={cn(`transition-all duration-300 ${sidebarState.left ? 'w-56' : 'w-0'} overflow-hidden bg-default-50`)}>
                    {sidebarState.left && <PhasedEditor state={state} dispatch={dispatch} />}
                </div>

                {/* Main Canvas */}
                <div className='flex-grow relative'>
                    <EditCategoryGraphDisplay state={state} dispatch={dispatch} className='w-full h-full' />
                </div>

                {/* Right Sidebar */}
                <div className={`transition-all duration-300 ${sidebarState.right ? 'w-60' : 'w-0'} overflow-hidden bg-default-50`}>
                    {sidebarState.right && <SelectionCard state={state} dispatch={userSelectionDispatch} />}
                </div>
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

function SaveButton({ state }: Readonly<{ state: EditCategoryState }>) {
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
            className='flex items-center gap-1 text-default-600 hover:text-default-800 cursor-pointer relative'
            onClick={save}
            title='Save Changes (Ctrl+S)'
        >
            {isFetching ? (
                <FaSpinner className='animate-spin' size={18} />
            ) : (
                <FaSave size={18} />
            )}
            {hasUnsavedChanges && !isFetching && (
                <span className='text-danger-500 text-sm absolute -top-2 right-0'>*</span>
            )}
        </div>
    );
}

function deleteSelectedElements(state: EditCategoryState, dispatch: EditCategoryDispatch) {
    // Delete all selected morphisms
    for (const edgeId of state.selection.edgeIds) {
        const morphism = state.graph.edges.get(edgeId);
        if (morphism) 
            state.evocat.deleteMorphism(morphism.schema.signature as Signature);
    }
        
    // Delete all selected nodes
    for (const nodeId of state.selection.nodeIds) {
        const node = state.graph.nodes.get(nodeId as string);
        if (node) 
            state.evocat.deleteObjex(node.schema.key);
    }
    
    // Update the graph state
    const graph = categoryToGraph(state.evocat.category);
    dispatch({ type: 'phase', phase: EditorPhase.default, graph });
}

// Function to detect unsaved changes: node movement, schema updates
function detectUnsavedChanges(state: EditCategoryState) {
    const evocat = state.evocat;

    const hasSchemaChanges = evocat.uncommitedOperations.hasUnsavedChanges();

    const hasMovedNodes = evocat.category.getObjexes().some(objex => {
        return !isPositionEqual(objex.metadata.position, objex.originalMetadata.position);
    });

    return hasSchemaChanges || hasMovedNodes;
}
