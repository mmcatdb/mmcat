import { EditMappingGraphDisplay } from './EditMappingGraphDisplay';
import { createInitialState, type EditMappingAction, editMappingReducer, type EditMappingState, EditorPhase } from './editMappingReducer';
import { type Category } from '@/types/schema';
import { useCallback, useReducer } from 'react';
import { FreeSelection, type FreeSelectionAction, PathSelection, SelectionType } from '../graph/graphSelection';
import { SelectionCard } from '../category/SelectionCard';
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
                <div className='absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-background p-4 rounded-lg shadow-lg z-20'>
                    <p className='text-lg font-semibold text-default-800'>Please select a root node</p>
                    {state.selection instanceof FreeSelection && !state.selection.isEmpty && (
                        <>
                            <SelectionCard selection={state.selection} graph={state.graph} dispatch={freeSelectionDispatch} />
                            <Button size='sm' color='primary' onPress={handleSetRoot} className='mt-2 w-full'>
                                OK
                            </Button>
                        </>
                    )}
                </div>
            )}

            <AccessPathCard state={state} dispatch={dispatch} />

            {state.editorPhase === EditorPhase.BuildPath && (
                <div className='absolute bottom-2 right-2 z-20 p-3 bg-background flex gap-2'>
                    <Button color='primary' size='sm' onPress={handleSave}>
                        Finish Mapping
                    </Button>
                    <Button color='default' variant='ghost' size='sm' onPress={handleCancel}>
                        Cancel
                    </Button>
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
        <div className='absolute bottom-2 left-2 z-20 w-[300px] p-3 bg-background'>
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
            {/* {selectionType === SelectionType.Free && (
                <div className='mt-3 text-sm text-default-500'>
                    <p>Click on + button to add properties.</p>
                </div>
            )} */}
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
