import { Button, Input } from '@nextui-org/react';
import { type EditCategoryDispatch, type EditCategoryState } from './editCategoryReducer';
import { type DefaultState, type PhasedEditorAction, EditorPhase, type CreateObjectState, type PhasedEditorState, type PhasedState, type CreateObjectAction, type DefaultAction } from './phasedEditorReducer';
import { type Dispatch, useCallback } from 'react';
import { cn } from '../utils';

type PhasedEditorProps = Readonly<{
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
    className?: string;
}>;

export function PhasedEditor({ state, dispatch, className }: PhasedEditorProps) {
    const phase = state.editor.phase;
    const phasedDispatch = useCallback((action: Omit<PhasedEditorAction, 'type'>) => dispatch({ type: phase, ...action } as PhasedEditorAction), [ phase, dispatch ]);

    return (
        <div className={cn('border p-3 flex flex-col gap-3', className)}>
            {switchComponents(phase, state, phasedDispatch)}
        </div>
    );
}

function switchComponents(phase: EditorPhase, state: EditCategoryState, phasedDispatch: Dispatch<Omit<PhasedEditorAction, 'type'>>) {
    switch (phase) {
    case EditorPhase.default: return <Default state={state as PhasedState<DefaultState>} dispatch={phasedDispatch} />;
    case EditorPhase.createObject: return <CreateObject state={state as PhasedState<CreateObjectState>} dispatch={phasedDispatch} />;
    }
}

type StateDispatchProps<TState extends PhasedEditorState, TAction> = Readonly<{
    state: PhasedState<TState>;
    dispatch: Dispatch<TAction>;
}>;

function Default({ state, dispatch }: StateDispatchProps<DefaultState, DefaultAction>) {
    return (<>
        <h3>Default</h3>
        <Button onClick={() => dispatch({ operation: 'createObject' })}>Create object</Button>
    </>);
}

function CreateObject({ state, dispatch }: StateDispatchProps<CreateObjectState, CreateObjectAction>) {

    function finish() {
        state.evocat.createObjex({ label: state.editor.label, position: state.editor.position });
        dispatch({ operation: 'finish' });
    }

    return (<>
        <h3>Create object</h3>

        <Input
            label='Label'
            value={state.editor.label}
            onChange={e => dispatch({ operation: 'label', value: e.target.value })}
        />

        <div className='flex gap-2'>
            <Button onClick={() => dispatch({ operation: 'cancel' })}>
                Cancel
            </Button>

            <Button color='primary' onClick={finish} disabled={state.editor.label === ''}>
                Finish
            </Button>
        </div>
    </>);
}
