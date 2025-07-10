import { useReducer, useRef, type Dispatch } from 'react';
import { FreeSelection, type FreeSelectionAction } from '../graph/graphSelection';
import { type CategoryGraph, categoryToGraph } from './categoryGraph';
import { Evocat } from '@/types/evocat/Evocat';
import { type GraphEvent } from '../graph/graphEngine';
import { SchemaUpdate, type SchemaUpdateResponse } from '@/types/schema/SchemaUpdate';
import { useLoaderData } from 'react-router-dom';
import { Category, type SchemaCategoryResponse } from '@/types/schema';
import { useDeleteHandlers } from './useDeleteHandlers';

export function useCategoryEditor() {
    const loaderData = useLoaderData() as {
        category: SchemaCategoryResponse;
        updates: SchemaUpdateResponse[];
    };

    // A non-reactive reference to the Evocat instance. It's used for handling events. None of its properties should be used in React directly!
    const evocatRef = useRef<Evocat>();
    if (!evocatRef.current) {
        const updates = loaderData.updates.map(SchemaUpdate.fromResponse);
        const category = Category.fromResponse(loaderData.category);
        evocatRef.current = new Evocat(category, updates);
    }

    const [ state, dispatch ] = useReducer(categoryEditorReducer, evocatRef.current, createInitialState);
    useDeleteHandlers(state, dispatch);

    return { state, dispatch };
}

export type CategoryEditorState = {
    /** Immutable category data model. */
    evocat: Evocat;
    /** The graph representation of the category for rendering. */
    graph: CategoryGraph;
    /** The current selection state of nodes and edges. */
    selection: FreeSelection;
    /** The mode of the left panel (e.g., creating objexes or morphisms). */
    leftPanelMode: LeftPanelMode;
    /** The mode of the right panel (e.g., updating objexes or morphisms). */
    rightPanelMode: RightPanelMode;
};

/**
 * Initializes the state for updating a category, setting up the graph and default modes.
 */
function createInitialState(evocat: Evocat): CategoryEditorState {
    return {
        evocat,
        graph: categoryToGraph(evocat.category),
        selection: FreeSelection.create(),
        leftPanelMode: LeftPanelMode.default,
        rightPanelMode: RightPanelMode.default,
    };
}

export type CategoryEditorDispatch = Dispatch<CategoryEditorAction>;

type CategoryEditorAction =
    | GraphAction
    | SelectAction
    | LeftPanelAction
    | RightPanelAction
    | CreateObjexAction
    | CreateMorphismAction
    | DeleteElementsAction;

function categoryEditorReducer(state: CategoryEditorState, action: CategoryEditorAction): CategoryEditorState {
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

/**
 * Action for handling low-level graph events like node movement or selection.
 */
type GraphAction = {
    type: 'graph';
    event: GraphEvent;
};

/**
 * Processes graph-related events, such as moving nodes or updating selections.
 */
function graph(state: CategoryEditorState, { event }: GraphAction): CategoryEditorState {
    switch (event.type) {
    case 'move': {
        const node = state.graph.nodes.get(event.nodeId);
        if (!node)
            return state;

        // Update node position in the category model
        state.evocat.updateObjex(node.schema.key, { position: event.position });

        // Rebuild graph to reflect position changes
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
    default:
        return state;
    }
}

// Selection

/**
 * Action for updating the selection state of nodes and edges.
 */
type SelectAction = {
    type: 'select';
} & FreeSelectionAction;

/**
 * Updates the selection state, enforcing constraints like limiting node selection during morphism creation.
 *
 * @param state - The current editor state.
 * @param action - The selection action to apply.
 * @returns The updated state with new selection.
 */
function select(state: CategoryEditorState, action: SelectAction): CategoryEditorState {
    const newSelection = state.selection.updateFromAction(action);

    // Restrict selection during morphism creation to max 2 nodes and no edges
    if (state.leftPanelMode === LeftPanelMode.createMorphism) {
        const selectedNodeIds = Array.from(newSelection.nodeIds);
        // Limit to 2 nodes and no edges in createMorphism mode
        if (selectedNodeIds.length > 2 || newSelection.edgeIds.size > 0) {
            return {
                ...state,
                selection: FreeSelection.create(selectedNodeIds.slice(0, 2), []),
            };
        }
    }
    return {
        ...state,
        selection: newSelection,
    };
}

// Editor modes - Left panel

/**
 * Modes for the left panel, controlling the editor's interaction state.
 */
export enum LeftPanelMode {
    default = 'default',
    createObjex = 'createObjex',
    createMorphism = 'createMorphism',
}

type LeftPanelAction = {
    /** The type of action, indicating a left panel mode change. */
    type: 'leftPanelMode';
    /** The new mode for the left panel. */
    mode: LeftPanelMode;
    /** Optional updated graph to synchronize state. */
    graph?: CategoryGraph;
};

/**
 * Updates the left panel mode and synchronizes the graph and selection state.
 */
function setLeftPanel(state: CategoryEditorState, { mode: leftPanelMode, graph }: LeftPanelAction): CategoryEditorState {
    const updatedGraph = graph ?? state.graph;
    let newSelection = state.selection;

    // Clear selection when entering createMorphism mode
    if (leftPanelMode === LeftPanelMode.createMorphism)
        newSelection = FreeSelection.create();

    return {
        ...state,
        graph: updatedGraph,
        selection: newSelection.updateFromGraph(updatedGraph),
        leftPanelMode,
    };
}

// Editor modes - Right panel

/**
 * Modes for the right panel, controlling objex and morphism updates.
 */
export enum RightPanelMode {
    default = 'default',
    updateObjex = 'updateObjex',
    updateMorphism = 'updateMorphism',
}

/**
 * Action to update the right panel's mode and optionally the graph state.
 */
type RightPanelAction = {
    type: 'rightPanelMode';
    mode: RightPanelMode;
    graph?: CategoryGraph;
};

/**
 * Updates the right panel mode and synchronizes the graph and selection state.
 */
function setRightPanel(state: CategoryEditorState, { mode: rightPanelMode, graph }: RightPanelAction): CategoryEditorState {
    const updatedGraph = graph ?? state.graph;

    return {
        ...state,
        graph: updatedGraph,
        selection: state.selection.updateFromGraph(updatedGraph),
        rightPanelMode,
    };
}

// Operations on objex

/**
 * Action for handling the creation of a new objex.
 */
type CreateObjexAction = {
    type: 'createObjex';
    graph: CategoryGraph;
};

/**
 * Updates state after creating a new objex, selecting it and resetting the mode.
 */
function afterObjexCreation(state: CategoryEditorState, { graph }: CreateObjexAction): CategoryEditorState {
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

/**
 * Action for handling the creation of a new morphism.
 */
type CreateMorphismAction = {
    type: 'createMorphism';
    graph: CategoryGraph;
};

/**
 * Updates state after creating a new morphism, selecting it and resetting the mode.
 */
function afterMorphismCreation(state: CategoryEditorState, { graph }: CreateMorphismAction): CategoryEditorState {
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

// Handle selection after deletion

/**
 * Action for handling the deletion of selected schema objexes and morphisms.
 */
type DeleteElementsAction = {
    type: 'deleteElements';
    graph: CategoryGraph;
};

/**
 * Updates state after deleting elements, synchronizing the selection with the new graph.
 */
function afterElementsDeletion(state: CategoryEditorState, { graph }: DeleteElementsAction): CategoryEditorState {
    return {
        ...state,
        graph,
        selection: state.selection.updateFromGraph(graph),
    };
}
