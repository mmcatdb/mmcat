import { type KeyboardEvent as ReactKeyboardEvent, type RefObject, useEffect, useRef, useState } from 'react';
import { Button, Input } from '@heroui/react';
import { EditorPhase, type CategoryEditorDispatch, type CategoryEditorState } from './useCategoryEditor';
import { toPosition } from '@/types/utils/common';
import { Cardinality } from '@/types/schema/Morphism';
import { ObjexIds } from '@/types/identifiers';
import { useSave } from './SaveContext';
import { FaSave } from 'react-icons/fa';
import { ExclamationTriangleIcon } from '@heroicons/react/20/solid';
import { cn } from '@/components/utils';
import { SpinnerButton } from '../../common';
import { FreeSelection } from '../graph/selection';

type StateDispatchProps = {
    /** The current state of the category editor. */
    state: CategoryEditorState;
    /** The dispatch function for updating the editor state. */
    dispatch: CategoryEditorDispatch;
};

type LeftPanelEditorProps = StateDispatchProps & {
    /** Optional CSS class for custom styling. */
    className?: string;
};

/**
 * Renders the left panel for editing a category, dynamically displaying content based on the current mode.
 */
export function LeftPanelCategoryEditor({ state, dispatch, className }: LeftPanelEditorProps) {
    const Component = components[state.leftPanelMode];

    // Add keyboard shortcuts for Ctrl+O (Create Objex) and Ctrl+M (Create Morphism)
    useEffect(() => {
        // Only handle shortcuts in default mode to avoid conflicts with input fields
        if (state.leftPanelMode !== EditorPhase.default)
            return;

        function handleKeyDown(event: KeyboardEvent) {
            // Check for Ctrl key (or Cmd on Mac) and the specific key
            if (event.ctrlKey || event.metaKey) {
                if (event.key === 'o' || event.key === 'O') {
                    event.preventDefault(); // Prevent browser shortcuts (e.g., open file)
                    dispatch({ type: 'phase', phase: EditorPhase.createObjex });
                }
                else if (event.key === 'm' || event.key === 'M') {
                    event.preventDefault();
                    dispatch({ type: 'phase', phase: EditorPhase.createMorphism });
                }
            }
        }

        document.addEventListener('keydown', handleKeyDown);
        return () => document.removeEventListener('keydown', handleKeyDown);
    }, [ state.leftPanelMode, dispatch ]);

    return (
        <div className={cn('px-3 py-2 flex flex-col gap-3', className)}>
            <Component state={state} dispatch={dispatch} />
        </div>
    );
}

/**
 * Mapping of left panel modes to their respective display components.
 */
const components: Record<EditorPhase, (props: StateDispatchProps) => JSX.Element> = {
    [EditorPhase.default]: DefaultDisplay,
    [EditorPhase.createObjex]: CreateObjexDisplay,
    [EditorPhase.createMorphism]: CreateMorphismDisplay,
};

/**
 * Displays the default mode of the left panel, offering options to create objexes or morphisms.
 */
function DefaultDisplay({ state, dispatch }: StateDispatchProps) {
    const { hasUnsavedChanges, isSaving, handleSave } = useSave();
    const category = state.evocat.category;

    return (<>
        <h3 className='text-lg font-semibold py-2 text-default-800 truncate max-w-[180px]'>
            {category.label}
        </h3>

        <Button
            title='Create object (Ctrl+O)'
            onPress={() => dispatch({ type: 'phase', phase: EditorPhase.createObjex })}
            color='default'
        >
            Create object
        </Button>

        <Button
            title='Create morphism (Ctrl+M)'
            onPress={() => dispatch({ type: 'phase', phase: EditorPhase.createMorphism })}
            color='default'
        >
            Create Morphism
        </Button>

        {hasUnsavedChanges && (
            <div className='mt-4 animate-fade-in'>
                <div className='flex flex-col gap-2 p-3 bg-warning-100 rounded-lg border border-warning-300'>
                    <div className='flex items-center gap-2 text-warning-800'>
                        <ExclamationTriangleIcon className='size-4 shrink-0' />
                        <span className='text-sm font-medium'>You have unsaved changes.</span>
                    </div>
                    <SpinnerButton
                        variant='solid'
                        color='warning'
                        size='sm'
                        fullWidth
                        onPress={() => handleSave()}
                        isFetching={isSaving}
                        startContent={isSaving ? null : <FaSave className='size-3.5' />}
                        className='shadow-xs hover:shadow-md transition-shadow'
                    >
                        {isSaving ? 'Saving...' : 'Save Changes'}
                    </SpinnerButton>
                </div>
            </div>
        )}
    </>);
}

