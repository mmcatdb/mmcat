import { EditMappingGraphDisplay } from './EditMappingGraphDisplay';
import { createInitialState, type EditMappingAction, editMappingReducer, type EditMappingState, EditorPhase } from './editMappingReducer';
import { type Category } from '@/types/schema';
import { type Dispatch, useCallback, useReducer } from 'react';
import { FreeSelection, type FreeSelectionAction, PathSelection, SelectionType } from '../graph/graphSelection';
import { type Mapping } from '@/types/mapping';
import { Button, Input } from '@nextui-org/react';
import { useNavigate } from 'react-router-dom';
import { PlusIcon, CheckCircleIcon, XMarkIcon } from '@heroicons/react/20/solid';
import { type CategoryGraph } from '../category/categoryGraph';

type MappingEditorProps = Readonly<{
    /** The schema category being edited. */
    category: Category;
    /** The initial mapping to edit. */
    mapping: Mapping;
    kindName: string;
    setKindName: (name: string) => void;
    /** Optional callback to handle saving the mapping. */
    onSave?: (mapping: Mapping, kindName: string) => void;
    /** Name of the datasource for which the mapping is created. */
    datasourceLabel: string;
}>;

/**
 * Renders the mapping editor with a graph display and panels for root selection and access path building.
 */
export function MappingEditor({ category, mapping, kindName, setKindName, onSave, datasourceLabel }: MappingEditorProps) {
    const [ state, dispatch ] = useReducer(editMappingReducer, { category, mapping }, createInitialState);
    const navigate = useNavigate();

    const freeSelectionDispatch = useCallback((action: FreeSelectionAction) => {
        dispatch({ type: 'select', ...action });
    }, [ dispatch ]);

    function handleSetRoot() {
        if (state.selection instanceof FreeSelection && !state.selection.isEmpty) {
            const rootNodeId = state.selection.nodeIds.values().next().value;
            // @ts-expect-error FIXME
            dispatch({ type: 'set-root', rootNodeId });
        }
    }

    function handleSave() {
        if (onSave)
            onSave(state.mapping, kindName);
        navigate(-1);
    }

    function handleCancel() {
        navigate(-1);
    }

    return (
        <div className='relative flex h-[calc(100vh-40px)]'>
            {/* Left Panel - Form Controls */}
            <div className='w-80 bg-content1 border-r-1 border-default-200 p-4 flex flex-col'>
                <div className=''>
                    <h2 className='text-xl font-semibold'>Create Mapping</h2>

                    {/* Show datasource info */}
                    <div className='pt-2 pb-6 text-sm text-default-600'>
                        For datasource 
                        {datasourceLabel ?? mapping.datasourceId}
                    </div>

                    <Input
                        label='Kind Name'
                        value={kindName}
                        onChange={e => setKindName(e.target.value)}
                        placeholder='Enter Kind Name'
                        fullWidth
                        autoFocus
                    />
                </div>

                {/* <PathCard state={state} dispatch={dispatch} /> */}

                {/* Scrollable content area */}
                <div className='flex-1 overflow-y-auto py-2 mt-3'>
                    {state.editorPhase === EditorPhase.SelectRoot ? (
                        <RootSelectionPanel
                            // @ts-expect-error FIXME
                            selection={state.selection}
                            graph={state.graph}
                            dispatch={freeSelectionDispatch}
                            onConfirm={handleSetRoot}
                        />
                    ) : (
                        <AccessPathCard state={state} dispatch={dispatch} />
                    )}
                </div>

                {/* Fixed footer with buttons */}
                <div className='pt-4 border-t-1 border-default-100'>
                    <div className='flex gap-3 justify-end'>
                        <Button
                            color='danger'
                            variant='flat'
                            onPress={handleCancel}
                            startContent={<XMarkIcon className='h-4 w-4' />}
                            size='sm'
                        >
                            Discard
                        </Button>
                        <Button
                            color='success'
                            variant='solid'
                            onPress={handleSave}
                            startContent={<CheckCircleIcon className='h-4 w-4' />}
                            size='sm'
                            isDisabled={!state.rootNodeId}
                        >
                            Create Mapping
                        </Button>
                    </div>
                </div>
            </div>

            {/* Main Graph Area */}
            <EditMappingGraphDisplay state={state} dispatch={dispatch} className='flex-grow' />
        </div>
    );
}

type RootSelectionPanelProps = {
    /** The current selection state. */
    selection: FreeSelection;
    graph: CategoryGraph;
    /** Dispatch function for selection actions. */
    dispatch: Dispatch<FreeSelectionAction>;
    /** Callback to confirm the root node. */
    onConfirm: () => void;
};

/**
 * Renders a panel for selecting the root node during the SelectRoot phase.
 */
function RootSelectionPanel({ selection, graph, dispatch, onConfirm }: RootSelectionPanelProps) {
    const selectedNode = selection.nodeIds.size > 0
        ? graph.nodes.get([ ...selection.nodeIds ][0])
        : null;

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
                            onClick={() => dispatch({ operation: 'clear', range: 'nodes' })}
                        >
                            <XMarkIcon className='h-4 w-4' />
                        </Button>
                    </div>
                    <Button
                        fullWidth
                        color='primary'
                        className='mt-3'
                        onPress={onConfirm}
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

/**
 * List of available selection types.
 */
const selectionTypes = [
    SelectionType.None,
    SelectionType.Free,
    SelectionType.Sequence,
    SelectionType.Path,
];

type StateDispatchProps = Readonly<{
    state: EditMappingState;
    dispatch: React.Dispatch<EditMappingAction>;
}>;

/**
 * Renders a card for building and displaying the access path.
 */
function AccessPathCard({ state, dispatch }: StateDispatchProps) {
    const { mapping, selection, selectionType } = state;

    function handleAddSubpath() {
        // Switch to path selection mode when + is clicked
        dispatch({ type: 'selection-type', selectionType: SelectionType.Path });
    }

    function handleConfirmPath() {
        if (selection instanceof PathSelection && !selection.isEmpty) {
            const newNodeId = selection.lastNodeId;
            dispatch({ type: 'append-to-access-path', nodeId: newNodeId });
            // Reset to free selection after adding
            dispatch({ type: 'selection-type', selectionType: SelectionType.Free });
        }
    }

    function handleDeleteSubpath(index: number) {
        dispatch({ 
            type: 'remove-from-access-path', 
            subpathIndex: index, 
        });
    }

    function renderAccessPath() {
        const root = mapping.accessPath;
        
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
                        {selection instanceof PathSelection && !selection.isEmpty ? (
                            <>
                                <p className='text-sm text-default-600 mb-2 truncate'>
                                    Selected: {state.graph.nodes.get(selection.lastNodeId)?.metadata.label}
                                </p>
                                <Button 
                                    fullWidth 
                                    color='primary' 
                                    onPress={handleConfirmPath}
                                >
                                    Add to Path
                                </Button>
                            </>
                        ) : (
                            <p className='text-sm text-default-500'>Select a path in the graph</p>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

function PathCard({ state }: StateDispatchProps) {
    const { selection } = state;

    if (!(selection instanceof PathSelection) || selection.isEmpty)
        return null;

    return (
        <div className='absolute top-2 left-2 z-20 p-3 flex gap-3 bg-background'>
            {selection.nodeIds.map((nodeIds, index) => (
                <div key={index}>
                    {state.graph.nodes.get(nodeIds)!.metadata.label}
                </div>
            ))}
        </div>
    );
}