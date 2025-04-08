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
};

export function createInitialState({ category, mapping }: { category: Category, mapping: Mapping }): EditMappingState {
    return {
        category,
        graph: categoryToGraph(category),
        selectionType: SelectionType.Free,
        selection: FreeSelection.create(),
        mapping,
        editorPhase: EditorPhase.SelectRoot,
    };
}

export type EditMappingDispatch = Dispatch<EditMappingAction>;

export type EditMappingAction =
    | GraphAction
    | SelectAction
    | SequenceAction
    | PathAction
    | TempSelectionTypeAction
    | { type: 'set-root', rootNodeId: string }
    | { type: 'append-to-access-path', nodeId: string };

export function editMappingReducer(state: EditMappingState, action: EditMappingAction): EditMappingState {
    console.log('REDUCE', action, state);

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
        const { rootNodeId } = action;
        const rootNode = state.graph.nodes.get(rootNodeId);
        if (!rootNode)
            return state;

        const newAccessPath = new RootProperty(new TypedName(TypedName.ROOT), []);

        // .fromServer(newAccessPathInput);

        return {
            ...state,
            mapping: { ...state.mapping, accessPath: newAccessPath, rootObjexKey: Key.fromServer(Number(rootNodeId)) },
            selectionType: SelectionType.Free,
            selection: FreeSelection.create(),
            editorPhase: EditorPhase.BuildPath,
        };
    }
    case 'append-to-access-path': {
        const selection = state.selection;
        if (!(selection instanceof PathSelection))
            return state;

        const { nodeId } = action;
        const newNode = state.graph.nodes.get(nodeId);
        if (!newNode)
            return state;
    
        // Calculate signature using PathSelection
        const signatures = selection.edgeIds.map((edgeId, index) => {
            const edge: CategoryEdge = state.graph.edges.get(edgeId)!;
            const fromNodeId = selection.nodeIds[index];
            return edge.from === fromNodeId ? edge.schema.signature : edge.schema.signature.dual();
        });

        const signature = Signature.concatenate(...signatures);

        const newSubpath = new SimpleProperty(new StringName(newNode.metadata.label), signature, state.mapping.accessPath);

        // Merge with existing subpaths (just add new ones)
        const newAccessPath = new RootProperty(state.mapping.accessPath.name, [
            ...state.mapping.accessPath.subpaths,
            newSubpath,
        ]);

        return {
            ...state,
            mapping: { ...state.mapping, accessPath: newAccessPath },
            selectionType: SelectionType.Free,
            selection: FreeSelection.create(),
        };
    }
    }
}

type GraphAction = { type: 'graph', event: GraphEvent };

function graph(state: EditMappingState, { event }: GraphAction): EditMappingState {
    switch (event.type) {
    case 'move':
        return state;
    case 'select': {
        if (state.selectionType === SelectionType.Path && state.selection instanceof PathSelection) {
            // Handle path selection
            if (state.selection.isEmpty) {
                // Starting a new path
                return {
                    ...state,
                    selection: PathSelection.create([ event.nodeId ]),
                };
            }
            else {
                // Find the edge between last node and new node
                const lastNodeId = state.selection.lastNodeId;
                const edge = state.graph.edges.bundledEdges.flat().find(e =>
                    (e.from === lastNodeId && e.to === event.nodeId) ||
                    (e.to === lastNodeId && e.from === event.nodeId),
                );

                if (edge) {
                    return {
                        ...state,
                        selection: state.selection.updateFromAction({
                            operation: 'add',
                            nodeIds: [ event.nodeId ],
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

type SelectAction = { type: 'select' } & FreeSelectionAction;

function select(state: EditMappingState, action: SelectAction): EditMappingState {
    if (!(state.selection instanceof FreeSelection) || state.editorPhase !== EditorPhase.SelectRoot)
        return state;
    const updatedSelection = state.selection.updateFromAction(action);
    // Limit to one node
    if (updatedSelection.nodeIds.size > 1) {
        const firstNode = updatedSelection.nodeIds.values().next().value;
        return { ...state, selection: FreeSelection.create([ firstNode ]) };
    }
    return { ...state, selection: updatedSelection };
}

type SequenceAction = { type: 'sequence' } & SequenceSelectionAction;

function sequence(state: EditMappingState, action: SequenceAction): EditMappingState {
    if (!(state.selection instanceof SequenceSelection))
        return state;
    return { ...state, selection: state.selection.updateFromAction(action) };
}

type PathAction = { type: 'path' } & PathSelectionAction;

function path(state: EditMappingState, action: PathAction): EditMappingState {
    if (!(state.selection instanceof PathSelection) || state.editorPhase !== EditorPhase.BuildPath)
        return state;
    return { ...state, selection: state.selection.updateFromAction(action) };
}

type TempSelectionTypeAction = { type: 'selection-type', selectionType: SelectionType };
