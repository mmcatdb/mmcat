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
    leftPanelMode: LeftPanelMode;
    rightPanelMode: RightPanelMode;
};

export function createInitialState(evocat: Evocat): EditCategoryState {
    return {
        evocat,
        graph: categoryToGraph(evocat.category),
        selection: FreeSelection.create(),
        leftPanelMode: LeftPanelMode.default,
        rightPanelMode: RightPanelMode.default,
    };
}

export type EditCategoryDispatch = Dispatch<EditCategoryAction>;

type EditCategoryAction = GraphAction | SelectAction | LeftPanelAction | RightPanelAction | CreateObjexAction | CreateMorphismAction | DeleteElementsAction;

export function editCategoryReducer(state: EditCategoryState, action: EditCategoryAction): EditCategoryState {
    // console.log('REDUCE', state.leftPanelMode, action, state);

    switch (action.type) {
    case 'graph': return graph(state, action);
    case 'select': return select(state, action);
    case 'leftPanelMode': return setLeftPanel(state, action);
    case 'rightPanelMode': return setRightPanel(state, action);
    case 'createObjex': return afterObjexCreation(state, action);
    case 'createMorphism': return afterMorphismCreation(state, action);
    case 'deleteElements': return afterElementsDeletion(state, action);
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

    // If we are in morphism creation mode, prevent selecting more than 2 nodes, just proceed and prevent selecting morphisms
    if (state.leftPanelMode === LeftPanelMode.createMorphism) {
        // Prevent selecting more than 2 nodes
        const selectedNodeIds = Array.from(newSelection.nodeIds);
        if (selectedNodeIds.length > 2)
            return state;

        if (newSelection.edgeIds.size > 0)
            return state;
    }

    return {
        ...state,
        selection: newSelection,
    };
}

// Editor modes
// Left panel

export enum LeftPanelMode {
    default = 'default',
    createObjex = 'createObjex',   // Enter create schema object mode
    createMorphism = 'createMorphism',   // Enter create morphism mode
}

export type LeftPanelAction = {
    type: 'leftPanelMode';
    /** The mode we want to switch to. */
    mode: LeftPanelMode;
    /** The graph state should be updated by this value. */
    graph?: CategoryGraph;
}

function setLeftPanel(state: EditCategoryState, { mode: leftPanelMode, graph }: LeftPanelAction): EditCategoryState {
    const updatedGraph = graph ?? state.graph;

    return {
        ...state,
        graph: updatedGraph,
        selection: state.selection.updateFromGraph(updatedGraph),
        leftPanelMode,
    };
}

// Right panel

export enum RightPanelMode {
    default = 'default',
    updateObjex = 'updateObjex',   // Enter update schema object mode
    updateMorphism = 'updateMorphism',   // Enter update morphism mode
}

export type RightPanelAction = {
    type: 'rightPanelMode';
    /** The mode we want to switch to. */
    mode: RightPanelMode;
    /** The graph state should be updated by this value. */
    graph?: CategoryGraph;
}

function setRightPanel(state: EditCategoryState, { mode: rightPanelMode, graph }: RightPanelAction): EditCategoryState {
    const updatedGraph = graph ?? state.graph;

    return {
        ...state,
        graph: updatedGraph,
        selection: state.selection.updateFromGraph(updatedGraph),
        rightPanelMode,
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
        leftPanelMode: LeftPanelMode.default,
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
        leftPanelMode: LeftPanelMode.default,
    };
}

// Handle selection after deleting selected schema object(s) and morphism(s)

type DeleteElementsAction = {
    type: 'deleteElements';
    graph: CategoryGraph;
};

function afterElementsDeletion(state: EditCategoryState, { graph }: DeleteElementsAction): EditCategoryState {
    return {
        ...state,
        graph: graph,
        selection: state.selection.updateFromGraph(graph),
    };
}