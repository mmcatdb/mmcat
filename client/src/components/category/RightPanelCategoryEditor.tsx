import { useCallback, useEffect, useState } from 'react';
import { Button, Input, Radio, RadioGroup } from '@heroui/react';
import { RightPanelMode, type EditCategoryDispatch, type EditCategoryState } from './editCategoryReducer';
import { cn } from '../utils';
import { type FormPosition, toFormNumber, toNumber, toPosition } from '@/types/utils/common';
import { categoryToGraph } from './categoryGraph';
import { type FreeSelectionAction } from '../graph/FreeSelection';
import { SelectionCard } from './SelectionCard';
import { Cardinality, type Min } from '@/types/schema';

type StateDispatchProps = {
    /** The current state of the category editor. */
    state: EditCategoryState;
    /** The dispatch function for updating the editor state. */
    dispatch: EditCategoryDispatch;
};

type RightPanelEditorProps = StateDispatchProps & {
    /** Optional class name for additional styling. */
    className?: string;
};

/**
 * Renders the right panel for editing a category, dynamically displaying content based on the current selection and mode.
 */
export function RightPanelCategoryEditor({ state, dispatch, className }: RightPanelEditorProps) {
    // Dynamically select the component based on selection state
    const Component = getRightPanelComponent(state);

    return (
        <div className={cn('p-2 flex flex-col gap-3', className)}>
            <Component state={state} dispatch={dispatch} />
        </div>
    );
}

/**
 * Determines the appropriate display component based on the current selection.
 *
 * @param state - The current editor state.
 */
function getRightPanelComponent(state: EditCategoryState) {
    if (state.selection.nodeIds.size === 1 && state.selection.edgeIds.size === 0)
        return UpdateObjexDisplay;

    if (state.selection.nodeIds.size === 0 && state.selection.edgeIds.size === 1)
        return UpdateMorphismDisplay;

    return DefaultDisplay;
}

/**
 * Hook to extract and validate selection for the right panel.
 *
 * @param state - The current editor state.
 * @returns An object containing selected node and edge data, if valid.
 */
function useSelection(state: EditCategoryState) {
    const selectedNodeId = Array.from(state.selection.nodeIds)[0];
    const selectedEdgeId = Array.from(state.selection.edgeIds)[0];
    const selectedNode = selectedNodeId ? state.graph.nodes.get(selectedNodeId) : undefined;
    const selectedMorphism = selectedEdgeId ? state.graph.edges.get(selectedEdgeId) : undefined;
    const hasSelection = state.selection.nodeIds.size > 0 || state.selection.edgeIds.size > 0;

    return { selectedNode, selectedMorphism, hasSelection };
}

/**
 * Resets the right panel to its default mode by clearing the selection.
 *
 * @param dispatch - The dispatch function for updating the editor state.
 */
function resetToDefaultMode(dispatch: EditCategoryDispatch) {
    dispatch({ type: 'select', operation: 'clear', range: 'all' });
}

/**
 * Displays the default mode of the right panel, showing selection details or a prompt if nothing is selected.
 */
function DefaultDisplay({ state, dispatch }: StateDispatchProps) {
    // Memoize dispatch to prevent unnecessary re-renders
    const freeSelectionDispatch = useCallback(
        (action: FreeSelectionAction) => {
            dispatch({ type: 'select', ...action });
        },
        [ dispatch ],
    );

    const { hasSelection } = useSelection(state);

    return (<>
        {hasSelection ? (
            <SelectionCard selection={state.selection} graph={state.graph} dispatch={freeSelectionDispatch} />
        ) : (
            <div className='p-4 text-default-500 text-center'>
                <h3 className='text-lg font-semibold'>No Selection</h3>
                <p>Select an object or morphism to edit.</p>
            </div>
        )}
    </>);
}

type EditorFormProps = {
    /** Additional form fields or content. */
    children: React.ReactNode;
    /** Function to call when submitting the form. */
    onSubmit: () => void;
    /** Function to call when canceling the form. */
    onCancel: () => void;
    /** Whether the submit button should be disabled. */
    isSubmitDisabled: boolean;
};

/**
 * Reusable form component for editing objexes and morphisms with consistent button behavior.
 */
function EditorForm({ children, onSubmit, onCancel, isSubmitDisabled }: EditorFormProps) {
    return (<>
        {children}
        <div className='grid grid-cols-2 gap-2'>
            <Button onClick={onCancel}>Cancel</Button>
            <Button color='primary' onClick={onSubmit} isDisabled={isSubmitDisabled}>
                    Apply
            </Button>
        </div>
    </>);
}

/**
 * Renders a form to update a selected objex's label and position.
 */
