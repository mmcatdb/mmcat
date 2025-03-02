import { EditMappingGraphDisplay } from './EditMappingGraphDisplay';
import { createInitialState, type EditMappingDispatch, editMappingReducer, type EditMappingState } from './editMappingReducer';
import { type Category } from '@/types/schema';
import { useCallback, useReducer } from 'react';
import { type FreeSelectionAction } from '../graph/graphSelection';
import { SelectionCard } from '../category/SelectionCard';
import { type Mapping } from '@/types/mapping';

type MappingEditorProps = Readonly<{
    category: Category;
    mapping: Mapping;
}>;

export function MappingEditor({ category, mapping }: MappingEditorProps) {
    const [ state, dispatch ] = useReducer(editMappingReducer, { category, mapping }, createInitialState);

    const userSelectionDispatch = useCallback((action: FreeSelectionAction) => dispatch({ type: 'select', ...action }), [ dispatch ]);

    return (
        <div className='relative h-[700px] flex'>
            <EditMappingGraphDisplay state={state} dispatch={dispatch} className='w-full h-full flex-grow' />

            {(state.selection.nodeIds.size > 0 || state.selection.edgeIds.size > 0) && (
                <div className='z-20 absolute top-2 right-2'>
                    <SelectionCard state={state} dispatch={userSelectionDispatch} />
                </div>
            )}

            {/* TODO */}

            {/* <PhasedEditor state={state} dispatch={dispatch} className='w-80 z-20 absolute bottom-2 left-2' /> */}

            <AccessPathCard state={state} dispatch={dispatch} />

            {/* <div className='absolute bottom-2 right-2'>
                <SaveButton state={state} dispatch={dispatch} />
            </div> */}

            <PathCard state={state} dispatch={dispatch} />

        </div>
    );
}

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
    if (!state.path)
        return null;

    return (
        <div className='absolute top-2 left-2 z-20 p-3 flex gap-3 bg-black'>
            {state.path.selection.nodes.map((node, index) => (
                <div key={index}>
                    {node.metadata.label}
                </div>
            ))}
        </div>
    );
}
