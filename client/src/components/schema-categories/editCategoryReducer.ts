import { type Dispatch } from 'react';
import { type GraphSelection, type UserSelectAction, createDefaultGraphSelection, updateSelectionFromGraph, updateSelectionFromGraphEvent, updateSelectionFromUserAction } from '../graph/graphSelection';
import { type CategoryGraph, categoryToGraph } from './categoryGraph';
import { type Evocat } from '@/types/evocat/Evocat';
import { type GraphEvent } from '../graph/graphEngine';

export type EditCategoryState = {
    /** Immutable. */
    evocat: Evocat;
    graph: CategoryGraph;
    selection: GraphSelection;
    phase: EditorPhase;
};

export function createInitialState(evocat: Evocat): EditCategoryState {
    return {
        evocat,
        graph: categoryToGraph(evocat.category),
        selection: createDefaultGraphSelection(),
        phase: EditorPhase.default,
    };
}

export type EditCategoryDispatch = Dispatch<EditCategoryAction>;

type EditCategoryAction = GraphAction | SelectAction | PhaseAction;

export function editCategoryReducer(state: EditCategoryState, action: EditCategoryAction): EditCategoryState {
    console.log('REDUCE', state.phase, action, state);

    switch (action.type) {
    case 'graph': return graph(state, action);
    case 'select': return select(state, action);
    case 'phase': return phase(state, action);
    }
}

// Low-level graph library events

type GraphAction = {
    type: 'graph';
    event: GraphEvent;
};

function graph(state: EditCategoryState, { event }: GraphAction): EditCategoryState {
    switch (event.type) {
    case 'move': {
        const node = state.graph.nodes.find(node => node.id === event.nodeId);
        if (!node)
            return state;

        state.evocat.updateObjex(node.schema.key, { position: event.position });

        return {
            ...state,
            graph: categoryToGraph(state.evocat.category),
        };
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

function select(state: EditCategoryState, action: SelectAction): EditCategoryState {
    return {
        ...state,
        selection: updateSelectionFromUserAction(state.selection, action),
    };
}

// Editor phases

export enum EditorPhase {
    default = 'default',
    createObjex = 'createObjex',
}

export type PhaseAction = {
    type: 'phase';
    /** The phase we want to switch to. */
    phase: EditorPhase;
    /** If defined, the graph state should be updated by this value. */
    graph?: CategoryGraph;
}

function phase(state: EditCategoryState, { phase, graph }: PhaseAction): EditCategoryState {
    if (!graph)
        return { ...state, phase };

    return {
        ...state,
        graph,
        selection: updateSelectionFromGraph(state.selection, graph),
        phase,
    };
}
