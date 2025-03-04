import { type Dispatch } from 'react';
import { FreeSelection, type FreeSelectionAction, PathSelection, type PathSelectionAction, SelectionType, SequenceSelection, type SequenceSelectionAction } from '../graph/graphSelection';
import { type CategoryGraph, categoryToGraph } from '../category/categoryGraph';
import { type GraphEvent } from '../graph/graphEngine';
import { type Category } from '@/types/schema';
import { type Mapping } from '@/types/mapping';

export type EditMappingState = {
    category: Category;
    graph: CategoryGraph;
    mapping: Mapping;
    // phase: EditorPhase;
    selectionType: SelectionType;
    selection: FreeSelection | SequenceSelection | PathSelection;
};

export function createInitialState({ category, mapping }: { category: Category, mapping: Mapping }): EditMappingState {
    return {
        category,
        graph: categoryToGraph(category),
        // TODO This should depened on the selection class.
        selectionType: SelectionType.Free,
        selection: FreeSelection.create(),
        mapping,
        // phase: EditorPhase.default,
    };
}

export type EditMappingDispatch = Dispatch<EditMappingAction>;

// type EditMappingAction = GraphAction | SelectAction | PhaseAction;
type EditMappingAction = GraphAction | SelectAction | SequenceAction | PathAction | TempSelectionTypeAction;

export function editMappingReducer(state: EditMappingState, action: EditMappingAction): EditMappingState {
    console.log('REDUCE', action, state);

    switch (action.type) {
    case 'graph': return graph(state, action);
    case 'select': return select(state, action);
    case 'sequence': return sequence(state, action);
    case 'path': return path(state, action);
    case 'selection-type': {
        const { selectionType } = action;
        if (selectionType === SelectionType.Free)
            return { ...state, selectionType, selection: FreeSelection.create() };
        if (selectionType === SelectionType.Sequence)
            return { ...state, selectionType, selection: SequenceSelection.create() };
        if (selectionType === SelectionType.Path)
            return { ...state, selectionType, selection: PathSelection.create() };

        return { ...state, selectionType: action.selectionType };
    }
    // case 'phase': return phase(state, action);
    }
}

// Low-level graph library events

type GraphAction = {
    type: 'graph';
    event: GraphEvent;
};

function graph(state: EditMappingState, { event }: GraphAction): EditMappingState {
    switch (event.type) {
    case 'move': {
        // TODO This is not supported, alghough it should be. Probably would require a new way how to handle metadata ...
        return state;

        // const node = state.graph.nodes.get(event.nodeId);
        // if (!node)
        //     return state;

        // state.evocat.updateObjex(node.schema.key, { position: event.position });

        // return {
        //     ...state,
        //     graph: categoryToGraph(state.evocat.category),
        // };
    }
    case 'select': {
        if (!(state.selection instanceof FreeSelection))
            return state;

        return {
            ...state,
            selection: state.selection.updateFromGraphEvent(event),
        };
    }
    }
}

// Selection

type SelectAction = {
    type: 'select';
} & FreeSelectionAction;

function select(state: EditMappingState, action: SelectAction): EditMappingState {
    if (!(state.selection instanceof FreeSelection))
        return state;

    return {
        ...state,
        selection: state.selection.updateFromAction(action),
    };
}

type SequenceAction = {
    type: 'sequence';
} & SequenceSelectionAction;

function sequence(state: EditMappingState, action: SequenceAction): EditMappingState {
    if (!(state.selection instanceof SequenceSelection))
        return state;

    return {
        ...state,
        selection: state.selection.updateFromAction(action),
    };
}

type PathAction = {
    type: 'path';
} & PathSelectionAction;

function path(state: EditMappingState, action: PathAction): EditMappingState {
    if (!(state.selection instanceof PathSelection))
        return state;

    return {
        ...state,
        selection: state.selection.updateFromAction(action),
    };
}

/** @deprecated */
type TempSelectionTypeAction = {
    type: 'selection-type';
    selectionType: SelectionType;
};

// TODO This

// Editor phases

// export enum EditorPhase {
//     default = 'default',
// }

// export type PhaseAction = {
//     type: 'phase';
//     /** The phase we want to switch to. */
//     phase: EditorPhase;
//     /** If defined, the graph state should be updated by this value. */
//     graph?: CategoryGraph;
// }

// function phase(state: EditMappingState, { phase, graph }: PhaseAction): EditMappingState {
//     if (!graph)
//         return { ...state, phase };

//     return {
//         ...state,
//         graph,
//         selection: state.selection.updateFromGraph(graph),
//         phase,
//     };
// }
