import { useEffect, useReducer, useState, type Dispatch } from 'react';
import { FreeSelection, type FreeSelectionAction, PathSelection, type PathSelectionAction, SequenceSelection, type SequenceSelectionAction } from '../graph/graphSelection';
import { type CategoryGraph, categoryToGraph } from '../category/categoryGraph';
import { type GraphEvent } from '../graph/graphEngine';
import { type Category } from '@/types/schema';
import { Mapping, RootProperty, type MappingInit, type MappingEdit, type AccessPath, traverseAccessPath, updateAccessPath } from '@/types/mapping';
import { type Key, type Name, type NamePath, type Signature, type SignatureId, TypedName } from '@/types/identifiers';
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
    selection: FreeSelection | SequenceSelection | PathSelection;
    /** Id of the component who owns the current selection. */
    selectionKey?: string;
    editorPhase: EditorPhase;
    /** Path to the currently viewed / edited property. */
    selectedPropertyPath?: NamePath;
};

type MappingEditorFormState = {
    kindName: string;
    rootObjexKey: Key | undefined;
    primaryKey: SignatureId | undefined;
    accessPath: AccessPath;
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
        selection: FreeSelection.create(),
        form: {
            kindName: original?.kindName ?? '',
            rootObjexKey: original?.rootObjexKey,
            primaryKey: original?.primaryKey,
            accessPath: (original?.accessPath ?? new RootProperty(new TypedName(TypedName.ROOT), [])).toEditable(),
        },
        editorPhase: original ? EditorPhase.BuildPath : EditorPhase.SelectRoot,
    };
}

export type MappingEditorDispatch = Dispatch<MappingEditorAction>;

type MappingEditorAction =
    | GraphAction
    | SelectAction
    | SequenceAction
    | PathAction
    | SetRootAction
    | FormAction
    | AccessPathAction
    | { type: 'sync' };

function mappingEditorReducer(state: MappingEditorState, action: MappingEditorAction): MappingEditorState {
    // console.log('REDUCE', action, state);

    switch (action.type) {
    case 'graph': return graph(state, action);
    case 'select': return select(state, action);
    case 'sequence': return sequence(state, action);
    case 'path': return path(state, action);
    case 'setRoot': return root(state, action);
    case 'form': return form(state, action);
    case 'accessPath': return accessPath(state, action);
    case 'sync': {
        if (state.sync)
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
        // Node movement doesn’t update state in mapping editor (handled by graph engine).
        return state;
    case 'select': {
        // Default free selection handling
        if (state.selection instanceof FreeSelection) {
            const updatedSelection = state.selection.updateFromGraphEvent(event);
            return { ...state, selection: updatedSelection };
        }

        // Path selection would be a nightmare - we would have to compute here the whole path options and whether we can actually insert the node / edge ...
        // TODO Maybe disable the selection box in that case?

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
        const firstNode = updatedSelection.nodeIds.values().next().value!;
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
    type: 'setRoot';
    key: Key;
};

/**
 * Sets the root node in the mapping editor state.
 */
function root(state: MappingEditorState, action: SetRootAction): MappingEditorState {
    const rootObject = state.category.getObjex(action.key);
    const ids = rootObject.schema.ids.signatureIds;

    return {
        ...state,
        form: {
            ...state.form,
            rootObjexKey: action.key,
            primaryKey: ids.length === 1 ? ids[0] : undefined,
        },
        selection: FreeSelection.create(),
        editorPhase: EditorPhase.BuildPath,
    };
}

type FormAction = {
    type: 'form';
} & ({
    field: 'kindName';
    value: string;
} | {
    field: 'primaryKey';
    value: SignatureId;
});

function form(state: MappingEditorState, action: FormAction): MappingEditorState {
    if (action.field === 'kindName')
        return { ...state, form: { ...state.form, kindName: action.value } };

    return { ...state, form: { ...state.form, primaryKey: action.value } };
}

type AccessPathAction = {
    type: 'accessPath';
} & ({
    operation: 'select';
    path: NamePath | undefined;
} | {
    operation: 'delete';
} | {
    operation: 'selection';
    selectionKey: string;
    /** If defined, the selection should start. */
    selection: PathSelection | undefined;
} | {
    operation: 'update';
    accessPath: AccessPath;
} | {
    operation: 'create';
    name: Name;
    signature: Signature;
});

function accessPath(state: MappingEditorState, action: AccessPathAction): MappingEditorState {
    if (action.operation === 'select')
        return { ...state, selectedPropertyPath: action.path, selection: FreeSelection.create() };

    if (action.operation === 'selection') {
        // Starting selection doesn't require key check - we just overwrite the previous selection.
        if (action.selection)
            return { ...state, selection: action.selection, selectionKey: action.selectionKey };

        if (state.selectionKey !== action.selectionKey)
            return state;

        return { ...state, selection: FreeSelection.create(), selectionKey: undefined };
    }

    if (!state.selectedPropertyPath)
        throw new Error('No property selected in mapping editor');

    const form = state.form;
    const selected = traverseAccessPath(form.accessPath, state.selectedPropertyPath);

    if (action.operation === 'delete') {
        if (selected.isRoot)
            return state;

        return {
            ...state,
            form: {
                ...form,
                accessPath: updateAccessPath(form.accessPath, state.selectedPropertyPath, undefined),
            },
            // The selected property path is no longer valid.
            selectedPropertyPath: undefined,
        };
    }

    if (action.operation === 'update') {
        return {
            ...state,
            form: {
                ...form,
                accessPath: updateAccessPath(form.accessPath, state.selectedPropertyPath, action.accessPath),
            },
            // The selected property path did change but we can fix it.
            selectedPropertyPath: state.selectedPropertyPath.replaceLast(action.accessPath.name),
        };
    }

    if (action.operation !== 'create')
        // This should be banned by the ts compiler. Don't know why it isn't.
        throw new Error('Impossibruh');

    const pathToNewChild = state.selectedPropertyPath.append(action.name);

    return {
        ...state,
        form: {
            ...form,
            accessPath: updateAccessPath(form.accessPath, pathToNewChild, {
                name: action.name,
                signature: action.signature,
                subpaths: [],
                isRoot: false,
            }),
        },
        selection: FreeSelection.create(),
        // The selected property path didn't change at all, so we keep it.
    };
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
        accessPath: RootProperty.fromEditable(state.form.accessPath).toServer(),
    };
}
