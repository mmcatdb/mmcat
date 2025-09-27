import { MappingEditorGraph } from './MappingEditorGraph';
import { type MappingEditorState, EditorPhase, useMappingEditor, type MappingEditorDispatch, type MappingEditorInput } from './useMappingEditor';
import { type Category } from '@/types/schema';
import { type FreeSelection } from '../graph/graphSelection';
import { collectAccessPathSignature, traverseAccessPath, type AccessPath, type Mapping } from '@/types/mapping';
import { Button, Input, Select, SelectItem } from '@heroui/react';
import { PlusIcon, CheckCircleIcon, XMarkIcon } from '@heroicons/react/20/solid';
import { useCallback, useMemo, useState } from 'react';
import { DynamicName, type Name, type NamePath, Signature, StringName } from '@/types/identifiers';
import { PencilIcon } from '@heroicons/react/24/solid';
import { TrashIcon } from '@heroicons/react/24/outline';
import { AccessPathDisplay } from './AccessPathDisplay';
import { traverseCategoryGraph } from '../category/categoryGraph';
import { fromEditableName, NameInput, toEditableName } from './NameInput';
import { SignatureInput } from './SignatureInput';

type MappingEditorProps = {
    /** The schema category to which the mapping belongs. */
    category: Category;
    input: MappingEditorInput;
    /** Optional callback to handle saving the mapping. */
    onSave?: (mapping: Mapping) => void;
    onCancel?: () => void;
};

/**
 * Renders the mapping editor with a graph display and panels for root selection and access path building.
 */
export function MappingEditor({ category, input, onSave, onCancel }: MappingEditorProps) {
    const { state, dispatch, isFetching } = useMappingEditor(category, input, onSave);

    return (
        <div className='relative flex h-[calc(100vh-40px)]'>
            {/* Left Panel - Form Controls */}
            <div className='w-80 bg-content1 border-r border-default-200 p-4 flex flex-col'>
                <div className=''>
                    <h2 className='text-xl font-semibold'>
                        {input.mapping ? 'Edit Mapping' : 'Create Mapping'}
                    </h2>

                    {/* Show datasource info */}
                    <div className='pt-2 pb-6 text-sm text-default-600'>
                        For datasource
                        {input.datasource.label}
                    </div>

                    <Input
                        label='Kind Name'
                        value={state.form.kindName}
                        onChange={e => dispatch({ type: 'form', field: 'kindName', value: e.target.value })}
                        placeholder='Enter Kind Name'
                        fullWidth
                        autoFocus
                    />
                </div>

                {/* Scrollable content area */}
                <div className='flex-1 overflow-y-auto py-2 mt-3'>
                    {state.editorPhase === EditorPhase.SelectRoot ? (
                        <RootSelectionPanel state={state} dispatch={dispatch} />
                    ) : (<>
                        <PrimaryKeySelect state={state} dispatch={dispatch} />
                        <div className='h-3' />
                        <AccessPathPanel state={state} dispatch={dispatch} />
                    </>)}
                </div>

                {/* Fixed footer with buttons */}
                <div className='pt-4 border-t border-default-100'>
                    <div className='flex gap-3 justify-end'>
                        {onCancel && (
                            <Button
                                color='danger'
                                variant='flat'
                                onPress={onCancel}
                                startContent={<XMarkIcon className='size-4' />}
                                size='sm'
                            >
                                Discard
                            </Button>
                        )}

                        <Button
                            color='success'
                            variant='solid'
                            onPress={() => dispatch({ type: 'sync' })}
                            startContent={<CheckCircleIcon className='size-4' />}
                            size='sm'
                            isDisabled={!state.form.rootObjexKey}
                            isLoading={isFetching}
                        >
                            {input.mapping ? 'Save Mapping' : 'Create Mapping'}
                        </Button>
                    </div>
                </div>
            </div>

            {/* Main Graph Area */}
            <MappingEditorGraph state={state} dispatch={dispatch} className='grow' />
        </div>
    );
}

type StateDispatchProps = {
    state: MappingEditorState;
    dispatch: MappingEditorDispatch;
};

/**
 * Renders a panel for selecting the root node during the SelectRoot phase.
 */
