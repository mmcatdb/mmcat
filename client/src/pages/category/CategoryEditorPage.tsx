import { useEffect, useReducer, useRef, useState } from 'react';
import { api } from '@/api';
import { Category, isPositionEqual } from '@/types/schema';
import { SchemaUpdate } from '@/types/schema/SchemaUpdate';
import { type Params, useLoaderData } from 'react-router-dom';
import { EditCategoryGraphDisplay } from '@/components/category/EditCategoryGraphDisplay';
import { FaTrash } from 'react-icons/fa6';
import { createInitialState, type EditCategoryDispatch, editCategoryReducer, type EditCategoryState } from '@/components/category/editCategoryReducer';
import { Evocat } from '@/types/evocat/Evocat';
import { LeftPanelCategoryEditor } from '@/components/category/LeftPanelCategoryEditor';
import { RightPanelCategoryEditor } from '@/components/category/RightPanelCategoryEditor';
import { cn } from '@/components/utils';
import { TbLayoutSidebarFilled, TbLayoutSidebarRightFilled } from 'react-icons/tb';
import { useDeleteHandlers } from '@/components/category/useDeleteHandlers';
import { categoryToGraph } from '@/components/category/categoryGraph';
import { SaveProvider, SaveButton } from '@/components/category/SaveContext';

type EditorSidebarState = {
    left: boolean;
    right: boolean;
};

export function CategoryEditorPage() {
    const loaderData = useLoaderData() as Awaited<ReturnType<typeof categoryEditorLoader>>;
    
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
        <SaveProvider categoryState={state}>
            <div className='flex flex-col h-[calc(100vh-40px)]'>
                {/* Navbar */}
                <div className='h-8 flex items-center justify-between px-4 bg-default-100 border-b border-default-200'>
                    {/* Left Sidebar Toggle */}
                    <button
                        onClick={() => toggleSidebar('left')}
                        title='Toggle Main Editor Sidebar'
                        className='text-default-600 hover:text-default-800'
                    >
                        <TbLayoutSidebarFilled size={20} aria-hidden='true' />
                    </button>

                    {/* Delete, Save and Right Sidebar Toggle */}
                    <div className='flex items-center gap-2'>
                        {/* Delete Button */}
                        <button
                            onClick={() => {
                                if (state.selection.nodeIds.size > 0 || state.selection.edgeIds.size > 0)
                                    deleteSelectedElements(state, dispatch);
                            }}
                            disabled={state.selection.nodeIds.size === 0 && state.selection.edgeIds.size === 0}
                            title='Delete selected elements (Delete)'
                            className={`p-1 transition rounded focus:outline-none focus-visible:ring-2 focus-visible:ring-danger-300 ${
                                state.selection.nodeIds.size === 0 && state.selection.edgeIds.size === 0
                                    ? 'text-danger-400 opacity-50 cursor-not-allowed'
                                    : 'text-danger-400 hover:text-danger-500 hover:opacity-70 cursor-pointer'
                            }`}
                        >
                            <FaTrash size={16} aria-hidden='true' />
                        </button>

                        {/* Save Button */}
                        <div className='w-px bg-default-400 h-5 mx-2'></div>
                        <SaveButton />
                        <div className='w-px bg-default-400 h-5 mx-2'></div>

                        {/* Right Sidebar Toggle */}
                        <button
                            onClick={() => toggleSidebar('right')}
                            title='Toggle Edit Sidebar'
                            className='text-default-600 hover:text-default-800'
                        >
                            <TbLayoutSidebarRightFilled size={20} aria-hidden='true' />
                        </button>
                    </div>

                </div>

                <div className='relative flex flex-grow'>
                    {/* Left Sidebar */}
                    <div className={cn(`transition-all duration-300 ${sidebarState.left ? 'w-56' : 'w-0'} overflow-hidden bg-default-50`)}>
                        {sidebarState.left && <LeftPanelCategoryEditor state={state} dispatch={dispatch} />}
                    </div>

                    {/* Main Canvas */}
                    <div className='flex-grow relative'>
                        <EditCategoryGraphDisplay state={state} dispatch={dispatch} className={cn('w-full h-full', sidebarState.left)} />
                    </div>

                    {/* Right Sidebar */}
                    <div className={`transition-all duration-300 ${sidebarState.right ? 'w-60' : 'w-0'} overflow-hidden bg-default-50`}>
                        {sidebarState.right && <RightPanelCategoryEditor state={state} dispatch={dispatch} />}
                    </div>
                </div>
            </div>
        </SaveProvider>
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

function deleteSelectedElements(state: EditCategoryState, dispatch: EditCategoryDispatch) {
    // Delete all selected morphisms
    for (const edgeId of state.selection.edgeIds) {
        const morphism = state.graph.edges.get(edgeId);
        if (morphism)
            state.evocat.deleteMorphism(morphism.schema.signature);
    }

    // Delete all selected nodes
    for (const nodeId of state.selection.nodeIds) {
        const node = state.graph.nodes.get(nodeId);
        if (node)
            state.evocat.deleteObjex(node.schema.key);
    }

    // Update the graph state
    const graph = categoryToGraph(state.evocat.category);
    dispatch({ type: 'deleteElements', graph });
}

/*
 * Function to detect unsaved changes: node movement, schema updates
 */
export function detectUnsavedChanges(state: EditCategoryState) {
    const evocat = state.evocat;
    const hasSchemaChanges = evocat.uncommitedOperations.hasUnsavedChanges();
    const hasMovedNodes = evocat.category.getObjexes().some(objex =>
        !isPositionEqual(objex.metadata.position, objex.originalMetadata.position),
    );
    const hasRenamedNodes = evocat.category.getObjexes().some(
        objex => objex.metadata.label !== objex.originalMetadata.label,
    );
    return hasSchemaChanges || hasMovedNodes || hasRenamedNodes;
}
