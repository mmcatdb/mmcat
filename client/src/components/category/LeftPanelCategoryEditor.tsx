import { useEffect, useRef, useState } from 'react';
import { Button, Input } from '@nextui-org/react';
import { LeftPanelMode, type EditCategoryDispatch, type EditCategoryState } from './editCategoryReducer';
import { cn } from '../utils';
import { toPosition } from '@/types/utils/common';
import { Cardinality } from '@/types/schema/Morphism';
import { Key } from '@/types/identifiers/Key';
import { categoryToGraph } from './categoryGraph';

/**
 * Props for components that receive editor state and dispatch.
 *
 * @property state - The current state of the category editor.
 * @property dispatch - The dispatch function for updating the editor state.
 */
type StateDispatchProps = Readonly<{
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

/**
 * Props for the LeftPanelCategoryEditor component.
 *
 * @extends StateDispatchProps
 * @property className - Optional CSS class for custom styling.
 */
type LeftPanelEditorProps = StateDispatchProps & Readonly<{
    className?: string;
}>;

/**
 * Renders the left panel for editing a category, dynamically displaying content based on the current mode.
 */
export function LeftPanelCategoryEditor({ state, dispatch, className }: LeftPanelEditorProps) {
    // Dynamically select the component based on the current left panel mode
    const Component = components[state.leftPanelMode];

    return (
        <div className={cn('p-3 flex flex-col gap-3', className)}>
            <Component state={state} dispatch={dispatch} />
        </div>
    );
}

/**
 * Mapping of left panel modes to their respective display components.
 */
const components: Record<LeftPanelMode, (props: StateDispatchProps) => JSX.Element> = {
    [LeftPanelMode.default]: DefaultDisplay,
    [LeftPanelMode.createObjex]: CreateObjexDisplay,
    [LeftPanelMode.createMorphism]: CreateMorphismDisplay,
};

/**
 * Displays the default mode of the left panel, offering options to create objects or morphisms.
 */
function DefaultDisplay({ state, dispatch }: StateDispatchProps) {
    // Disable morphism creation if more than 2 nodes or any edges are selected
    const isValidSelection = state.selection.nodeIds.size <= 2 && state.selection.edgeIds.size === 0;

    return (
        <>
            <h3 className='text-lg font-semibold py-2'>Default</h3>

            <Button
                onClick={() => dispatch({ type: 'leftPanelMode', mode: LeftPanelMode.createObjex })}
                color='primary'
            >
                Create object
            </Button>

            <Button
                onClick={() => dispatch({ type: 'leftPanelMode', mode: LeftPanelMode.createMorphism })}
                isDisabled={!isValidSelection}
                color='primary'
            >
                Create Morphism
            </Button>
        </>
    );
}

/**
 * Props for the reusable editor form component.
 */
type EditorFormProps = {
    label: string;
    setLabel: (value: string) => void;
    onSubmit: () => void;
    onCancel: () => void;
    inputRef: React.RefObject<HTMLInputElement>;
    isSubmitDisabled: boolean;
    placeholder?: string;
};

/**
 * Reusable form component for creating objects and morphisms with consistent input and button behavior.
 */
function EditorForm({
    label,
    setLabel,
    onSubmit,
    onCancel,
    inputRef,
    isSubmitDisabled,
    placeholder,
}: EditorFormProps) {
    // Handles Enter key press to trigger form submission.
    const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
        if (event.key === 'Enter') 
            onSubmit();
    };

    return (
        <>
            <Input
                label='Label'
                value={label}
                onChange={e => setLabel(e.target.value)}
                onKeyDown={handleKeyDown}
                ref={inputRef}
                placeholder={placeholder}
            />
            <div className='grid grid-cols-2 gap-2'>
                <Button onClick={onCancel}>Cancel</Button>
                <Button color='primary' onClick={onSubmit} isDisabled={isSubmitDisabled}>
                    Add
                </Button>
            </div>
        </>
    );
}

/**
 * Renders a form to create a new schema object (objex) with a label and fixed position.
 */
