import { type Dispatch } from 'react';
import { FreeSelection, type FreeSelectionAction, PathSelection, type PathSelectionAction, SelectionType, SequenceSelection, type SequenceSelectionAction } from '../graph/graphSelection';
import { type CategoryGraph, categoryToGraph } from '../category/categoryGraph';
import { type GraphEvent } from '../graph/graphEngine';
import { type Category } from '@/types/schema';
import { type Mapping, SimpleProperty, RootProperty, type SimplePropertyFromServer, type RootPropertyFromServer } from '@/types/mapping';
import { Key, type NameFromServer, type SignatureFromServer } from '@/types/identifiers';

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
        // TODO This should depened on the selection class.
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

        const newAccessPathInput: RootPropertyFromServer = {
            name: { value: rootNode.metadata.label } as NameFromServer,
            signature: 'EMPTY' as SignatureFromServer,
            subpaths: [],
        };
        const newAccessPath = RootProperty.fromServer(newAccessPathInput);

        return {
            ...state,
            mapping: { ...state.mapping, accessPath: newAccessPath, rootObjexKey: Key.fromServer(Number(rootNodeId)) },
            selectionType: SelectionType.Path,
            selection: PathSelection.create([ rootNodeId ]),
            editorPhase: EditorPhase.BuildPath,
        };
    }
    case 'append-to-access-path': {
        const { nodeId } = action;
        const newNode = state.graph.nodes.get(nodeId);
        if (!newNode) 
            return state;

        const newSubpathInput: SimplePropertyFromServer = {
            name: { value: newNode.metadata.label } as NameFromServer,
            signature: 'EMPTY' as SignatureFromServer,
        };
        const newSubpath = SimpleProperty.fromServer(newSubpathInput, state.mapping.accessPath);

        const currentAccessPathServer = state.mapping.accessPath.toServer();
        const newAccessPathInput: RootPropertyFromServer = {
            name: currentAccessPathServer.name,
            signature: currentAccessPathServer.signature,
            subpaths: [ ...currentAccessPathServer.subpaths, newSubpath.toServer() ],
        };
        const newAccessPath = RootProperty.fromServer(newAccessPathInput);

        return {
            ...state,
            mapping: { ...state.mapping, accessPath: newAccessPath },
            selectionType: SelectionType.Path,
            selection: PathSelection.create([ state.mapping.rootObjexKey.toString() ]), // Reset to root
        };
    }
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
    }
    case 'select': {
        if (!(state.selection instanceof FreeSelection) || state.editorPhase !== EditorPhase.SelectRoot) 
            return state;
        const updatedSelection = state.selection.updateFromGraphEvent(event);
        // Limit to one node
        if (updatedSelection.nodeIds.size > 1) {
            const firstNode = updatedSelection.nodeIds.values().next().value;
            return {
                ...state,
                selection: FreeSelection.create([ firstNode ]),
            };
        }
        return { ...state, selection: updatedSelection };
    }
    }
}

// Selection

type SelectAction = {
    type: 'select';
} & FreeSelectionAction;

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
    if (!(state.selection instanceof PathSelection) || state.editorPhase !== EditorPhase.BuildPath) 
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
