import { MappingEditorGraph } from './MappingEditorGraph';
import { type MappingEditorState, EditorPhase, useMappingEditor, type MappingEditorDispatch, type MappingEditorInput } from './useMappingEditor';
import { type Category } from '@/types/schema';
import { type FreeSelection, PathSelection, SelectionType } from '../graph/graphSelection';
import { type Mapping } from '@/types/mapping';
import { Button, Input } from '@heroui/react';
import { PlusIcon, CheckCircleIcon, XMarkIcon } from '@heroicons/react/20/solid';
import { type CategoryGraph } from '../category/categoryGraph';
import { getPathSignature } from '../graph/graphUtils';

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
                    <h2 className='text-xl font-semibold'>Create Mapping</h2>

                    {/* Show datasource info */}
                    <div className='pt-2 pb-6 text-sm text-default-600'>
                        For datasource
                        {input.datasource.label}
                    </div>

                    <Input
                        label='Kind Name'
                        value={state.form.kindName}
                        onChange={e => dispatch({ type: 'kindName', value: e.target.value })}
                        placeholder='Enter Kind Name'
                        fullWidth
                        autoFocus
                    />
                </div>

                {/* Scrollable content area */}
                <div className='flex-1 overflow-y-auto py-2 mt-3'>
                    {state.editorPhase === EditorPhase.SelectRoot ? (
                        <RootSelectionPanel
                            selection={state.selection as FreeSelection}
                            graph={state.graph}
                            dispatch={dispatch}
                        />
                    ) : (
                        <AccessPathCard state={state} dispatch={dispatch} />
                    )}
                </div>

                {/* Fixed footer with buttons */}
                <div className='pt-4 border-t border-default-100'>
                    <div className='flex gap-3 justify-end'>
                        {onCancel && (
                            <Button
                                color='danger'
                                variant='flat'
                                onPress={onCancel}
                                startContent={<XMarkIcon className='h-4 w-4' />}
                                size='sm'
                            >
                            Discard
                            </Button>
                        )}

                        <Button
                            color='success'
                            variant='solid'
                            onPress={() => dispatch({ type: 'sync' })}
                            startContent={<CheckCircleIcon className='h-4 w-4' />}
                            size='sm'
                            isDisabled={!state.form.rootObjexKey}
                            isLoading={isFetching}
                        >
                            Create Mapping
                        </Button>
                    </div>
                </div>
            </div>

            {/* Main Graph Area */}
            <MappingEditorGraph state={state} dispatch={dispatch} className='grow' />
        </div>
    );
}

type RootSelectionPanelProps = {
    /** The current selection state. */
    selection: FreeSelection;
    graph: CategoryGraph;
    dispatch: MappingEditorDispatch;
};

/**
 * Renders a panel for selecting the root node during the SelectRoot phase.
 */
function RootSelectionPanel({ selection, graph, dispatch }: RootSelectionPanelProps) {
    const selectedNode = selection.nodeIds.size > 0
        ? graph.nodes.get([ ...selection.nodeIds ][0])
        : null;

    function setRoot() {
        if (!selection.isEmpty) {
            const rootNodeId = selection.nodeIds.values().next().value!;
            const rootNode = graph.nodes.get(rootNodeId)!;
            dispatch({ type: 'set-root', key: rootNode.schema.key });
        }
    }

    return (
        <div className='space-y-4'>
            <h3 className='text-lg font-semibold'>Select Root Node</h3>

            {selectedNode ? (
                <div className='bg-default-100 rounded-lg p-3'>
                    <div className='flex items-center justify-between'>
                        <div>
                            <p className='font-medium'>{selectedNode.metadata.label}</p>
                            <p className='text-xs text-default-500'>{selectedNode.schema.key.toString()}</p>
                        </div>
                        <Button
                            isIconOnly
                            size='sm'
                            variant='light'
                            onPress={() => dispatch({ type: 'select', operation: 'clear', range: 'nodes' })}
                        >
                            <XMarkIcon className='h-4 w-4' />
                        </Button>
                    </div>
                    <Button
                        fullWidth
                        color='primary'
                        className='mt-3'
                        onPress={setRoot}
                    >
                        Confirm Root Selection
                    </Button>
                </div>
            ) : (
                <div className='bg-default-100 rounded-lg p-3 text-center'>
                    <p className='text-default-500 text-sm'>
                        Click on a node in the graph to select it as root
                    </p>
                </div>
            )}

            {selection.edgeIds.size > 0 && (
                <div className='bg-warning-100 rounded-lg p-3 text-warning-800 text-sm'>
                    <p>Only nodes can be selected as root objects</p>
                </div>
            )}
        </div>
    );
}

