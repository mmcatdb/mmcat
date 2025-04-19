import { type Dispatch } from 'react';
import { FreeSelection, type FreeSelectionAction, PathSelection, type PathSelectionAction, SelectionType, SequenceSelection, type SequenceSelectionAction } from '../graph/graphSelection';
import { type CategoryEdge, type CategoryGraph, categoryToGraph } from '../category/categoryGraph';
import { type GraphEvent } from '../graph/graphEngine';
import { type Category } from '@/types/schema';
import { type Mapping, SimpleProperty, RootProperty } from '@/types/mapping';
import { Key, Signature, StringName, TypedName } from '@/types/identifiers';

export enum EditorPhase {
    SelectRoot = 'select-root',
    BuildPath = 'build-path',
}

export type EditMappingState = {
    category: Category;
    graph: CategoryGraph;
    mapping: Mapping;
    selectionType: SelectionType;
    selection: FreeSelection | SequenceSelection | PathSelection;
    editorPhase: EditorPhase;
    rootNodeId: string | null;
};

/**
 * Creates the initial state for the mapping editor.
 */
export function createInitialState({ category, mapping }: { category: Category, mapping: Mapping }): EditMappingState {
    return {
        category,
        // Convert category to graph for visualization
        graph: categoryToGraph(category),
        selectionType: SelectionType.Free,
        selection: FreeSelection.create(),
        mapping,
        editorPhase: EditorPhase.SelectRoot,
        rootNodeId: null,
    };
}

/**
 * Type for the dispatch function used in the mapping editor.
 */
export type EditMappingDispatch = Dispatch<EditMappingAction>;

export type EditMappingAction =
    | GraphAction
    | SelectAction
    | SequenceAction
    | PathAction
    | TempSelectionTypeAction
    | { type: 'set-root', rootNodeId: string }
    | { type: 'append-to-access-path', nodeId: string }
    | { type: 'remove-from-access-path', subpathIndex: number };

/**
 * Reduces the editor state based on the provided action.
 */
export function editMappingReducer(state: EditMappingState, action: EditMappingAction): EditMappingState {
    // console.log('REDUCE', action, state);

    switch (action.type) {
    case 'graph':
        return graph(state, action);
    case 'select':
        return select(state, action);
    case 'sequence':
        return sequence(state, action);
    case 'path':
        return path(state, action);
    case 'selection-type': {
        const { selectionType } = action;
        if (selectionType === SelectionType.Free)
            return { ...state, selectionType, selection: FreeSelection.create() };
        if (selectionType === SelectionType.Sequence)
            return { ...state, selectionType, selection: SequenceSelection.create() };
        if (selectionType === SelectionType.Path)
            return { ...state, selectionType, selection: PathSelection.create([ state.mapping.rootObjexKey.toString() ]) };
        return { ...state, selectionType: action.selectionType };
    }
    case 'set-root': {
        return root(state, action.rootNodeId);
    }
    case 'append-to-access-path': {
        return appendToAccessPath(state, action.nodeId);
    }
    case 'remove-from-access-path':
        return removeFromAccessPath(state, action.subpathIndex);
    }
}

type GraphAction = { type: 'graph', event: GraphEvent };

/**
 * Handles graph-related actions (e.g., node movement, selection).
 */
function graph(state: EditMappingState, { event }: GraphAction): EditMappingState {
    switch (event.type) {
    case 'move':
        // Node movement doesn’t update state in mapping editor (handled by graph engine)
        return state;
    case 'select': {
        if (state.selectionType === SelectionType.Path && state.selection instanceof PathSelection) {
            // Handle path selection
            if (state.selection.isEmpty) {
                // Starting a new path
                return {
                    ...state,
                    // @ts-expect-error FIXME
                    selection: PathSelection.create([ String(event.nodeId) ]),
                };
            }
            else {
                // Find the edge between last node and new node
                const lastNodeId = state.selection.lastNodeId;
                const edge = state.graph.edges.bundledEdges.flat().find(e =>
                    // @ts-expect-error FIXME
                    (e.from === lastNodeId && e.to === event.nodeId) ||
                    // @ts-expect-error FIXME
                    (e.to === lastNodeId && e.from === event.nodeId),
                );

                if (edge) {
                    return {
                        ...state,
                        selection: state.selection.updateFromAction({
                            operation: 'add',
                            // @ts-expect-error FIXME
                            nodeIds: [ String(event.nodeId) ],
                            edgeIds: [ edge.id ],
                        }),
                    };
                }
            }
        }

        // Default free selection handling
        if (state.selection instanceof FreeSelection) {
            const updatedSelection = state.selection.updateFromGraphEvent(event);
            return { ...state, selection: updatedSelection };
        }
        return state;
    }
    }
}

