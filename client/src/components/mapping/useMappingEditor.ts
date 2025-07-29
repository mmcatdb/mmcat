import { useEffect, useReducer, useState, type Dispatch } from 'react';
import { FreeSelection, type FreeSelectionAction, PathSelection, type PathSelectionAction, SelectionType, SequenceSelection, type SequenceSelectionAction } from '../graph/graphSelection';
import { type CategoryEdge, type CategoryGraph, categoryToGraph } from '../category/categoryGraph';
import { type GraphEvent } from '../graph/graphEngine';
import { type Category } from '@/types/schema';
import { Mapping, SimpleProperty, RootProperty, type MappingInit, type MappingEdit } from '@/types/mapping';
import { type Key, Signature, type SignatureId, StringName, TypedName } from '@/types/identifiers';
import { type Datasource } from '@/types/Datasource';
import { api } from '@/api';
import { type Id } from '@/types/id';
import { toast } from 'react-toastify';

export type MappingEditorInput = {
    /** The existing mapping to edit (or undefined if it's a new mapping). */
    mapping: Mapping | undefined;
    datasource: Datasource;
};

export function useMappingEditor(category: Category, input: MappingEditorInput, onSave?: (mapping: Mapping) => void) {
    const [ state, dispatch ] = useReducer(mappingEditorReducer, { category, input }, createInitialState);

    const [ isFetching, setIsFetching ] = useState(false);

    async function saveMapping(data: MappingEditorSync) {
        setIsFetching(true);
        const request = 'init' in data
            ? api.mappings.createMapping({}, data.init)
            : api.mappings.updateMapping({ id: data.mappingId }, data.edit);
        const response = await request;
        setIsFetching(false);

        if (!response.status) {
            toast.error('Failed to save mapping');
            return;
        }

        toast.success('Mapping saved successfully!');
        onSave?.(Mapping.fromResponse(response.data));
    }

    useEffect(() => {
        if (state.sync)
            void saveMapping(state.sync);
    }, [ state.sync ]);

    return { state, dispatch, isFetching };
}

export enum EditorPhase {
    SelectRoot = 'selectRoot',
    BuildPath = 'buildPath',
}

export type MappingEditorState = {
    original?: Mapping;
    datasource: Datasource;
    category: Category;
    graph: CategoryGraph;
    form: MappingEditorFormState;
    sync?: MappingEditorSync;
    selectionType: SelectionType;
    selection: FreeSelection | SequenceSelection | PathSelection;
    editorPhase: EditorPhase;
};

type MappingEditorFormState = {
    kindName: string;
    rootObjexKey: Key | undefined;
    primaryKey: SignatureId | undefined;
    accessPath: RootProperty;
};

type MappingEditorSync = {
    init: MappingInit;
} | {
    mappingId: Id;
    edit: MappingEdit;
};

/**
 * Creates the initial state for the mapping editor.
 */
function createInitialState({ category, input }: { category: Category, input: MappingEditorInput }): MappingEditorState {
    const original = input.mapping;

    return {
        original,
        datasource: input.datasource,
        category,
        // Convert category to graph for visualization
        graph: categoryToGraph(category),
        selectionType: SelectionType.Free,
        selection: FreeSelection.create(),
        form: {
            kindName: original?.kindName ?? '',
            rootObjexKey: original?.rootObjexKey,
            primaryKey: original?.primaryKey,
            accessPath: original?.accessPath ?? new RootProperty(new TypedName(TypedName.ROOT), []),
        },
        editorPhase: EditorPhase.SelectRoot,
    };
}

export type MappingEditorDispatch = Dispatch<MappingEditorAction>;

type MappingEditorAction =
    | GraphAction
    | SelectAction
    | SequenceAction
    | PathAction
    | SetRootAction
    | { type: 'kindName', value: string }
    // FIXME allow nested paths
    | { type: 'add-subpath'}
    | { type: 'append-to-access-path', nodeId: string }
    | { type: 'remove-from-access-path', subpathIndex: number }
    | { type: 'sync' };

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
    case 'set-root':
        return root(state, action);
    case 'kindName':
        return { ...state, form: { ...state.form, kindName: action.value } };
    case 'add-subpath': {
        if (!state.form.rootObjexKey)
            return state;

        return { ...state, selectionType: SelectionType.Path, selection: PathSelection.create([ state.form.rootObjexKey.toString() ]) };
    }
    case 'append-to-access-path':
        return appendToAccessPath(state, action.nodeId);
    case 'remove-from-access-path':
        return removeFromAccessPath(state, action.subpathIndex);
    case 'sync': {
        if (!state.sync)
            return state;

        const sync = state.original ? { mappingId: state.original.id, edit: createMappingEdit(state) } : { init: createMappingInit(state) };
        return { ...state, sync };
    }
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

type SetRootAction = {
    type: 'set-root';
    key: Key;
};

/**
 * Sets the root node in the mapping editor state.
 */
function root(state: MappingEditorState, action: SetRootAction): MappingEditorState {
    return {
        ...state,
        form: {
            ...state.form,
            rootObjexKey: action.key,
        },
        selectionType: SelectionType.Free,
        selection: FreeSelection.create(),
        editorPhase: EditorPhase.BuildPath,
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

function createMappingInit(state: MappingEditorState): MappingInit {
    if (!state.form.rootObjexKey)
        throw new Error('Root object key must be set before syncing mapping');

    return {
        categoryId: state.category.id,
        datasourceId: state.datasource.id,
        rootObjexKey: state.form.rootObjexKey.toServer(),
        ...createMappingEdit(state),
    };
}

function createMappingEdit(state: MappingEditorState): MappingEdit {
    if (!state.form.primaryKey)
        throw new Error('Primary key must be set before syncing mapping');

    return {
        primaryKey: state.form.primaryKey.toServer(),
        kindName: state.form.kindName,
        accessPath: state.form.accessPath.toServer(),
    };
}