type StateDispatchProps = {
    state: MappingEditorState;
    dispatch: MappingEditorDispatch;
};

/**
 * Renders a card for building and displaying the access path.
 */
function AccessPathCard({ state, dispatch }: StateDispatchProps) {
    const { form, selection, selectionType } = state;

    function handleAddSubpath() {
        // Switch to path selection mode when + is clicked
        dispatch({ type: 'add-subpath' });
    }

    function handleConfirmPath() {
        if (selection instanceof PathSelection && !selection.isEmpty) {
            const newNodeId = selection.lastNodeId;
            dispatch({ type: 'append-to-access-path', nodeId: newNodeId });
        }
    }

    function handleDeleteSubpath(index: number) {
        dispatch({
            type: 'remove-from-access-path',
            subpathIndex: index,
        });
    }

    function renderAccessPath() {
        const root = form.accessPath;

        return (
            <div className='text-sm text-default-800'>
                <div>{root.name.toString()}: {'{'}</div>
                {root.subpaths.map((subpath, index) => (
                    <div key={index} className='ml-6 my-1 group'>
                        <span className='inline-flex items-center'>
                            <span className='flex-1'>
                                {subpath.name.toString()}: {subpath.signature.toString()},
                            </span>
                            <span
                                role='button'
                                aria-label={`Delete subpath ${subpath.name.toString()}`}
                                className='inline-flex items-center justify-center w-5 h-5 ml-1 rounded-full opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer hover:bg-danger-100 text-danger-500 hover:text-danger-700'
                                onClick={() => handleDeleteSubpath(index)}
                                onKeyDown={e => {
                                    if (e.key === 'Enter' || e.key === ' ') {
                                        e.preventDefault();
                                        handleDeleteSubpath(index);
                                    }
                                }}
                                tabIndex={0}
                            >
                                <XMarkIcon className='w-3 h-3' />
                            </span>
                        </span>
                    </div>
                ))}
                <div className='flex items-start'>
                    <Button
                        isIconOnly
                        size='sm'
                        variant='solid'
                        onPress={handleAddSubpath}
                        color='primary'
                        className='ml-6 mt-1'
                        radius='sm'
                        isDisabled={selectionType !== SelectionType.Free}
                    >
                        <PlusIcon className='w-4 h-4' />
                    </Button>
                </div>
                <span>{'}'}</span>
            </div>
        );
    }

    return (
        <div className='space-y-4'>
            <h3 className='text-lg font-semibold'>Access Path</h3>

            <div className='bg-default-100 rounded-lg p-3'>
                <div className='overflow-x-auto'>
                    {renderAccessPath()}
                </div>

                {selectionType === SelectionType.Path && (
                    <div className='mt-3'>
                        {selection instanceof PathSelection && !selection.isEmpty ? (<>
                            <p className='text-default-800 truncate'>
                                Selected: {state.graph.nodes.get(selection.lastNodeId)?.metadata.label}
                            </p>
                            <div className='overflow-x-auto whitespace-nowrap text-sm text-default-600 mb-2'>
                                Path: {getPathSignature(state.graph, selection).toString()}
                            </div>
                            <Button
                                fullWidth
                                color='primary'
                                onPress={handleConfirmPath}
                            >
                                Add to Path
                            </Button>
                        </>) : (
                            <p className='text-sm text-default-500'>Select a path in the graph</p>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
