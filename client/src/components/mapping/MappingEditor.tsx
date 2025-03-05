import { EditMappingGraphDisplay } from './EditMappingGraphDisplay';
import { createInitialState, type EditMappingDispatch, editMappingReducer, type EditMappingState } from './editMappingReducer';
import { type Category } from '@/types/schema';
import { useCallback, useReducer } from 'react';
import { FreeSelection, type FreeSelectionAction, PathSelection, SelectionType } from '../graph/graphSelection';
import { SelectionCard } from '../category/SelectionCard';
import { type Mapping } from '@/types/mapping';
import { Select, SelectItem, type SharedSelection } from '@nextui-org/react';

type MappingEditorProps = Readonly<{
    category: Category;
    mapping: Mapping;
}>;

export function MappingEditor({ category, mapping }: MappingEditorProps) {
    const [ state, dispatch ] = useReducer(editMappingReducer, { category, mapping }, createInitialState);

    const freeSelectionDispatch = useCallback((action: FreeSelectionAction) => dispatch({ type: 'select', ...action }), [ dispatch ]);

    const selectSelectionType = useCallback((keys: SharedSelection) => {
        dispatch({ type: 'selection-type', selectionType: (keys as Set<SelectionType>).values().next().value! });
    }, []);

    return (
        <div className='relative h-[700px] flex'>
            <EditMappingGraphDisplay state={state} dispatch={dispatch} className='w-full h-full flex-grow' />

            {state.selection instanceof FreeSelection && !state.selection.isEmpty && (
                <div className='z-20 absolute top-2 right-2 bg-black'>
                    <SelectionCard selection={state.selection} graph={state.graph} dispatch={freeSelectionDispatch} />
                </div>
            )}

            {/* TODO */}

            {/* <LeftPanelEditor state={state} dispatch={dispatch} className='w-80 z-20 absolute bottom-2 left-2' /> */}

            <AccessPathCard state={state} dispatch={dispatch} />

            {/* <div className='absolute bottom-2 right-2'>
                <SaveButton state={state} dispatch={dispatch} />
            </div> */}

            <PathCard state={state} dispatch={dispatch} />

            <div className='absolute bottom-2 right-2 z-20 p-3 bg-black'>
                {/* Just a temporary setter for testing. */}
                <Select
                    label='Selection type'
                    defaultSelectedKeys={[ state.selectionType ]}
                    onSelectionChange={selectSelectionType}
                    disallowEmptySelection
                    size='sm'
                    className='w-[200px]'
                >
                    {selectionTypes.map(type => (
                        <SelectItem key={type}>
                            {type}
                        </SelectItem>
                    ))}
                </Select>
            </div>
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
    dispatch: EditMappingDispatch;
}>;

function AccessPathCard({ state }: StateDispatchProps) {
    return (
        <div className='absolute bottom-2 left-2 z-20 w-[300px] p-3 bg-black'>
            <h3 className='text-white'>Access path</h3>

            <pre className='mt-3'>
                {state.mapping.accessPath.toString()}
            </pre>
        </div>
    );
}

function PathCard({ state }: StateDispatchProps) {
    const { selection } = state;

    if (!(selection instanceof PathSelection) || selection.isEmpty)
        return null;

    return (
        <div className='absolute top-2 left-2 z-20 p-3 flex gap-3 bg-black'>
            {selection.nodeIds.map((nodeIds, index) => (
                <div key={index}>
                    {state.graph.nodes.get(nodeIds)!.metadata.label}
                </div>
            ))}
        </div>
    );
}
