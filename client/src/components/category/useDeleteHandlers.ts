import { useEffect, useCallback } from 'react';
import { type EditCategoryState, EditorPhase, type EditCategoryDispatch } from '@/components/category/editCategoryReducer';
import { type CategoryNode, type CategoryEdge, categoryToGraph } from '@/components/category/categoryGraph';

export function useDeleteHandlers(state: EditCategoryState, dispatch: EditCategoryDispatch) {
    const deleteObjex = useCallback((node: CategoryNode) => {
        state.evocat.deleteObjex(node.schema.key);
        const graph = categoryToGraph(state.evocat.category);
        dispatch({ type: 'phase', phase: EditorPhase.default, graph });
    }, [ state, dispatch ]);

    const deleteSelectedMorphism = useCallback((morphism: CategoryEdge) => {
        state.evocat.deleteMorphism(morphism.schema.signature);
        const graph = categoryToGraph(state.evocat.category);
        dispatch({ type: 'phase', phase: EditorPhase.default, graph });
    }, [ state, dispatch ]);

    useEffect(() => {
        function handleKeyDown(event: KeyboardEvent) {
            if (event.key === 'Delete') {
                const singleSelectedNode = (state.selection.nodeIds.size === 1 && state.selection.edgeIds.size === 0)
                    ? state.graph.nodes.find(node => state.selection.nodeIds.has(node.id))
                    : undefined;

                const singleSelectedMorphism = (state.selection.edgeIds.size === 1 && state.selection.nodeIds.size === 0)
                    ? state.graph.edges.find(edge => state.selection.edgeIds.has(edge.id))
                    : undefined;

                if (singleSelectedNode)
                    deleteObjex(singleSelectedNode);
                else if (singleSelectedMorphism)
                    deleteSelectedMorphism(singleSelectedMorphism);
            }
        }

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [ state.selection, deleteObjex, deleteSelectedMorphism ]);

    return { deleteObjex, deleteSelectedMorphism };
}
