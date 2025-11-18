import { useEffect, useCallback } from 'react';
import { type CategoryEditorState, type CategoryEditorDispatch } from '@/components/category/editor/useCategoryEditor';
import { getEdgeSignature, getNodeKey } from '../graph/categoryGraph';
import { FreeSelection } from '../graph/selection';

/**
 * Hook to handle deletion of selected elements via keyboard or programmatic trigger.
 */
export function useDeleteHandlers(state: CategoryEditorState, dispatch: CategoryEditorDispatch) {
    const deleteSelectedElements = useCallback(() => {
        if (!(state.selection instanceof FreeSelection))
            return;

        let updated = false;

        state.selection.edgeIds.forEach(edgeId => {
            state.evocat.deleteMorphism(getEdgeSignature(edgeId));
            updated = true;
        });

        state.selection.nodeIds.forEach(nodeId => {
            state.evocat.deleteObjex(getNodeKey(nodeId));
            updated = true;
        });

        // If something was deleted, update the graph state
        if (updated)
            dispatch({ type: 'deleteElements' });
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

    return {
        deleteSelectedElements,
    };
}
