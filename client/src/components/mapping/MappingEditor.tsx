import { EditMappingGraphDisplay } from './EditMappingGraphDisplay';
import { createInitialState, type EditMappingAction, editMappingReducer, type EditMappingState, EditorPhase } from './editMappingReducer';
import { type Category } from '@/types/schema';
import { type Dispatch, useCallback, useReducer } from 'react';
import { FreeSelection, type FreeSelectionAction, PathSelection, SelectionType } from '../graph/graphSelection';
import { type Mapping } from '@/types/mapping';
import { Button } from '@nextui-org/react';
import { useNavigate } from 'react-router-dom';
import { PlusIcon } from '@heroicons/react/20/solid';

type MappingEditorProps = Readonly<{
    category: Category;
    mapping: Mapping;
    onSave?: (mapping: Mapping) => void;
}>;

export function MappingEditor({ category, mapping, onSave }: MappingEditorProps) {
    const [ state, dispatch ] = useReducer(editMappingReducer, { category, mapping }, createInitialState);
    const navigate = useNavigate();

    const freeSelectionDispatch = useCallback((action: FreeSelectionAction) => {
        dispatch({ type: 'select', ...action });
    }, [ dispatch ]);

    function handleSetRoot() {
        if (state.selection instanceof FreeSelection && !state.selection.isEmpty) {
            const rootNodeId = state.selection.nodeIds.values().next().value;
            dispatch({ type: 'set-root', rootNodeId });
        }
    }

    function handleSave() {
        if (onSave) 
            onSave(state.mapping);
        navigate(-1);
    }

    function handleCancel() {
        navigate(-1);
    }

    return (
        <div className='relative h-[700px] flex'>
            <EditMappingGraphDisplay state={state} dispatch={dispatch} className='w-full h-full flex-grow' />
    
            {state.editorPhase === EditorPhase.SelectRoot && (
                <RootSelectionPanel 
                    selection={state.selection} 
                    graph={state.graph}
                    dispatch={freeSelectionDispatch}
                    onConfirm={handleSetRoot}
                />
            )}
    
            {state.editorPhase !== EditorPhase.SelectRoot && (
                <AccessPathCard state={state} dispatch={dispatch} />
            )}

            {state.editorPhase === EditorPhase.BuildPath && (
                <div className='absolute bottom-2 right-2 bg-content1 rounded-xl shadow-lg z-20 p-4 space-y-3'>
                    <div className='flex gap-3'>
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
                        >
                            Create Mapping
                        </Button>
                    </div>
                </div>
            )}
        </div>
    );
}

import { CheckCircleIcon, XMarkIcon } from '@heroicons/react/20/solid';
import { type CategoryGraph } from '../category/categoryGraph';

type RootSelectionPanelProps = {
    selection: FreeSelection;
    graph: CategoryGraph;
    dispatch: Dispatch<FreeSelectionAction>;
    onConfirm: () => void;
};

function RootSelectionPanel({ selection, graph, dispatch, onConfirm }: RootSelectionPanelProps) {
    const selectedNode = selection.nodeIds.size > 0 
        ? graph.nodes.get([ ...selection.nodeIds ][0])
        : null;

    return (
        <div className='absolute bottom-2 left-2 w-80 bg-content1 rounded-xl shadow-lg z-20 p-4 space-y-4'>
            <div className='flex items-center justify-between'>
                <h3 className='text-lg font-semibold'>Select Root Node</h3>
                {selectedNode && (
                    <Button 
                        size='sm' 
                        color='success' 
                        variant='flat'
                        onPress={onConfirm}
                        startContent={<CheckCircleIcon className='h-4 w-4' />}
                    >
                        Confirm
                    </Button>
                )}
            </div>

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

function AccessPathCard({ state, dispatch }: StateDispatchProps) {
    const { mapping, selection, selectionType, editorPhase } = state;

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

    function renderAccessPath() {
        const root = mapping.accessPath;
        const subpaths = root.subpaths.reduce((acc: Record<string, string>, subpath) => {
            acc[subpath.name.toString()] = subpath.signature.toString();
            return acc;
        }, {});

        // json-like formatting of access path
        let result = `${root.name.toString()}: {\n`;
        for (const [ key, value ] of Object.entries(subpaths)) 
            result += `    ${key}: ${value},\n`;
        // result += '}';

        return result;
    }

    return (
        <div className='absolute bottom-2 left-2 w-80 bg-content1 rounded-xl shadow-lg z-20 p-4 space-y-4'>
            <h3>Access Path</h3>
            <div className='mt-3 space-y-2'>
                <pre className='text-sm text-default-800'>
                    {renderAccessPath()}
                    {editorPhase === EditorPhase.BuildPath && (
                        <Button
                            isIconOnly
                            size='sm'
                            variant='solid'
                            onPress={handleAddSubpath}
                            color='primary'
                            className='my-1 ml-7'
                            radius='sm'
                            isDisabled={selectionType !== SelectionType.Free}
                        >
                            <PlusIcon className='w-4 h-4' />
                        </Button>
                    )}
                    <div className='text-default-800'>{'}'}</div>
                </pre>
            </div>
            {selectionType === SelectionType.Path && (
                <div className='mt-3'>
                    {selection instanceof PathSelection && !selection.isEmpty ? (
                        <>
                            <p className='text-sm text-default-600'>
                                Selected: {state.graph.nodes.get(selection.lastNodeId)?.metadata.label}
                            </p>
                            <Button size='sm' color='primary' onPress={handleConfirmPath} className='mt-2'>
                                Add
                            </Button>
                        </>
                    ) : (
                        <p className='text-sm text-default-500'>Select a path in the graph</p>
                    )}
                </div>
            )}
            {selectionType === SelectionType.Free && editorPhase === EditorPhase.BuildPath && (
                <div className='mt-3 text-sm text-default-500'>
                    <p>Click on + button to add properties.</p>
                </div>
            )}
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
