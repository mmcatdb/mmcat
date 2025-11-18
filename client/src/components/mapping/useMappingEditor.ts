import { useEffect, useReducer, useState, type Dispatch } from 'react';
import { FreeSelection, type CategoryGraphSelection } from '../category/graph/selection';
import { type CategoryGraph, type CategoryNode, categoryToGraph, traverseCategoryGraph } from '../category/graph/categoryGraph';
import { type Category } from '@/types/schema';
import { Mapping, RootProperty, type MappingInit, type MappingEdit, type AccessPath, traverseAccessPath, updateAccessPath, collectAccessPathSignature } from '@/types/mapping';
import { type Key, type Name, type NamePath, type Signature, type SignatureId, TypedName } from '@/types/identifiers';
import { type Datasource } from '@/types/Datasource';
import { api } from '@/api';
import { type Id } from '@/types/id';
import { toast } from 'react-toastify';
import { type GraphMoveEvent } from '../graph/graphEngine';

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
    selection: CategoryGraphSelection | undefined;
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
        // Convert category to graph for visualization.
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
    | GraphMoveEvent
    | SelectionAction
    | SetRootAction
    | FormAction
    | AccessPathAction
    | { type: 'sync' };

function mappingEditorReducer(state: MappingEditorState, action: MappingEditorAction): MappingEditorState {
    // console.log('REDUCE', action, state);

    switch (action.type) {
    // Node movement doesn’t update state in mapping editor (it's handled by the graph engine).
    case 'move': return state;
    case 'selection': return selection(state, action);
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

type SelectionAction = {
    type: 'selection';
    selection: CategoryGraphSelection | undefined;
    selectionKey?: string;
};

function selection(state: MappingEditorState, { selection, selectionKey }: SelectionAction): MappingEditorState {
    if (!selection) {
        if (state.selectionKey !== selectionKey)
            // Only the component that owns the selection can clear it. Howevever, starting a new selection is fine.
            return state;

        selectionKey = undefined;
    }
    else {
        // Some graph events don't respect the selectionKey - in that case, we continue with the previous one.
        selectionKey = selectionKey ?? state.selectionKey;
    }

    if (selection instanceof FreeSelection) {
        // Limit to one node.
        if (selection.firstNodeId)
            selection = FreeSelection.create([ selection.firstNodeId ]);
    };

    return { ...state, selection, selectionKey };
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
        selection: undefined,
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

/**
 * Finds node corresponding to the schema objex corresponding to the selected property (or its parent).
 * @param useParent - If true, we are interested in the parent property.
 */
export function findSelectedNode(state: MappingEditorState, useParent = false): CategoryNode | undefined {
    if (!state.selectedPropertyPath)
        return;

    const toPath = useParent ? state.selectedPropertyPath.pop() : state.selectedPropertyPath;
    const pathFromRoot = collectAccessPathSignature(state.form.accessPath, toPath);

    const rootNode = state.graph.nodes.get(state.form.rootObjexKey!.toString())!;
    return traverseCategoryGraph(state.graph, rootNode, pathFromRoot);
}
