import { useEffect, useRef, useState } from 'react';
import { Button, Input } from '@nextui-org/react';
import { LeftPanelMode, type EditCategoryDispatch, type EditCategoryState } from './editCategoryReducer';
import { cn } from '../utils';
import { toPosition } from '@/types/utils/common';
import { Cardinality } from '@/types/schema/Morphism';
import { Key } from '@/types/identifiers';
import { categoryToGraph } from './categoryGraph';
import { useSave } from './SaveContext';
import { FaSave } from 'react-icons/fa';
import { ExclamationTriangleIcon } from '@heroicons/react/20/solid';

type StateDispatchProps = Readonly<{
    /** The current state of the category editor. */
    state: EditCategoryState;
    /** The dispatch function for updating the editor state. */
    dispatch: EditCategoryDispatch;
}>;

type LeftPanelEditorProps = StateDispatchProps & Readonly<{
    /** Optional CSS class for custom styling. */
    className?: string;
}>;

/**
 * Renders the left panel for editing a category, dynamically displaying content based on the current mode.
 */
export function LeftPanelCategoryEditor({ state, dispatch, className }: LeftPanelEditorProps) {
    const Component = components[state.leftPanelMode];

    // Add keyboard shortcuts for Ctrl+O (Create Object) and Ctrl+M (Create Morphism)
    useEffect(() => {
        // Only handle shortcuts in default mode to avoid conflicts with input fields
        if (state.leftPanelMode !== LeftPanelMode.default) 
            return;

        const handleKeyDown = (event: KeyboardEvent) => {
            // Check for Ctrl key (or Cmd on Mac) and the specific key
            if (event.ctrlKey || event.metaKey) {
                if (event.key === 'o' || event.key === 'O') {
                    event.preventDefault(); // Prevent browser shortcuts (e.g., open file)
                    dispatch({ type: 'leftPanelMode', mode: LeftPanelMode.createObjex });
                }
                else if (event.key === 'm' || event.key === 'M') {
                    event.preventDefault();
                    dispatch({ type: 'leftPanelMode', mode: LeftPanelMode.createMorphism });
                }
            }
        };

        document.addEventListener('keydown', handleKeyDown);
        return () => document.removeEventListener('keydown', handleKeyDown);
    }, [ state.leftPanelMode, dispatch ]);

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
    const { hasUnsavedChanges, isSaving, handleSave } = useSave();
    const category = state.evocat.category;

    return (
        <>
            <h3 className='text-lg font-semibold py-2 text-default-800 truncate max-w-[180px]'>
                {category.label}
            </h3>

            <Button
                title='Create object (Ctrl+O)'
                onClick={() => dispatch({ type: 'leftPanelMode', mode: LeftPanelMode.createObjex })}
                color='default'
            >
                Create object
            </Button>

            <Button
                title='Create morphism (Ctrl+M)'
                onClick={() => dispatch({ type: 'leftPanelMode', mode: LeftPanelMode.createMorphism })}
                color='default'
            >
                Create Morphism
            </Button>

            {hasUnsavedChanges && (
                <div className='mt-4 animate-fade-in'>
                    <div className='flex flex-col gap-2 p-3 bg-warning-100 rounded-lg border border-warning-300'>
                        <div className='flex items-center gap-2 text-warning-800'>
                            <ExclamationTriangleIcon className='h-4 w-4 flex-shrink-0' />
                            <span className='text-sm font-medium'>You have unsaved changes.</span>
                        </div>
                        <Button 
                            variant='solid'
                            color='warning'
                            size='sm'
                            fullWidth
                            onClick={() => handleSave()}
                            isLoading={isSaving}
                            startContent={isSaving ? null : <FaSave className='h-3.5 w-3.5' />}
                            className='shadow-sm hover:shadow-md transition-shadow'
                        >
                            {isSaving ? 'Saving...' : 'Save Changes'}
                        </Button>
                    </div>
                </div>
            )}
        </>
    );
}

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
    const inputRef = useRef<HTMLInputElement>(null);

    // Auto-focus the input field when the component mounts
    useEffect(() => {
        inputRef.current?.focus();
    }, []);

    // Calculate position for the new object
    const getNewObjectPosition = () => {
        const nodes = state.graph.nodes.values().toArray();
        const index = nodes.length % 9; // Reset to 0 after 9 objects
        const gridSize = 3; // 3x3
        const spacing = 20;
        return {
            x: ((index % gridSize) * spacing) - spacing,
            y: (Math.floor(index / gridSize) * spacing) - spacing,
        };
    };

    function createObjex() {
        state.evocat.createObjex({
            label: label,
            position: toPosition(getNewObjectPosition()),
        });
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
                isSubmitDisabled={false} // Label is optional
                placeholder='Enter object label (optional)'
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
    const codomainNode = selectedNodes[1] ? state.graph.nodes.get(selectedNodes[1]) : undefined;
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
        if (!isValidSelection) 
            return;

        const domId = Number(selectedNodes[0]);
        const codId = Number(selectedNodes[1]);
        if (isNaN(domId) || isNaN(codId)) 
            return;

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
                    className={cn('font-semibold', !domainNode && 'font-bold text-danger-500')}
                >
                    {domainNode?.metadata.label ?? 'Select first node'}
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
                    {codomainNode?.metadata.label ?? (domainNode ? 'Select second node' : 'Select first node first')}
                </span>
            </p>
        </div>

        <EditorForm
            label={label}
            setLabel={setLabel}
            onSubmit={createMorphism}
            onCancel={() => resetToDefaultMode(dispatch)}
            inputRef={inputRef}
            isSubmitDisabled={!isValidSelection}
            placeholder='Enter label (optional)'
        />
    </>);
}
