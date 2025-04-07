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
            signature: '0' as SignatureFromServer,
            subpaths: [],
        };
        const newAccessPath = RootProperty.fromServer(newAccessPathInput);

        return {
            ...state,
            mapping: { ...state.mapping, accessPath: newAccessPath, rootObjexKey: Key.fromServer(Number(rootNodeId)) },
            selectionType: SelectionType.Free,
            selection: FreeSelection.create(),
            editorPhase: EditorPhase.BuildPath,
        };
    }
    case 'append-to-access-path': {
        const { nodeId } = action;
        const newNode = state.graph.nodes.get(nodeId);
        if (!newNode) 
            return state;
    
        // TODO: How to do this properly?
        // Calculate signature using PathSelection
        let signature = '1'; // there should be no value and dynamic property? or what?
        if (state.selection instanceof PathSelection && state.selection.edgeIds.length > 0) {
            const edgeSignatures = state.selection.edgeIds.map((edgeId, index) => {
                const edge = state.graph.edges.bundledEdges.flat().find(e => e.id === edgeId);
                if (!edge) 
                    return `${index + 1}`; // Fallback
    
                // here to check direction (+/-)
                const lastNodeId = state.selection.nodeIds[index];
                const nextNodeId = state.selection.nodeIds[index + 1];
                const isForward = edge.from === lastNodeId && edge.to === nextNodeId;
    
                // Use edge signature or ID if signature not available
                const edgeSignature = edge.metadata?.signature || edge.id;
                return isForward ? edgeSignature : `-${edgeSignature}`;
            });
            signature = edgeSignatures.join('.');
        }
    
        const newSubpathInput: SimplePropertyFromServer = {
            name: { value: newNode.metadata.label } as NameFromServer,
            signature: signature,
        };
        const newSubpath = SimpleProperty.fromServer(newSubpathInput, state.mapping.accessPath);
    
        // Merge with existing subpaths (just add new ones)
        const currentAccessPathServer = state.mapping.accessPath.toServer();
        const newAccessPathInput: RootPropertyFromServer = {
            ...currentAccessPathServer,
            subpaths: [
                ...currentAccessPathServer.subpaths,
                newSubpath.toServer(),
            ],
        };
        const newAccessPath = RootProperty.fromServer(newAccessPathInput);
    
        return {
            ...state,
            mapping: { ...state.mapping, accessPath: newAccessPath },
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