function UpdateObjexDisplay({ state, dispatch }: StateDispatchProps) {
    const { selectedNode } = useSelection(state);
    const [ label, setLabel ] = useState(selectedNode?.metadata.label ?? '');
    const [ position, setPosition ] = useState<FormPosition>(
        selectedNode ? { x: selectedNode.x, y: selectedNode.y } : { x: 0, y: 0 },
    );

    // Check if any changes were made
    const hasChanges = selectedNode && (
        label !== selectedNode.metadata.label ||
        position.x !== selectedNode.x ||
        position.y !== selectedNode.y
    );

    // Sync form state when selection or node data changes
    useEffect(() => {
        if (selectedNode) {
            setLabel(selectedNode.metadata.label);
            setPosition({ x: selectedNode.x, y: selectedNode.y });
        }
    }, [ selectedNode ]);

    /**
     * Updates the objex with new label and position, then refreshes the graph.
     */
    function handleApply() {
        if (!selectedNode || !label || !hasChanges)
            return;
        state.evocat.updateObjex(selectedNode.schema.key, {
            label,
            position: toPosition(position),
        });
        const graph = categoryToGraph(state.evocat.category);
        dispatch({ type: 'rightPanelMode', mode: RightPanelMode.updateObjex, graph });
    }

    function handleKeyDown(e: React.KeyboardEvent) {
        if (e.key === 'Enter' && hasChanges) {
            e.preventDefault();
            handleApply();
        }
    }

    if (!selectedNode)
        return <div className='p-4 text-danger-500'>Error: No object selected</div>;

    return (
        <div className='p-3 flex flex-col gap-3'>
            <h3 className='text-lg font-semibold'>Update Object</h3>

            <p>
                <strong>Key:</strong> {selectedNode.schema.key.toString()}
            </p>

            <EditorForm
                onSubmit={handleApply}
                onCancel={() => resetToDefaultMode(dispatch)}
                isSubmitDisabled={!hasChanges || !label}
            >
                <Input
                    label='Label'
                    value={label}
                    onChange={e => setLabel(e.target.value)}
                    onKeyDown={handleKeyDown}
                    placeholder='Enter object label'
                />
                <div className='grid grid-cols-2 gap-2'>
                    <Input
                        label='Position x'
                        type='number'
                        value={toNumber(position.x).toFixed(0)}
                        onChange={e => setPosition({ ...position, x: toFormNumber(e.target.value) })}
                        onKeyDown={handleKeyDown}
                    />
                    <Input
                        label='Position y'
                        type='number'
                        value={toNumber(position.y).toFixed(0)}
                        onChange={e => setPosition({ ...position, y: toFormNumber(e.target.value) })}
                        onKeyDown={handleKeyDown}
                    />
                </div>
            </EditorForm>
        </div>
    );
}

/**
 * Renders a form to update a selected morphism's cardinality.
 */
export function UpdateMorphismDisplay({ state, dispatch }: StateDispatchProps) {
    const { selectedMorphism } = useSelection(state);
    const [ minCardinality, setMinCardinality ] = useState<Min>(selectedMorphism?.schema.min ?? Cardinality.Zero);
    const [ label, setLabel ] = useState(selectedMorphism?.metadata.label ?? '');

    // Check if any changes were made
    const hasChanges = selectedMorphism && (
        minCardinality !== selectedMorphism.schema.min ||
        label !== selectedMorphism.metadata.label
    );

    // Sync state when morphism changes
    useEffect(() => {
        if (selectedMorphism) {
            setMinCardinality(selectedMorphism.schema.min);
            setLabel(selectedMorphism.metadata.label);
        }
    }, [ selectedMorphism ]);

    /**
     * Updates the morphism and refreshes the graph.
     */
    function handleApply() {
        if (!selectedMorphism || !hasChanges)
            return;

        state.evocat.updateMorphism(selectedMorphism.schema, {
            min: minCardinality,
            label: label !== selectedMorphism.metadata.label ? label : undefined,
        });

        const graph = categoryToGraph(state.evocat.category);
        dispatch({
            type: 'rightPanelMode',
            mode: RightPanelMode.updateMorphism,
            graph,
        });
    }

    function handleCancel() {
        resetToDefaultMode(dispatch);
    }

    function handleKeyDown(e: React.KeyboardEvent) {
        if (e.key === 'Enter' && hasChanges) {
            e.preventDefault();
            handleApply();
        }
    }

    if (!selectedMorphism)
        return <div className='p-4 text-danger-500'>Error: No morphism selected</div>;

    return (
        <div className='p-3 flex flex-col gap-3'>
            <h3 className='text-lg font-semibold'>Update Morphism</h3>

            <Input
                label='Label'
                value={label}
                onValueChange={setLabel}
                onKeyDown={handleKeyDown}
                placeholder='No morphism label'
                className='max-w-xs'
            />

            <p className='text-default-600'>
                <strong>Signature:</strong> {selectedMorphism.schema.signature.toString()}
            </p>

            <EditorForm
                onSubmit={handleApply}
                onCancel={handleCancel}
                isSubmitDisabled={!hasChanges}
            >
                <div className='space-y-2'>
                    <p className='text-sm'>Minimum Cardinality:</p>
                    <RadioGroup
                        value={minCardinality}
                        onChange={e => setMinCardinality(e.target.value as Min)}
                        orientation='horizontal'
                    >
                        <Radio value={Cardinality.Zero}>0</Radio>
                        <Radio value={Cardinality.One}>1</Radio>
                    </RadioGroup>
                </div>
            </EditorForm>
        </div>
    );
}
