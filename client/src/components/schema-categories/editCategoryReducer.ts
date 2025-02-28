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

type EditCategoryAction = GraphAction | SelectAction | PhaseAction | CancelCreationAction;

export function editCategoryReducer(state: EditCategoryState, action: EditCategoryAction): EditCategoryState {
    console.log('REDUCE', state.phase, action, state);

    switch (action.type) {
    case 'graph': return graph(state, action);
    case 'select': return select(state, action);
    case 'phase': return phase(state, action);
    case 'cancelCreation': return cancelCreation(state);
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
    const newSelection = updateSelectionFromUserAction(state.selection, action);

    // If we are in morphism creation phase, prevent selecting more than 2 nodes, just proceed and prevent selecting morphisms
    if (state.phase === EditorPhase.createMorphism) {
        // Prevent selecting more than 2 nodes
        const selectedNodeIds = Array.from(newSelection.nodeIds);
        if (selectedNodeIds.length > 2) 
            return state;

        if (newSelection.edgeIds.size > 0) 
            return state;
    }

    // Automatically proceed when exactly two nodes are selected
    const newPhase = (state.phase === EditorPhase.createMorphism && newSelection.nodeIds.size === 2)
        ? EditorPhase.createMorphism
        : state.phase;

    return {
        ...state,
        selection: newSelection,
        phase: newPhase,
    };
}

// Editor phases

export enum EditorPhase {
    default = 'default',
    createObjex = 'createObjex',   // Enter create schema object phase
    createMorphism = 'createMorphism',   // Enter create morphism phase
}

export type PhaseAction = {
    type: 'phase';
    /** The phase we want to switch to. */
    phase: EditorPhase;
    /** If defined, the graph state should be updated by this value. */
    graph?: CategoryGraph;
}

function phase(state: EditCategoryState, { phase, graph }: PhaseAction): EditCategoryState {
    const updatedGraph = graph ?? state.graph;

    if (phase === EditorPhase.default) {
        if (state.phase === EditorPhase.createObjex) 
            return handleObjectCreation(state);
        

        if (state.phase === EditorPhase.createMorphism) 
            return handleMorphismCreation(state);
    }

    return {
        ...state,
        graph: updatedGraph,
        selection: updateSelectionFromGraph(state.selection, updatedGraph),
        phase,
    };
}

function handleObjectCreation(state: EditCategoryState): EditCategoryState {
    const latestObjex = Array.from(state.evocat.category.objexes.values()).pop();

    return {
        ...state,
        graph: categoryToGraph(state.evocat.category),
        selection: latestObjex
            ? { nodeIds: new Set([ latestObjex.schema.key.toString() ]), edgeIds: new Set() }
            : createDefaultGraphSelection(),
        phase: EditorPhase.default,
    };
}

function handleMorphismCreation(state: EditCategoryState): EditCategoryState {
    const latestMorphism = Array.from(state.evocat.category.morphisms.values()).pop();

    return {
        ...state,
        graph: categoryToGraph(state.evocat.category),
        selection: latestMorphism
            ? { nodeIds: new Set(), edgeIds: new Set([ latestMorphism.schema.signature.toString() ]) }
            : createDefaultGraphSelection(),
        phase: EditorPhase.default,
    };
}

// Cancel creation

type CancelCreationAction = {
    type: 'cancelCreation';
};

function cancelCreation(state: EditCategoryState): EditCategoryState {
    return {
        ...state,
        graph: categoryToGraph(state.evocat.category),
        selection: createDefaultGraphSelection(),
        phase: EditorPhase.default,
    };
}
