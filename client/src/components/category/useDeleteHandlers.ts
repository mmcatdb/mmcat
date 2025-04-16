import { useEffect, useCallback } from 'react';
import { type EditCategoryState, type EditCategoryDispatch } from '@/components/category/editCategoryReducer';
import { categoryToGraph } from '@/components/category/categoryGraph';

/**
 * Hook to handle deletion of selected elements via keyboard or programmatic trigger.
 */
export function useDeleteHandlers(state: EditCategoryState, dispatch: EditCategoryDispatch) {
    const deleteSelectedElements = useCallback(() => {
        let updated = false;

        state.selection.edgeIds.forEach(edgeId => {
            const edge = state.graph.edges.get(edgeId);
            if (edge) {
                state.evocat.deleteMorphism(edge.schema.signature);
                updated = true;
            }
        });

        state.selection.nodeIds.forEach(nodeId => {
            const node = state.graph.nodes.get(nodeId);
            if (node) {
                state.evocat.deleteObjex(node.schema.key);
                updated = true;
            }
        });

        // If something was deleted, update the graph state
        if (updated) {
            const graph = categoryToGraph(state.evocat.category);
            dispatch({ type: 'deleteElements', graph });
        }
    }, [ state, dispatch ]);

    // Set up keyboard listener for Delete key
    useEffect(() => {
        function handleKeyDown(event: KeyboardEvent) {
            if (event.key === 'Delete') 
                deleteSelectedElements();
        }

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [ deleteSelectedElements ]);

    return { deleteSelectedElements };
}
