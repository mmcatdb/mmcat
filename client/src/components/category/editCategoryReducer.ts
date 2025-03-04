import { type Dispatch } from 'react';
import { FreeSelection, type FreeSelectionAction } from '../graph/graphSelection';
import { type CategoryGraph, categoryToGraph } from './categoryGraph';
import { type Evocat } from '@/types/evocat/Evocat';
import { type GraphEvent } from '../graph/graphEngine';

export type EditCategoryState = {
    /** Immutable. */
    evocat: Evocat;
    graph: CategoryGraph;
    selection: FreeSelection;
    phase: EditorPhase;
};

export function createInitialState(evocat: Evocat): EditCategoryState {
    return {
        evocat,
        graph: categoryToGraph(evocat.category),
        selection: FreeSelection.create(),
        phase: EditorPhase.default,
    };
}

export type EditCategoryDispatch = Dispatch<EditCategoryAction>;

type EditCategoryAction = GraphAction | SelectAction | CancelCreationAction | CreateObjexAction | CreateMorphismAction | PhaseAction;

export function editCategoryReducer(state: EditCategoryState, action: EditCategoryAction): EditCategoryState {
    console.log('REDUCE', state.phase, action, state);

    switch (action.type) {
    case 'graph': return graph(state, action);
    case 'select': return select(state, action);
    case 'phase': return phase(state, action);
    case 'cancelCreation': return cancelCreation(state);
    case 'createObjex': return afterObjexCreation(state, action);
    case 'createMorphism': return afterMorphismCreation(state, action);
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
        const node = state.graph.nodes.get(event.nodeId);
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
            selection: state.selection.updateFromGraphEvent(event),
        };
    }
    }
}

// Selection

type SelectAction = {
    type: 'select';
} & FreeSelectionAction;

function select(state: EditCategoryState, action: SelectAction): EditCategoryState {
    const newSelection = state.selection.updateFromAction(action);

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
    /** The graph state should be updated by this value. */
    graph?: CategoryGraph;
}

function phase(state: EditCategoryState, { phase, graph }: PhaseAction): EditCategoryState {
    const updatedGraph = graph ?? state.graph;

    return {
        ...state,
        graph: updatedGraph,
        selection: state.selection.updateFromGraph(updatedGraph),
        phase,
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
        selection: FreeSelection.create(),
        phase: EditorPhase.default,
    };
}

// Operations on schema object (objex)

type CreateObjexAction = {
    type: 'createObjex';
    graph: CategoryGraph; // Already processed graph
};

function afterObjexCreation(state: EditCategoryState, { graph }: CreateObjexAction): EditCategoryState {
    const latestObjex = Array.from(state.evocat.category.objexes.values()).pop();

    return {
        ...state,
        graph,
        selection: latestObjex
            ? FreeSelection.create([ latestObjex.schema.key.toString() ], [])
            : FreeSelection.create(),
        phase: EditorPhase.default,
    };
}

// Operations on morphism

type CreateMorphismAction = {
    type: 'createMorphism';
    graph: CategoryGraph;
};

function afterMorphismCreation(state: EditCategoryState, { graph }: CreateMorphismAction): EditCategoryState {
    const latestMorphism = Array.from(state.evocat.category.morphisms.values()).pop();

    return {
        ...state,
        graph,
        selection: latestMorphism
            ? FreeSelection.create([], [ latestMorphism.schema.signature.toString() ])
            : FreeSelection.create(),
        phase: EditorPhase.default,
    };
}