function RootSelectionPanel({ state, dispatch }: StateDispatchProps) {
    const selection = state.selection as FreeSelection;
    const { graph } = state;

    const selectedNode = selection.nodeIds.size > 0
        ? graph.nodes.get([ ...selection.nodeIds ][0])
        : null;

    function setRoot() {
        if (!selection.isEmpty) {
            const rootNodeId = selection.nodeIds.values().next().value!;
            const rootNode = graph.nodes.get(rootNodeId)!;
            dispatch({ type: 'setRoot', key: rootNode.schema.key });
        }
    }

    return (
        <div className='space-y-4'>
            <h3 className='text-lg font-semibold'>Select Root Object</h3>

            {!selectedNode ? (
                <div className='p-3 rounded-lg bg-default-100 text-default-500 text-sm'>
                    Click on a node in the graph
                </div>
            ) : (
                <div className='p-3 rounded-lg bg-default-100'>
                    <div className='flex items-center gap-2'>
                        {!selectedNode.schema.ids?.isSignatures ? (
                            <p className='grow h-8 px-3 flex items-center rounded-lg bg-warning-100 text-warning-800 text-sm'>
                                Select an object with signature ids
                            </p>
                        ) : (<>
                            <div className='font-medium'>({selectedNode.schema.key.toString()})</div>
                            <div className='grow'>{selectedNode.metadata.label}</div>
                        </>)}

                        <Button
                            isIconOnly
                            size='sm'
                            variant='light'
                            onPress={() => dispatch({ type: 'select', operation: 'clear', range: 'nodes' })}
                        >
                            <XMarkIcon className='size-5' />
                        </Button>
                    </div>

                    <Button
                        fullWidth
                        color='primary'
                        className='mt-3'
                        onPress={setRoot}
                        isDisabled={!selectedNode.schema.ids?.isSignatures}
                    >
                        Confirm Root Selection
                    </Button>
                </div>
            )}

            {selection.edgeIds.size > 0 && (
                <p className='p-3 rounded-lg bg-warning-100 text-warning-800 text-sm'>
                    Only nodes can be selected as root objects
                </p>
            )}
        </div>
    );
}

function PrimaryKeySelect({ state, dispatch }: StateDispatchProps) {
    const { graph } = state;

    const rootObject = graph.nodes.get(state.form.rootObjexKey!.toString())!;

    const options = useMemo(() => {
        return rootObject.schema.ids!.signatureIds.map((id, index) => ({
            key: String(index),
            label: id.toString(),
        }));
    }, [ rootObject ]);

    const primaryKey = state.form.primaryKey;

    const selectedKeys = useMemo(() => {
        const output = new Set<string>();
        if (primaryKey) {
            const index = rootObject.schema.ids!.signatureIds.findIndex(id => id.equals(primaryKey));
            output.add(String(index));
        }
        return output;
    }, [ rootObject, primaryKey ]);

    return (
        <Select
            items={options}
            label='Primary Key'
            selectedKeys={selectedKeys}
            onSelectionChange={keys => {
                const key = (keys as Set<string>).values().next().value;
                if (key === undefined)
                    return;

                const id = rootObject.schema.ids!.signatureIds[Number(key)];
                dispatch({ type: 'form', field: 'primaryKey', value: id });
            }}
        >
            {item => <SelectItem key={item.key}>{item.label}</SelectItem>}
        </Select>
    );
}

/**
 * Renders a card for building and displaying the access path.
 */
function AccessPathPanel({ state, dispatch }: StateDispatchProps) {
    const selected = useMemo(
        () => state.selectedPropertyPath && traverseAccessPath(state.form.accessPath, state.selectedPropertyPath),
        [ state.selectedPropertyPath, state.form.accessPath ],
    );

    const setSelected = useCallback((path: NamePath) => dispatch({ type: 'accessPath', operation: 'select', path }), [ dispatch ]);

    return (
        <div className='space-y-4'>
            <h3 className='text-lg font-semibold'>Access Path</h3>

            <AccessPathDisplay
                property={state.form.accessPath}
                selected={selected}
                setSelected={setSelected}
            />

            {selected && (
                <SelectedProperty
                    // The user might be editing some property and then he clicks on another one. In that case, we have to reset the whole inner state.
                    key={state.selectedPropertyPath?.toString()}
                    state={state}
                    dispatch={dispatch}
                    selected={selected}
                />
            )}
        </div>
    );
}

enum PropertyPhase {
    view = 'view',
    create = 'create',
    update = 'update',
}

function SelectedProperty({ state, dispatch, selected }: StateDispatchProps & { selected: AccessPath }) {
    const [ phase, setPhase ] = useState<PropertyPhase>(PropertyPhase.view);

    function startViewing() {
        setPhase(PropertyPhase.view);
    }

    if (phase === PropertyPhase.create) {
        return (
            <CreateProperty state={state} dispatch={dispatch} selected={selected} onClose={startViewing} />
        );
    }

    if (phase === PropertyPhase.update) {
        return (
            <UpdateProperty state={state} dispatch={dispatch} selected={selected} onClose={startViewing} />
        );
    }

    return (<>
        <h3 className='text-lg font-semibold'>Selected property</h3>

        <div className='px-3 py-2 rounded-lg bg-default-100'>
            <div className='flex items-center gap-2'>
                <div className='font-semibold'>Name:</div>

                <div className='truncate'>{selected.name.toString()}</div>
            </div>

            <div className='mt-1 flex items-center gap-2'>
                <div className='font-semibold'>Signature:</div>

                <div className='truncate'>{selected.signature.toString()}</div>
            </div>
        </div>

        <div className='flex gap-2'>
            <Button color='primary' isIconOnly onPress={() => setPhase(PropertyPhase.create)}>
                <PlusIcon className='size-6' />
            </Button>

            {!selected.isRoot && (<>
                <Button color='default' isIconOnly onPress={() => setPhase(PropertyPhase.update)}>
                    <PencilIcon className='size-6' />
                </Button>

                <Button color='danger' isIconOnly onPress={() => dispatch({ type: 'accessPath', operation: 'delete' })}>
                    <TrashIcon className='size-6' />
                </Button>
            </>)}
        </div>
    </>);
}

