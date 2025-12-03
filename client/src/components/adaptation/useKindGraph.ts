import { type Dispatch, useCallback, useState } from 'react';
import { FreeSelection } from '../graph/selection';
import { type GraphMoveEvent } from '../graph/graphEngine';

/** @deprecated */
export function useKindGraph() {
    const [ selection, setSelection ] = useState(FreeSelection.create());

    const dispatch = useCallback((action: GraphMoveEvent | SelectionAction) => {
        if (action.type === 'selection' && action.selection)
            setSelection(action.selection);
    }, []);

    return {
        selection,
        dispatch,
    };
}

/** @deprecated */
export type UseKindGraphDispatch = Dispatch<GraphMoveEvent | SelectionAction>;

type SelectionAction = {
    type: 'selection';
    selection: FreeSelection;
};
