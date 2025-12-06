import { CategoryGraphDisplay } from '../category/graph/CategoryGraphDisplay';
import { type MappingEditorState, EditorPhase, useMappingEditor, type MappingEditorDispatch, type MappingEditorInput, findSelectedNode } from './useMappingEditor';
import { type Category } from '@/types/schema';
import { type FreeSelection } from '../graph/selection';
import { findIndexSubpaths, findTypedSubpath, getAccessPathType, traverseAccessPath, type AccessPath, type Mapping } from '@/types/mapping';
import { Button, Input, Select, SelectItem } from '@heroui/react';
import { PlusIcon, CheckCircleIcon, XMarkIcon, MinusIcon } from '@heroicons/react/20/solid';
import { useCallback, useMemo, useState } from 'react';
import { DynamicName, IndexName, type Name, type NamePath, Signature, StringName, TypedName } from '@/types/identifiers';
import { PencilIcon } from '@heroicons/react/24/solid';
import { TrashIcon } from '@heroicons/react/24/outline';
import { AccessPathDisplay, AccessPathPreview } from './AccessPathDisplay';
import { traverseCategoryGraph } from '../category/graph/categoryGraph';
import { NameInput } from './NameInput';
import { SignatureInput } from '../category/graph/SignatureInput';
import { SpinnerButton } from '../common/components';
import { PropertyTypeInput } from './PropertyTypeInput';
import { GraphHighlights } from '../category/graph/highlights';
import { DefaultPathGraphProvider } from '../category/graph/selection';
import { FaPlus } from 'react-icons/fa';

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

    const graphHighlights = useMemo(() => {
        const selectedNode = findSelectedNode(state);
        return selectedNode ? GraphHighlights.create([ selectedNode.id ]) : undefined;
        // This is ok since nothing else can't change without changing the selected path.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [ state.selectedPropertyPath ]);

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

                        <SpinnerButton
                            color='success'
                            variant='solid'
                            onPress={() => dispatch({ type: 'sync' })}
                            startContent={<CheckCircleIcon className='size-4' />}
                            size='sm'
                            isDisabled={!state.form.rootObjexKey}
                            isFetching={isFetching}
                        >
                            {input.mapping ? 'Save Mapping' : 'Create Mapping'}
                        </SpinnerButton>
                    </div>
                </div>
            </div>

            {/* Main Graph Area */}
            <CategoryGraphDisplay
                graph={state.graph}
                selection={state.selection}
                dispatch={dispatch}
                highlights={graphHighlights}
                className='grow'
            />
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

    const selectedNode = selection.firstNodeId ? graph.nodes.get(selection.firstNodeId) : undefined;

    function setRoot() {
        if (selectedNode)
            dispatch({ type: 'setRoot', key: selectedNode.schema.key });
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
                        {selectedNode.schema.ids.isEmpty ? (
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
                            // FIXME Replace with a proper action.
                            onPress={() => dispatch({ type: 'selection', selection: selection.update({ operation: 'clear', range: 'nodes' }) })}
                        >
                            <XMarkIcon className='size-5' />
                        </Button>
                    </div>

                    <Button
                        fullWidth
                        color='primary'
                        className='mt-3'
                        onPress={setRoot}
                        isDisabled={selectedNode.schema.ids.isEmpty}
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
    const rootObjex = state.category.getObjex(state.form.rootObjexKey!);

    const options = useMemo(() => {
        return rootObjex.schema.ids.signatureIds.map((id, index) => ({
            key: String(index),
            label: id.toString(),
        }));
    }, [ rootObjex ]);

    const primaryKey = state.form.primaryKey;

    const selectedKeys = useMemo(() => {
        const output = new Set<string>();
        if (primaryKey) {
            const index = rootObjex.schema.ids.signatureIds.findIndex(id => id.equals(primaryKey));
            output.add(String(index));
        }
        return output;
    }, [ rootObjex, primaryKey ]);

    return (
        <Select
            items={options}
            label='Primary Key'
            selectedKeys={selectedKeys}
            onSelectionChange={keys => {
                const key = (keys as Set<string>).values().next().value;
                if (key === undefined)
                    return;

                const id = rootObjex.schema.ids.signatureIds[Number(key)];
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
    const [ phase, setPhase ] = useState(PropertyPhase.view);

    const selected = useMemo(
        () => state.selectedPropertyPath && traverseAccessPath(state.form.accessPath, state.selectedPropertyPath),
        [ state.selectedPropertyPath, state.form.accessPath ],
    );

    const setSelected = useCallback((path: NamePath) => {
        setPhase(PropertyPhase.view);
        dispatch({ type: 'accessPath', operation: 'select', path });
    }, [ dispatch ]);

    return (
        <div className='space-y-4'>
            <h3 className='text-lg font-semibold'>Access Path</h3>

            <AccessPathDisplay
                property={state.form.accessPath}
                selected={selected}
                onClick={phase === PropertyPhase.view ? setSelected : undefined}
            />

            {selected && (
                <SelectedProperty
                    // The user might be editing some property and then he clicks on another one. In that case, we have to reset the whole inner state.
                    key={state.selectedPropertyPath?.toString()}
                    state={state}
                    dispatch={dispatch}
                    selected={selected}
                    phase={phase}
                    setPhase={setPhase}
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

type SelectedPropertyProps = StateDispatchProps & {
    selected: AccessPath;
    phase: PropertyPhase;
    setPhase: (phase: PropertyPhase) => void;
};

function SelectedProperty({ state, dispatch, selected, phase, setPhase }: SelectedPropertyProps) {
    function startViewing() {
        setPhase(PropertyPhase.view);
    }

    if (phase === PropertyPhase.create) {
        return (
            <CreateProperty state={state} dispatch={dispatch} parent={selected} onClose={startViewing} />
        );
    }

    if (phase === PropertyPhase.update) {
        return (
            <UpdateProperty state={state} dispatch={dispatch} original={selected} onClose={startViewing} />
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
                <FaPlus className='size-4' />
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

function CreateProperty({ state, dispatch, parent, onClose }: StateDispatchProps & { parent: AccessPath, onClose: () => void }) {
    const [ name, setName ] = useState('');
    const [ isNameTouched, setIsNameTouched ] = useState(false);
    const [ signature, setSignature ] = useState(Signature.empty());

    // This is ok since nothing else can't change without changing the selected path.
    // eslint-disable-next-line react-hooks/exhaustive-deps
    const selectedNode = useMemo(() => findSelectedNode(state)!, [ state.form.accessPath, state.selectedPropertyPath ]);

    const suggestedName = useMemo(() => {
        if (signature.isEmpty)
            return '';

        const childNode = traverseCategoryGraph(state.graph, selectedNode, signature);
        return state.datasource.createValidPropertyName(childNode.metadata.label);
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [ signature, selectedNode ]);

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

        <div className='space-y-2'>
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
                selection={state.selection}
                selectionKey={state.selectionKey}
                pathGraphProvider={new DefaultPathGraphProvider(state.category)}
                dispatch={dispatch}
                value={signature}
                onChange={setSignature}
                fromNode={selectedNode}
                label='Path'
            />
        </div>

        <div className='flex gap-2'>
            <Button size='sm' color='primary' onPress={createProperty} isDisabled={!canUseName(parent, nameObject)}>
                Add property
            </Button>

            <Button size='sm' color='secondary' onPress={onClose}>
                Cancel
            </Button>
        </div>
    </>);
}

function UpdateProperty({ state, dispatch, original, onClose }: StateDispatchProps & { original: AccessPath, onClose: () => void }) {
    const [ property, setProperty ] = useState(original);

    const parent = useMemo(() => {
        const parentPath = state.selectedPropertyPath!.pop();
        return traverseAccessPath(state.form.accessPath, parentPath);
    }, [ state.form.accessPath, state.selectedPropertyPath ]);

    // This is ok since nothing else can't change without changing the selected path.
    // eslint-disable-next-line react-hooks/exhaustive-deps
    const parentNode = useMemo(() => findSelectedNode(state, true)!, [ state.form.accessPath, state.selectedPropertyPath ]);

    function updateProperty() {
        dispatch({ type: 'accessPath', operation: 'update', accessPath: property });
        onClose();
    }

    function setSignature(signature: Signature) {
        setProperty(prev => ({
            ...prev,
            signature,
            // If the signature changed, we have to drop all subpaths.
            subpaths: signature.equals(prev.signature) ? prev.subpaths : [],
        }));
    }

    const indexSubpaths = findIndexSubpaths(property);

    function incrementDimension(up: boolean) {
        setProperty(prev => {
            const valueSubpath = findTypedSubpath(prev, TypedName.VALUE)!;
            const prevIndexSubpaths = findIndexSubpaths(prev);
            if (!up)
                return { ...prev, subpaths: [ ...prevIndexSubpaths.slice(0, -1), valueSubpath ] };

            const originalIndexSubpaths = findIndexSubpaths(original);
            const index = prevIndexSubpaths.length;
            const newSubpath = originalIndexSubpaths.length > index
                ? originalIndexSubpaths[index]
                : {
                    name: new IndexName(index),
                    signature: Signature.empty(),
                    subpaths: [],
                    isRoot: false,
                } satisfies AccessPath;

            return { ...prev, subpaths: [  ...prevIndexSubpaths, newSubpath, valueSubpath ] };
        });
    }

    const isChanged = !property.name.equals(original.name) ||
        !property.signature.equals(original.signature) ||
        property.subpaths.length !== original.subpaths.length ||
        getAccessPathType(property) !== getAccessPathType(original);

    return (<>
        <h3 className='text-lg font-semibold'>Edit property</h3>

        <div className='space-y-2'>
            {!isTypeImmutable(original) && (
                <div className='flex gap-2'>
                    <PropertyTypeInput state={state} property={property} setProperty={setProperty} original={original} />

                    <NameInput
                        state={state}
                        dispatch={dispatch}
                        name={property.name}
                        setName={name => setProperty(prev => ({ ...prev, name }))}
                    />
                </div>
            )}

            <SignatureInput
                selection={state.selection}
                selectionKey={state.selectionKey}
                pathGraphProvider={new DefaultPathGraphProvider(state.category)}
                dispatch={dispatch}
                value={property.signature}
                onChange={setSignature}
                fromNode={parentNode}
                label='Path'
            />

            {indexSubpaths.length !== 0 && (
                <div className='w-full pl-3 pr-2 py-2 rounded-medium flex gap-2 shadow-xs bg-default-100'>
                    <div className='flex flex-col'>
                        <label className='text-sm/5 text-default-600 cursor-pointer'>Dimension</label>

                        <div className='text-small leading-5 text-default-foreground'>
                            {indexSubpaths.length}
                        </div>
                    </div>

                    <div className='flex-1' />

                    <Button isIconOnly onPress={() => incrementDimension(false)} isDisabled={indexSubpaths.length <= 1}>
                        <MinusIcon className='size-5' />
                    </Button>

                    <Button isIconOnly onPress={() => incrementDimension(true)}>
                        <PlusIcon className='size-5' />
                    </Button>
                </div>
            )}
        </div>

        <AccessPathPreview property={property} className='mb-4' />

        <div className='flex gap-2'>
            <Button size='sm' color='primary' onPress={updateProperty} isDisabled={!isChanged || !canUseName(parent, property.name, original.name)}>
                Edit property
            </Button>

            <Button size='sm' color='secondary' onPress={onClose}>
                Cancel
            </Button>
        </div>
    </>);
}

function isTypeImmutable(property: AccessPath): boolean {
    return property.name instanceof TypedName && [ TypedName.VALUE, TypedName.KEY, IndexName.TYPE ].includes(property.name.type);
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
