import { useReducer, type Dispatch } from 'react';
import { FreeSelection, type FreeSelectionAction, PathSelection, type PathSelectionAction, SelectionType, SequenceSelection, type SequenceSelectionAction } from '../graph/graphSelection';
import { type CategoryEdge, type CategoryGraph, categoryToGraph } from '../category/categoryGraph';
import { type GraphEvent } from '../graph/graphEngine';
import { type Category } from '@/types/schema';
import { type Mapping, SimpleProperty, RootProperty } from '@/types/mapping';
import { Key, Signature, type SignatureId, StringName, TypedName } from '@/types/identifiers';
import { type Id } from '@/types/id';

type MappingEditorInput = Mapping | {
    datasourceId: Id;
};

export function useMappingEditor(category: Category, input: MappingEditorInput) {
    const [ state, dispatch ] = useReducer(mappingEditorReducer, { category, input }, createInitialState);

    return { state, dispatch };
}

export enum EditorPhase {
    SelectRoot = 'select-root',
    BuildPath = 'build-path',
}

export type MappingEditorState = {
    original?: Mapping;
    category: Category;
    graph: CategoryGraph;
    form: MappingEditorFormState;
    selectionType: SelectionType;
    selection: FreeSelection | SequenceSelection | PathSelection;
    editorPhase: EditorPhase;
    rootNodeId: string | null;
};

type MappingEditorFormState = {
    datasourceId: Id;
    kindName: string;
    rootObjexKey: Key | undefined;
    primaryKey: SignatureId | undefined;
    accessPath: RootProperty;
}

/**
 * Creates the initial state for the mapping editor.
 */
function createInitialState({ category, input }: { category: Category, input: MappingEditorInput }): MappingEditorState {
    const original = 'id' in input ? input : undefined;

    return {
        original,
        category,
        // Convert category to graph for visualization
        graph: categoryToGraph(category),
        selectionType: SelectionType.Free,
        selection: FreeSelection.create(),
        form: {
            datasourceId: input.datasourceId,
            kindName: original?.kindName ?? '',
            rootObjexKey: original?.rootObjexKey,
            primaryKey: original?.primaryKey,
            accessPath: original?.accessPath ?? new RootProperty(new TypedName(TypedName.ROOT), []),
        },
        editorPhase: EditorPhase.SelectRoot,
        rootNodeId: null,
    };
}

export type MappingEditorDispatch = Dispatch<MappingEditorAction>;

type MappingEditorAction =
    | GraphAction
    | SelectAction
    | SequenceAction
    | PathAction
    | TempSelectionTypeAction
    | { type: 'set-root', rootNodeId: string }
    | { type: 'append-to-access-path', nodeId: string }
    | { type: 'remove-from-access-path', subpathIndex: number };

export function mappingEditorReducer(state: MappingEditorState, action: MappingEditorAction): MappingEditorState {
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
            return { ...state, selectionType, selection: PathSelection.create([ state.form.rootObjexKey.toString() ]) };
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
function graph(state: MappingEditorState, { event }: GraphAction): MappingEditorState {
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
function select(state: MappingEditorState, action: SelectAction): MappingEditorState {
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
function sequence(state: MappingEditorState, action: SequenceAction): MappingEditorState {
    if (!(state.selection instanceof SequenceSelection))
        return state;
    return { ...state, selection: state.selection.updateFromAction(action) };
}

type PathAction = { type: 'path' } & PathSelectionAction;

/**
 * Handles path selection actions.
 */
function path(state: MappingEditorState, action: PathAction): MappingEditorState {
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
function root(state: MappingEditorState, rootNodeId: string): MappingEditorState {
    const rootNode = state.graph.nodes.get(rootNodeId);
    if (!rootNode)
        return state;

    return {
        ...state,
        form: {
            ...state.form,
            rootObjexKey: Key.fromResponse(Number(rootNodeId)),
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
function appendToAccessPath(state: MappingEditorState, nodeId: string): MappingEditorState {
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
        state.form.accessPath,
    );

    const newAccessPath = new RootProperty(state.form.accessPath.name, [
        ...state.form.accessPath.subpaths,
        newSubpath,
    ]);

    return {
        ...state,
        form: {
            ...state.form,
            accessPath: newAccessPath,
        },
        selectionType: SelectionType.Free,
        selection: FreeSelection.create(),
    };
}

/**
 * Removes a subpath from the access path in the mapping editor state.
 */
function removeFromAccessPath(state: MappingEditorState, subpathIndex: number): MappingEditorState {
    const newSubpaths = [ ...state.form.accessPath.subpaths ].toSpliced(subpathIndex, 1);
    const newAccessPath = new RootProperty(state.form.accessPath.name, newSubpaths);

    return { ...state, form: { ...state.form,
        accessPath: newAccessPath,
    } };
}