function CreateProperty({ state, dispatch, selected, onClose }: StateDispatchProps & { selected: AccessPath, onClose: () => void }) {
    const [ name, setName ] = useState('');
    const [ isNameTouched, setIsNameTouched ] = useState(false);
    const [ signature, setSignature ] = useState(Signature.empty());

    const suggestedName = useMemo(() => suggestName(signature, state), [ signature, state ]);
    const validName = isNameTouched ? state.datasource.createValidPropertyName(name) : suggestedName;
    // When creating property, we allow only StringName for simplicity. The user can always change it later.
    const nameObject = validName ? new StringName(validName) : undefined;
    const displayedName = isNameTouched ? name : suggestedName;

    function createProperty() {
        dispatch({ type: 'accessPath', operation: 'create', name: nameObject!, signature });
        onClose();
    }

    return (<>
        <h3 className='text-lg font-semibold'>Add property</h3>

        <Input
            label='Property name'
            value={displayedName ?? ''}
            onChange={e => {
                setName(e.target.value);
                setIsNameTouched(true);
            }}
            placeholder='Enter property name'
            fullWidth
        />

        <SignatureInput
            value={signature}
            onChange={setSignature}
            label='Path'
            state={state}
            dispatch={dispatch}
        />

        <div className='flex gap-2'>
            <Button size='sm' color='primary' onPress={createProperty} isDisabled={!canUseName(selected, nameObject)}>
                Add property
            </Button>

            <Button size='sm' color='secondary' onPress={onClose}>
                Cancel
            </Button>
        </div>
    </>);
}

function suggestName(signature: Signature, state: MappingEditorState): string | undefined {
    if (signature.isEmpty)
        return '';

    const { datasource, graph, form, selectedPropertyPath } = state;

    const signatureToSelected = collectAccessPathSignature(form.accessPath, selectedPropertyPath!);
    const rootNode = graph.nodes.get(form.rootObjexKey!.toString())!;
    const childNode = traverseCategoryGraph(graph, rootNode, signatureToSelected.concatenate(signature));
    return datasource.createValidPropertyName(childNode.metadata.label);
}

function UpdateProperty({ state, dispatch, selected, onClose }: StateDispatchProps & { selected: AccessPath, onClose: () => void }) {
    const [ name, setName ] = useState(toEditableName(selected.name));
    const nameObject = fromEditableName(name, state);

    const [ signature, setSignature ] = useState(selected.signature);

    const parent = useMemo(() => {
        const parentPath = state.selectedPropertyPath!.pop();
        return traverseAccessPath(state.form.accessPath, parentPath);
    }, [ state.form.accessPath, state.selectedPropertyPath ]);

    function updateProperty() {
        dispatch({ type: 'accessPath', operation: 'update', name: nameObject!, signature });
        onClose();
    }

    const isChanged = !nameObject?.equals(selected.name) || !signature.equals(selected.signature);

    return (<>
        <h3 className='text-lg font-semibold'>Edit property</h3>

        <NameInput
            state={state}
            dispatch={dispatch}
            name={name}
            setName={setName}
            selected={selected}
        />

        <SignatureInput
            isFromParent
            value={signature}
            onChange={setSignature}
            label='Path'
            state={state}
            dispatch={dispatch}
        />

        <div className='flex gap-2'>
            <Button size='sm' color='primary' onPress={updateProperty} isDisabled={!isChanged || !canUseName(parent, nameObject, selected.name)}>
                Edit property
            </Button>

            <Button size='sm' color='secondary' onPress={onClose}>
                Cancel
            </Button>
        </div>
    </>);
}

function canUseName(parent: AccessPath, name: Name | undefined, original?: Name): boolean {
    if (!name)
        return false;

    if (original?.equals(name))
        return true;

    // There can only be one dynamic name with null pattern.
    if (name instanceof DynamicName && !name.pattern) {
        if (parent.subpaths.some(s => s.name instanceof DynamicName && !s.name.pattern))
            return false;
    }

    // The subpaths are unique by their names, not their signatures! E.g., there can be multiple empty signatures.
    return !parent.subpaths.some(s => s.name.equals(name));
}
