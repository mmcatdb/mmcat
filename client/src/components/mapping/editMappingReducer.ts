import { type Dispatch } from 'react';
import { type GraphSelection, type UserSelectAction, createDefaultGraphSelection, updateSelectionFromGraphEvent, updateSelectionFromUserAction } from '../graph/graphSelection';
import { type CategoryGraph, categoryToGraph } from '../category/categoryGraph';
import { type GraphEvent } from '../graph/graphEngine';
import { type Category } from '@/types/schema';
import { type Mapping } from '@/types/mapping';

export type EditMappingState = {
    graph: CategoryGraph;
    selection: GraphSelection;
    mapping: Mapping;
    // phase: EditorPhase;
};

export function createInitialState({ category, mapping }: { category: Category, mapping: Mapping }): EditMappingState {
    return {
        graph: categoryToGraph(category),
        selection: createDefaultGraphSelection(),
        mapping,
        // phase: EditorPhase.default,
    };
}

export type EditMappingDispatch = Dispatch<EditMappingAction>;

// type EditMappingAction = GraphAction | SelectAction | PhaseAction;
type EditMappingAction = GraphAction | SelectAction;

export function editMappingReducer(state: EditMappingState, action: EditMappingAction): EditMappingState {
    console.log('REDUCE', action, state);

    switch (action.type) {
    case 'graph': return graph(state, action);
    case 'select': return select(state, action);
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

        // const node = state.graph.nodes.find(node => node.id === event.nodeId);
        // if (!node)
        //     return state;

        // state.evocat.updateObjex(node.schema.key, { position: event.position });

        // return {
        //     ...state,
        //     graph: categoryToGraph(state.evocat.category),
        // };
    }
    case 'select': {
        return {
            ...state,
            selection: updateSelectionFromGraphEvent(state.selection, event),
        };
    }
    }
}

// Selection

type SelectAction = {
    type: 'select';
} & UserSelectAction;

function select(state: EditMappingState, action: SelectAction): EditMappingState {
    return {
        ...state,
        selection: updateSelectionFromUserAction(state.selection, action),
    };
}

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
//         selection: updateSelectionFromGraph(state.selection, graph),
//         phase,
//     };
// }
