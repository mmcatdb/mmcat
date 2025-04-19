import { type Dispatch } from 'react';
import { FreeSelection, type FreeSelectionAction } from '../graph/graphSelection';
import { type CategoryGraph, categoryToGraph } from './categoryGraph';
import { type Evocat } from '@/types/evocat/Evocat';
import { type GraphEvent } from '../graph/graphEngine';

/**
 * Represents the state for editing a category graph, encapsulating all necessary data for rendering and interaction.
 *
 * @property evocat - The immutable category data model.
 * @property graph - The graph representation of the category for rendering.
 * @property selection - The current selection state of nodes and edges.
 * @property leftPanelMode - The mode of the left panel (e.g., creating objects or morphisms).
 * @property rightPanelMode - The mode of the right panel (e.g., updating objects or morphisms).
 */
export type EditCategoryState = {
    /** Immutable category data model. */
    evocat: Evocat;
    graph: CategoryGraph;
    selection: FreeSelection;
    leftPanelMode: LeftPanelMode;
    rightPanelMode: RightPanelMode;
};

/**
 * Initializes the state for editing a category, setting up the graph and default modes.
 *
 * @param evocat - The category data model to initialize the state with.
 * @returns The initial state for editing the category.
 */
export function createInitialState(evocat: Evocat): EditCategoryState {
    return {
        evocat,
        graph: categoryToGraph(evocat.category),
        selection: FreeSelection.create(),
        leftPanelMode: LeftPanelMode.default,
        rightPanelMode: RightPanelMode.default,
    };
}

/**
 * Type alias for the dispatch function handling category edit actions.
 */
export type EditCategoryDispatch = Dispatch<EditCategoryAction>;

/**
 * Union type of all possible actions for editing the category state.
 */
type EditCategoryAction =
    | GraphAction
    | SelectAction
    | LeftPanelAction
    | RightPanelAction
    | CreateObjexAction
    | CreateMorphismAction
    | DeleteElementsAction;

/**
 * Reducer changing the category state based on the provided action, handling graph updates, selections, and mode changes.
 *
 * @param state - The current state of the category editor.
 * @param action - The action to apply to the state.
 * @returns The new state after applying the action.
 */
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

/**
 * Action for handling low-level graph events like node movement or selection.
 */
type GraphAction = {
    type: 'graph';
    event: GraphEvent;
};

/**
 * Processes graph-related events, such as moving nodes or updating selections.
 *
 * @param state - The current editor state.
 * @param action - The graph action containing the event.
 * @returns The updated state.
 */
function graph(state: EditCategoryState, { event }: GraphAction): EditCategoryState {
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
function select(state: EditCategoryState, action: SelectAction): EditCategoryState {
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

/**
 * Action to update the left panel's mode and optionally the graph state.
 *
 * @interface LeftPanelAction
 * @property mode - The new mode for the left panel.
 * @property graph - Optional updated graph to synchronize state.
 */
export type LeftPanelAction = {
    type: 'leftPanelMode';
    mode: LeftPanelMode;
    graph?: CategoryGraph;
};

/**
 * Updates the left panel mode and synchronizes the graph and selection state.
 *
 * @param state - The current editor state.
 * @param action - The left panel action with mode and optional graph.
 * @returns The updated state.
 */
function setLeftPanel(state: EditCategoryState, { mode: leftPanelMode, graph }: LeftPanelAction): EditCategoryState {
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
 * Modes for the right panel, controlling object and morphism updates.
 */
export enum RightPanelMode {
    default = 'default',
    updateObjex = 'updateObjex',
    updateMorphism = 'updateMorphism',
}

/**
 * Action to update the right panel's mode and optionally the graph state.
 *
 * @interface RightPanelAction
 * @property mode - The new mode for the right panel.
 * @property graph - Optional updated graph to synchronize state.
 */
export type RightPanelAction = {
    type: 'rightPanelMode';
    mode: RightPanelMode;
    graph?: CategoryGraph;
};

/**
 * Updates the right panel mode and synchronizes the graph and selection state.
 *
 * @param state - The current editor state.
 * @param action - The right panel action with mode and optional graph.
 * @returns The updated state.
 */
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

/**
 * Action for handling the creation of a new schema object.
 */
type CreateObjexAction = {
    type: 'createObjex';
    graph: CategoryGraph;
};

/**
 * Updates state after creating a new schema object, selecting it and resetting the mode.
 *
 * @param state - The current editor state.
 * @param action - The action with the updated graph.
 * @returns The updated state with the new object selected.
 */
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

/**
 * Action for handling the creation of a new morphism.
 */
type CreateMorphismAction = {
    type: 'createMorphism';
    graph: CategoryGraph;
};

/**
 * Updates state after creating a new morphism, selecting it and resetting the mode.
 *
 * @param state - The current editor state.
 * @param action - The action with the updated graph.
 * @returns The updated state with the new morphism selected.
 */
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

// Handle selection after deletion

/**
 * Action for handling the deletion of selected schema objects and morphisms.
 */
type DeleteElementsAction = {
    type: 'deleteElements';
    graph: CategoryGraph;
};

/**
 * Updates state after deleting elements, synchronizing the selection with the new graph.
 *
 * @param state - The current editor state.
 * @param action - The action with the updated graph.
 * @returns The updated state with synchronized selection.
 */
function afterElementsDeletion(state: EditCategoryState, { graph }: DeleteElementsAction): EditCategoryState {
    return {
        ...state,
        graph,
        selection: state.selection.updateFromGraph(graph),
    };
}