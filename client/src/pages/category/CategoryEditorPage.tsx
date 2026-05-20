import { useEffect, useMemo, useState } from 'react';
import { api } from '@/api';
import { Category, isPositionEqual } from '@/types/schema';
import { type Params } from 'react-router-dom';
import { FaTrash } from 'react-icons/fa6';
import { type CategoryEditorDispatch, type CategoryEditorState, useCategoryEditor } from '@/components/category/editor/useCategoryEditor';
import { LeftPanelCategoryEditor } from '@/components/category/editor/LeftPanelCategoryEditor';
import { RightPanelCategoryEditor } from '@/components/category/editor/RightPanelCategoryEditor';
import { TbLayoutSidebarFilled, TbLayoutSidebarRightFilled } from 'react-icons/tb';
import { SaveProvider, SaveButton } from '@/components/category/editor/SaveContext';
import { cn } from '@/components/common/utils';
import { PageLayout } from '@/components/RootLayout';
import { SchemaUpdate } from '@/types/schema/SchemaUpdate';
import { getEdgeId, getEdgeSignature, getNodeId, getNodeKey } from '@/components/category/graph/categoryGraph';
import {  CategoryGraphDisplay } from '@/components/category/graph/CategoryGraphDisplay';
import { FreeSelection } from '@/components/graph/selection';
import { GraphHighlights } from '@/components/category/graph/highlights';

type EditorSidebarState = {
    left: boolean;
    right: boolean;
};

export function CategoryEditorPage() {
    const { state, dispatch } = useCategoryEditor();

    const [ sidebarState, setSidebarState ] = useState<EditorSidebarState>({
        left: true,
        right: true,
    });

    function toggleSidebar(side: keyof EditorSidebarState) {
        setSidebarState(prev => ({
            ...prev,
            [side]: !prev[side], // toggle the specified sidebar
        }));
    }

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

    const graphHighlights = useMemo(() => {
        switch (state.form?.type) {
        case 'objex': return GraphHighlights.create([ getNodeId(state.form.key) ]);
        case 'morphism': return GraphHighlights.create([], [ getEdgeId(state.form.signature) ]);
        default: return undefined;
        }
    }, [ state.form ]);

    return (
        <PageLayout isFullscreen>
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
                                onClick={() => deleteSelectedElements(state, dispatch)}
                                disabled={!(state.selection instanceof FreeSelection) || state.selection.isEmpty}
                                title='Delete selected elements (Delete)'
                                className='p-1 transition rounded focus:outline-hidden focus-visible:ring-2 focus-visible:ring-danger-300 text-danger-400 hover:text-danger-500 hover:opacity-70 cursor-pointer disabled:opacity-50 disabled:pointer-events-none'
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

                    <div className='relative flex grow'>
                        {/* Left Sidebar */}
                        <div className={cn('transition-all duration-300 overflow-hidden bg-default-50', sidebarState.left ? 'w-56' : 'w-0')}>
                            {sidebarState.left && <LeftPanelCategoryEditor state={state} dispatch={dispatch} />}
                        </div>

                        {/* Main Canvas */}
                        <div className='grow relative'>
                            <CategoryGraphDisplay
                                graph={state.graph}
                                selection={state.selection}
                                dispatch={dispatch}
                                highlights={graphHighlights}
                                className='w-full h-full'
                            />
                        </div>

                        {/* Right Sidebar */}
                        <div className={cn('transition-all duration-300 overflow-hidden bg-default-50', sidebarState.right ? 'w-60' : 'w-0')}>
                            {sidebarState.right && <RightPanelCategoryEditor state={state} dispatch={dispatch} />}
                        </div>
                    </div>
                </div>
            </SaveProvider>
        </PageLayout>
    );
}

export type CategoryEditorLoaderData = {
    category: Category;
    updates: SchemaUpdate[];
};

CategoryEditorPage.loader = async ({ params: { categoryId } }: { params: Params<'categoryId'> }): Promise<CategoryEditorLoaderData> => {
    if (!categoryId)
        throw new Error('Category ID is required');

    // TODO show mappings
    // const [ categoryResponse, updatesResponse, datasourcesResponse, mappingsResponse ] = await Promise.all([
    const [ categoryResponse, updatesResponse ] = await Promise.all([
        api.schemas.getCategory({ id: categoryId }),
        api.schemas.getCategoryUpdates({ id: categoryId }),
        // api.datasources.getAllDatasources({}, { categoryId }),
        // api.mappings.getAllMappingsInCategory({}, { categoryId }),
    ]);

    // if (!categoryResponse.status || !updatesResponse.status || !datasourcesResponse.status || !mappingsResponse.status)
    if (!categoryResponse.status || !updatesResponse.status)
        throw new Error('Failed to load schema category');

    return {
        category: Category.fromResponse(categoryResponse.data),
        updates: updatesResponse.data.map(SchemaUpdate.fromResponse),
        // datasources: datasourcesResponse.data,
        // mappings: mappingsResponse.data,
    };
};

function deleteSelectedElements(state: CategoryEditorState, dispatch: CategoryEditorDispatch) {
    const selection = state.selection as FreeSelection;
    // Delete all selected edges (before the nodes).
    for (const edgeId of selection.edgeIds)
        state.evocat.deleteMorphism(getEdgeSignature(edgeId));

    // Delete all selected nodes.
    for (const nodeId of selection.nodeIds)
        state.evocat.deleteObjex(getNodeKey(nodeId));

    dispatch({ type: 'deleteElements' });
}

/*
 * Function to get unsaved changes: node movement, schema updates
 */
export function getUnsavedChanges(state: CategoryEditorState) {
    const evocat = state.evocat;
    const hasSchemaChanges = evocat.uncommitedOperations.hasUnsavedChanges();
    const hasMovedNodes = evocat.category.getObjexes().some(
        objex => !isPositionEqual(objex.metadata.position, objex.originalMetadata.position),
    );
    const hasRenamedNodes = evocat.category.getObjexes().some(
        objex => objex.metadata.label !== objex.originalMetadata.label,
    );
    return hasSchemaChanges || hasMovedNodes || hasRenamedNodes;
}