function CreateObjexDisplay({ state, dispatch }: StateDispatchProps) {
    const [ label, setLabel ] = useState('');
    // Fixed initial position for new objects
    const position = { x: 0, y: 0 };
    const inputRef = useRef<HTMLInputElement>(null);

    // Auto-focus the input field when the component mounts
    useEffect(() => {
        inputRef.current?.focus();
    }, []);

    /**
     * Creates a new schema object and updates the graph state.
     */
    function createObjex() {
        if (!label) 
            return; // Prevent creating with empty label
        state.evocat.createObjex({ label, position: toPosition(position) });
        const graph = categoryToGraph(state.evocat.category);
        dispatch({ type: 'createObjex', graph });
    }

    return (
        <>
            <h3 className='text-lg font-semibold py-2'>Create object</h3>
            <EditorForm
                label={label}
                setLabel={setLabel}
                onSubmit={createObjex}
                onCancel={() => resetToDefaultMode(dispatch)}
                inputRef={inputRef}
                isSubmitDisabled={label === ''}
                placeholder='Enter object label'
            />
        </>
    );
}

/**
 * Hook to extract and validate selected nodes for morphism creation.
 */
function useMorphismSelection(state: EditCategoryState) {
    const selectedNodes = Array.from(state.selection.nodeIds);
    const isValidSelection = selectedNodes.length === 2 && state.selection.edgeIds.size === 0;
    const domainNode = state.graph.nodes.get(selectedNodes[0]);
    const codomainNode = state.graph.nodes.get(selectedNodes[1]);
    return { selectedNodes, isValidSelection, domainNode, codomainNode };
}

/**
 * Resets the left panel to its default mode.
 */
function resetToDefaultMode(dispatch: EditCategoryDispatch) {
    dispatch({ type: 'leftPanelMode', mode: LeftPanelMode.default });
}

/**
 * Renders a form to create a new morphism between two selected nodes.
 */
export function CreateMorphismDisplay({ state, dispatch }: StateDispatchProps) {
    const [ label, setLabel ] = useState('');
    const inputRef = useRef<HTMLInputElement>(null);
    const { selectedNodes, isValidSelection, domainNode, codomainNode } = useMorphismSelection(state);

    // Auto-focus the input field when two nodes are selected
    useEffect(() => {
        if (isValidSelection && inputRef.current) 
            inputRef.current.focus();
        
    }, [ isValidSelection ]);

    function createMorphism() {
        if (!isValidSelection || !label) 
            return; // if invalid state

        const domId = Number(selectedNodes[0]);
        const codId = Number(selectedNodes[1]);
        if (isNaN(domId) || isNaN(codId)) 
            return; // if invalid node IDs

        const domKey = Key.createNew(domId);
        const codKey = Key.createNew(codId);

        // Create morphism with default cardinality and provided label
        state.evocat.createMorphism({
            domKey,
            codKey,
            min: Cardinality.One,
            label,
        });

        const graph = categoryToGraph(state.evocat.category);
        dispatch({ type: 'createMorphism', graph });
    }

    return (<>
        <h3 className='text-lg font-semibold py-2'>Create Morphism</h3>

        <div>
            <p className='pb-2'>
                    Domain object:{' '}
                <span
                    className={cn(
                        'font-semibold',
                        !domainNode && 'font-bold text-danger-500',
                    )}
                >
                    {domainNode?.metadata.label ?? 'Select a node'}
                </span>
            </p>

            <p className='pb-2'>
                    Codomain object:{' '}
                <span
                    className={cn(
                        'font-semibold',
                        domainNode && !codomainNode && 'font-bold text-danger-500',
                    )}
                >
                    {codomainNode?.metadata.label ?? 'Hold Ctrl and Select a second node'}
                </span>
            </p>
        </div>

        <EditorForm
            label={label}
            setLabel={setLabel}
            onSubmit={createMorphism}
            onCancel={() => resetToDefaultMode(dispatch)}
            inputRef={inputRef}
            isSubmitDisabled={!isValidSelection || label === ''}
            placeholder='Enter morphism label'
        />
    </>);
}
