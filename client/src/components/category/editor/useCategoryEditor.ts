import { useReducer, useRef, type Dispatch } from 'react';
import { FreeSelection, type CategoryGraphSelection } from '../graph/selection';
import { type CategoryGraph, categoryToGraph, getEdgeId, getNodeId, getNodeKey } from '../graph/categoryGraph';
import { Evocat } from '@/types/evocat/Evocat';
import { useLoaderData } from 'react-router-dom';
import { useDeleteHandlers } from './useDeleteHandlers';
import { type CategoryEditorLoaderData } from '@/pages/category/CategoryEditorPage';
import { type Key, type Signature } from '@/types/identifiers';
import { type GraphMoveEvent } from '@/components/graph/graphEngine';

export function useCategoryEditor() {
    const loaderData = useLoaderData() as CategoryEditorLoaderData;

    // A non-reactive reference to the Evocat instance. It's used for handling events. None of its properties should be used in React directly!
    const evocatRef = useRef<Evocat>();
    if (!evocatRef.current)
        evocatRef.current = new Evocat(loaderData.category, loaderData.updates);

    const [ state, dispatch ] = useReducer(categoryEditorReducer, evocatRef.current, createInitialState);
    useDeleteHandlers(state, dispatch);

    return { state, dispatch };
}

export type CategoryEditorState = {
    /** Immutable category data model. */
    evocat: Evocat;
    /** The graph representation of the category for rendering. */
    graph: CategoryGraph;
    selection: CategoryGraphSelection | undefined;
    /** Id of the component who owns the current selection. */
    selectionKey?: string;
    /**
     * The mode of the left panel (e.g., creating objexes or morphisms).
     * @deprecated Should be handled in the panel itself, probably.
     */
    leftPanelMode: EditorPhase;
    form?: CategoryEditorForm;
};

function createInitialState(evocat: Evocat): CategoryEditorState {
    return {
        evocat,
        graph: categoryToGraph(evocat.category),
        selection: FreeSelection.create(),
        leftPanelMode: EditorPhase.default,
    };
}

export type CategoryEditorDispatch = Dispatch<CategoryEditorAction>;

type CategoryEditorAction =
    | GraphMoveEvent
    | SelectionAction
    | PhaseAction
    | FormAction
    | ObjexAction
    | MorphismAction
    | DeleteElementsAction;

function categoryEditorReducer(state: CategoryEditorState, action: CategoryEditorAction): CategoryEditorState {
    // console.log('REDUCE', state.leftPanelMode, action, state);

    switch (action.type) {
    case 'move': return move(state, action);
    case 'selection': return selection(state, action);
    case 'phase': return editorPhase(state, action);
    case 'form': return form(state, action);
    case 'objex': return objex(state, action);
    case 'morphism': return morphism(state, action);
    case 'deleteElements': return deleteElements(state);
    }
}

function move(state: CategoryEditorState, event: GraphMoveEvent): CategoryEditorState {
    const key = getNodeKey(event.nodeId);
    // Update node position in the category model
    state.evocat.updateObjex(key, { position: event.position });

    // Rebuild graph to reflect position changes
    return {
        ...state,
        graph: categoryToGraph(state.evocat.category),
    };
}

type SelectionAction = {
    type: 'selection';
    selection: CategoryGraphSelection | undefined;
    selectionKey?: string;
};

function selection(state: CategoryEditorState, { selection, selectionKey }: SelectionAction): CategoryEditorState {
    if (!selection) {
        if (state.selectionKey !== selectionKey)
            // Only the component that owns the selection can clear it. Howevever, starting a new selection is fine.
            return state;

        selectionKey = undefined;
    }
    else {
        // Some graph events don't respect the selectionKey - in that case, we continue with the previous one.
        selectionKey = selectionKey ?? state.selectionKey;
    }

    if (state.leftPanelMode === EditorPhase.createMorphism && selection instanceof FreeSelection) {
        // Restrict selection during morphism creation to max 2 nodes and no edges.
        const selectedNodeIds = [ ...selection.nodeIds ];
        if (selectedNodeIds.length > 2 || selection.edgeIds.size > 0)
            selection = FreeSelection.create(selectedNodeIds.slice(0, 2), []);
    }

    return { ...state, selection, selectionKey };
}

export enum EditorPhase {
    default = 'default',
    createObjex = 'createObjex',
    createMorphism = 'createMorphism',
}

type PhaseAction = {
    /** The type of action, indicating a left panel mode change. */
    type: 'phase';
    /** The new mode for the left panel. */
    phase: EditorPhase;
    /** Optional updated graph to synchronize state. */
    graph?: CategoryGraph;
};

function editorPhase(state: CategoryEditorState, { phase, graph }: PhaseAction): CategoryEditorState {
    const updatedGraph = graph ?? state.graph;
    let selection = state.selection;

    // Clear selection when entering createMorphism mode
    if (phase === EditorPhase.createMorphism || !(selection instanceof FreeSelection))
        selection = FreeSelection.create();
    else
        selection = selection.updateFromGraph(updatedGraph);

    return {
        ...state,
        graph: updatedGraph,
        selection,
        leftPanelMode: phase,
    };
}

// #region Form

export type CategoryEditorForm = {
    // TODO maybe use enum
    type: 'objex';
    key: Key;
} | {
    type: 'morphism';
    signature: Signature;
};

type FormAction = {
    type: 'form';
} & ({
    operation: 'objex';
    key: Key;
} | {
    operation: 'morphism';
    signature: Signature;
} | {
    operation: 'cancel';
});

function form(state: CategoryEditorState, action: FormAction): CategoryEditorState {
    if (action.operation === 'objex') {
        return {
            ...state,
            form: {
                type: 'objex',
                key: action.key,
            },
            selection: undefined,
        };
    }

    if (action.operation === 'morphism') {
        return {
            ...state,
            form: {
                type: 'morphism',
                signature: action.signature,
            },
            selection: undefined,
        };
    }

    if (!state.form)
        return state;

    // Cancel action.
    const selection = state.form?.type === 'objex'
        ? FreeSelection.create([ getNodeId(state.form.key) ], [])
        : FreeSelection.create([], [ getEdgeId(state.form.signature) ]);

    return { ...state, form: undefined, selection, selectionKey: undefined };
}

// #endregion

type ObjexAction = {
    type: 'objex';
    selectKey?: Key;
};

function objex(state: CategoryEditorState, { selectKey }: ObjexAction): CategoryEditorState {
    const graph = categoryToGraph(state.evocat.category);
    const selection = selectKey
        ? FreeSelection.create([ getNodeId(selectKey) ], [])
        : FreeSelection.create();

    return {
        ...state,
        graph,
        selection,
        selectionKey: undefined,
        form: undefined,
    };
}

type MorphismAction = {
    type: 'morphism';
    selectSignature?: Signature;
};

function morphism(state: CategoryEditorState, { selectSignature }: MorphismAction): CategoryEditorState {
    const graph = categoryToGraph(state.evocat.category);
    const selection = selectSignature
        ? FreeSelection.create([], [ getEdgeId(selectSignature) ])
        : FreeSelection.create();

    return {
        ...state,
        graph,
        selection,
        form: undefined,
    };
}

type DeleteElementsAction = {
    type: 'deleteElements';
};

function deleteElements(state: CategoryEditorState): CategoryEditorState {
    const graph = categoryToGraph(state.evocat.category);
    return {
        ...state,
        graph,
        selection: state.selection instanceof FreeSelection ? state.selection.updateFromGraph(graph) : FreeSelection.create(),
        selectionKey: undefined,
    };
}
