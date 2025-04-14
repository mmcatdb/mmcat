import { useEffect, useRef, useState } from 'react';
import { Button, Input } from '@nextui-org/react';
import { LeftPanelMode, type EditCategoryDispatch, type EditCategoryState } from './editCategoryReducer';
import { cn } from '../utils';
import { toPosition } from '@/types/utils/common';
import { Cardinality } from '@/types/schema/Morphism';
import { Key } from '@/types/identifiers/Key';
import { categoryToGraph } from './categoryGraph';

type StateDispatchProps = Readonly<{
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

type LeftPanelEditorProps = StateDispatchProps & Readonly<{
    className?: string;
}>;

export function LeftPanelCategoryEditor({ state, dispatch, className }: LeftPanelEditorProps) {
    const Component = components[state.leftPanelMode];

    return (
        <div className={cn('p-3 flex flex-col gap-3', className)}>
            <Component state={state} dispatch={dispatch} />
        </div>
    );
}

const components: Record<LeftPanelMode, (props: StateDispatchProps) => JSX.Element> = {
    [LeftPanelMode.default]: DefaultDisplay,
    [LeftPanelMode.createObjex]: CreateObjexDisplay,
    [LeftPanelMode.createMorphism]: CreateMorphismDisplay,
};

function DefaultDisplay({ state, dispatch }: StateDispatchProps) {
    const isValidSelection = state.selection.nodeIds.size <= 2 && state.selection.edgeIds.size === 0;

    return (<>
        <h3>Default</h3>

        <Button onClick={() => dispatch({ type: 'leftPanelMode', mode: LeftPanelMode.createObjex })}>
            Create object
        </Button>

        <Button
            onClick={() => dispatch({ type: 'leftPanelMode', mode: LeftPanelMode.createMorphism })}
            isDisabled={!isValidSelection}
        >
            Create Morphism
        </Button>
    </>);
}

function CreateObjexDisplay({ state, dispatch }: StateDispatchProps) {
    const [ label, setLabel ] = useState('');
    const position = { x: 0, y: 0 };
    const inputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (inputRef.current)
            inputRef.current.focus();
    }, []);

    function createObjex() {
        state.evocat.createObjex({ label, position: toPosition(position) });
        const graph = categoryToGraph(state.evocat.category);
        dispatch({ type: 'createObjex', graph });
    }

    function handleKeyDown(event: React.KeyboardEvent<HTMLInputElement>) {
        if (event.key === 'Enter')
            createObjex();
    }

    return (<>
        <h3>Create object</h3>

        <Input
            label='Label'
            value={label}
            onChange={e => setLabel(e.target.value)}
            onKeyDown={handleKeyDown}
            ref={inputRef}
        />

        <div className='grid grid-cols-2 gap-2'>
            <Button onClick={() => dispatch({ type: 'leftPanelMode', mode: LeftPanelMode.default })}>
                Cancel
            </Button>

            <Button color='primary' onClick={createObjex} isDisabled={label === ''}>
                Add
            </Button>
        </div>
    </>);
}

export function CreateMorphismDisplay({ state, dispatch }: StateDispatchProps) {
    const [ label, setLabel ] = useState('');
    const inputRef = useRef<HTMLInputElement>(null);

    // Extract selected nodes (should be exactly 2)
    const selectedNodes = Array.from(state.selection.nodeIds);
    const isValidSelection = selectedNodes.length === 2 && state.selection.edgeIds.size === 0;

    const domainNode = state.graph.nodes.get(selectedNodes[0]);
    const codomainNode = state.graph.nodes.get(selectedNodes[1]);

    // focus on input when both domain and codomain are selected
    useEffect(() => {
        if (isValidSelection && inputRef.current)
            inputRef.current.focus();

    }, [ isValidSelection ]);

    function createMorphism() {
        if (!isValidSelection || !label)
            return;

        const domKey = Key.createNew(Number(selectedNodes[0]));
        const codKey = Key.createNew(Number(selectedNodes[1]));

        state.evocat.createMorphism({
            domKey, // Source object
            codKey, // Target object
            min: Cardinality.One,
            label,
        });

        const graph = categoryToGraph(state.evocat.category);

        dispatch({ type: 'createMorphism', graph });
    }

    function handleKeyDown(event: React.KeyboardEvent<HTMLInputElement>) {
        if (event.key === 'Enter')
            createMorphism();
    }

    return (<>
        <h3 className='text-lg font-semibold py-2'>Create Morphism</h3>

        <div>
            <p className='pb-2'>
                Domain object: <span className={cn(
                    'font-semibold',
                    !domainNode ? 'font-bold text-danger-500' : '',
                )}>
                    {domainNode?.metadata.label ?? ' Select a node'}
                </span>
            </p>

            <p className='pb-2'>
                Codomain object: <span className={cn(
                    'font-semibold',
                    domainNode && !codomainNode ? 'font-bold text-danger-500' : '',
                )}>
                    {codomainNode?.metadata.label ?? ' Hold Ctrl and Select a second node'}
                </span>
            </p>
        </div>

        <Input
            label='Label'
            value={label}
            onChange={e => setLabel(e.target.value)}
            onKeyDown={handleKeyDown}
            ref={inputRef}
        />

        <div className='grid grid-cols-2 gap-2'>
            <Button onClick={() => dispatch({ type: 'leftPanelMode', mode: LeftPanelMode.default })}>
                Cancel
            </Button>

            <Button color='primary' onClick={createMorphism} isDisabled={!isValidSelection || label === ''}>
                Add
            </Button>
        </div>
    </>);
}