/**
 * Action for free selection operations.
 */
type SelectAction = { type: 'select' } & FreeSelectionAction;

/**
 * Handles free selection actions (e.g., node selection).
 */
function select(state: EditMappingState, action: SelectAction): EditMappingState {
    if (!(state.selection instanceof FreeSelection) || state.editorPhase !== EditorPhase.SelectRoot)
        return state;
    const updatedSelection = state.selection.updateFromAction(action);
    // Limit to one node
    if (updatedSelection.nodeIds.size > 1) {
        const firstNode = updatedSelection.nodeIds.values().next().value;
        // @ts-expect-error FIXME
        return { ...state, selection: FreeSelection.create([ firstNode ]) };
    }
    return { ...state, selection: updatedSelection };
}

type SequenceAction = { type: 'sequence' } & SequenceSelectionAction;

/**
 * Handles sequence selection actions.
 */
function sequence(state: EditMappingState, action: SequenceAction): EditMappingState {
    if (!(state.selection instanceof SequenceSelection))
        return state;
    return { ...state, selection: state.selection.updateFromAction(action) };
}

type PathAction = { type: 'path' } & PathSelectionAction;

/**
 * Handles path selection actions.
 */
function path(state: EditMappingState, action: PathAction): EditMappingState {
    if (!(state.selection instanceof PathSelection) || state.editorPhase !== EditorPhase.BuildPath)
        return state;
    return { ...state, selection: state.selection.updateFromAction(action) };
}

/**
 * Action for changing the selection type.
 */
type TempSelectionTypeAction = { type: 'selection-type', selectionType: SelectionType };

/**
 * Sets the root node in the mapping editor state.
 */
function root(state: EditMappingState, rootNodeId: string): EditMappingState {
    const rootNode = state.graph.nodes.get(rootNodeId);
    if (!rootNode)
        return state;

    const newAccessPath = new RootProperty(
        new TypedName(TypedName.ROOT),
        [],
    );

    return {
        ...state,
        mapping: {
            ...state.mapping,
            accessPath: newAccessPath,
            rootObjexKey: Key.fromServer(Number(rootNodeId)),
        },
        selectionType: SelectionType.Free,
        selection: FreeSelection.create(),
        editorPhase: EditorPhase.BuildPath,
        rootNodeId: rootNodeId,
    };
}

/**
 * Appends a node to the access path in the mapping editor state.
 */
function appendToAccessPath(state: EditMappingState, nodeId: string): EditMappingState {
    if (!(state.selection instanceof PathSelection)) 
        return state;

    const newNode = state.graph.nodes.get(nodeId);
    if (!newNode) 
        return state;

    const signatures = state.selection.edgeIds.map((edgeId, index) => {
        const edge: CategoryEdge = state.graph.edges.get(edgeId)!;
        const fromNodeId = Array.from(state.selection.nodeIds)[index];
        return edge.from === fromNodeId ? edge.schema.signature : edge.schema.signature.dual();
    });

    const signature = Signature.concatenate(...signatures);

    const newSubpath = new SimpleProperty(
        new StringName(newNode.metadata.label),
        signature,
        state.mapping.accessPath,
    );

    const newAccessPath = new RootProperty(state.mapping.accessPath.name, [
        ...state.mapping.accessPath.subpaths,
        newSubpath,
    ]);

    return {
        ...state,
        mapping: {
            ...state.mapping,
            accessPath: newAccessPath,
        },
        selectionType: SelectionType.Free,
        selection: FreeSelection.create(),
    };
}

/**
 * Removes a subpath from the access path in the mapping editor state.
 */
function removeFromAccessPath(state: EditMappingState, subpathIndex: number): EditMappingState {
    const newSubpaths = [ ...state.mapping.accessPath.subpaths ];
    newSubpaths.splice(subpathIndex, 1);
    
    const newAccessPath = new RootProperty(
        state.mapping.accessPath.name,
        newSubpaths,
    );
    
    return {
        ...state,
        mapping: {
            ...state.mapping,
            accessPath: newAccessPath,
        },
    };
}