type EditorFormProps = {
    label: string;
    setLabel: (value: string) => void;
    onSubmit: () => void;
    onCancel: () => void;
    inputRef: RefObject<HTMLInputElement>;
    isSubmitDisabled: boolean;
    placeholder?: string;
};

/**
 * Reusable form component for creating objexes and morphisms with consistent input and button behavior.
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
    // Handles Enter key press to trigger form submission and Escape for cancel.
    function handleKeyDown(event: ReactKeyboardEvent<HTMLInputElement>) {
        if (event.key === 'Enter')
            onSubmit();
        if (event.key === 'Escape')
            onCancel();
    }

    return (<>
        <Input
            label='Label'
            value={label}
            onChange={e => setLabel(e.target.value)}
            onKeyDown={handleKeyDown}
            ref={inputRef}
            placeholder={placeholder}
        />
        <div className='grid grid-cols-2 gap-2'>
            <Button onPress={onCancel}>Cancel</Button>
            <Button color='primary' onPress={onSubmit} isDisabled={isSubmitDisabled}>
                Add
            </Button>
        </div>
    </>);
}

/**
 * Renders a form to create a new objex with a label and fixed position.
 */
function CreateObjexDisplay({ state, dispatch }: StateDispatchProps) {
    const [ label, setLabel ] = useState('');
    const inputRef = useRef<HTMLInputElement>(null);

    // Auto-focus the input field when the component mounts
    useEffect(() => {
        inputRef.current?.focus();
    }, []);

    // Calculate position for the new objex
    function getNewObjexPosition() {
        const nodes = state.graph.nodes.values().toArray();
        const index = nodes.length % 9; // Reset to 0 after 9 objexes
        const gridSize = 3; // 3x3
        const spacing = 20;

        return {
            x: ((index % gridSize) * spacing) - spacing,
            y: (Math.floor(index / gridSize) * spacing) - spacing,
        };
    }

    function createObjex() {
        const key = state.evocat.createObjex({
            label: label,
            position: toPosition(getNewObjexPosition()),
            ids: ObjexIds.empty(),
        });

        dispatch({ type: 'objex', selectKey: key });
    }

    return (<>
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
    </>);
}

function getMorphismSelection(state: CategoryEditorState) {
    if (!(state.selection instanceof FreeSelection))
        return { isValidSelection: false, domainNode: undefined, codomainNode: undefined };

    const selectedNodes = [ ...state.selection.nodeIds ];
    const isValidSelection = selectedNodes.length === 2 && state.selection.edgeIds.size === 0;
    const domainNode = state.graph.nodes.get(selectedNodes[0]);
    const codomainNode = selectedNodes[1] ? state.graph.nodes.get(selectedNodes[1]) : undefined;

    return { isValidSelection, domainNode, codomainNode };
}

/**
 * Resets the left panel to its default mode.
 */
function resetToDefaultMode(dispatch: CategoryEditorDispatch) {
    dispatch({ type: 'phase', phase: EditorPhase.default });
}

/**
 * Renders a form to create a new morphism between two selected nodes.
 */
export function CreateMorphismDisplay({ state, dispatch }: StateDispatchProps) {
    const [ label, setLabel ] = useState('');
    const inputRef = useRef<HTMLInputElement>(null);
    const { isValidSelection, domainNode, codomainNode } = getMorphismSelection(state);

    // Auto-focus the input field when two nodes are selected
    useEffect(() => {
        if (isValidSelection && inputRef.current)
            inputRef.current.focus();
    }, [ isValidSelection ]);

    function createMorphism() {
        if (!isValidSelection)
            return;

        const signature = state.evocat.createMorphism({
            domKey: domainNode!.schema.key,
            codKey: codomainNode!.schema.key,
            min: Cardinality.One,
            label,
        });

        dispatch({ type: 'morphism', selectSignature: signature });
    }

    return (<>
        <h3 className='text-lg font-semibold py-2'>Create Morphism</h3>

        <div>
            <p className='pb-2'>
                Domain object:{' '}
                <span className={domainNode ? 'font-semibold' : 'font-bold text-danger-500'}>
                    {domainNode?.metadata.label ?? 'Select first node'}
                </span>
            </p>

            <p className='pb-2'>
                Codomain object:{' '}
                <span className={(!domainNode || codomainNode) ? 'font-semibold' : 'font-bold text-danger-500'}>
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
