import { useCallback, useEffect, useState } from 'react';
import { Button, Input, Radio, RadioGroup } from '@nextui-org/react';
import { RightPanelMode, type EditCategoryDispatch, type EditCategoryState } from './editCategoryReducer';
import { cn } from '../utils';
import { type FormPosition, toFormNumber, toNumber, toPosition } from '@/types/utils/common';
import { categoryToGraph } from './categoryGraph';
import { type FreeSelectionAction } from '../graph/FreeSelection';
import { SelectionCard } from './SelectionCard';
import { Cardinality, type Min } from '@/types/schema';

/**
 * Props for components that receive editor state and dispatch.
 *
 * @interface StateDispatchProps
 * @property state - The current state of the category editor.
 * @property dispatch - The dispatch function for updating the editor state.
 */
type StateDispatchProps = Readonly<{
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

type RightPanelEditorProps = StateDispatchProps & Readonly<{
    className?: string;
}>;

/**
 * Renders the right panel for editing a category, dynamically displaying content based on the current selection and mode.
 *
 * @param props - The component props.
 * @returns A React component rendering the right panel.
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
 * Mapping of right panel modes to their respective display components.
 */
const components: Record<RightPanelMode, (props: StateDispatchProps) => JSX.Element> = {
    [RightPanelMode.default]: DefaultDisplay,
    [RightPanelMode.updateObjex]: UpdateObjexDisplay,
    [RightPanelMode.updateMorphism]: UpdateMorphismDisplay,
};

/**
 * Determines the appropriate display component based on the current selection.
 *
 * @param state - The current editor state.
 * @returns The component to render for the right panel.
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
 *
 * @param props - The component props with state and dispatch.
 * @returns A React component for the default panel mode.
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

    return (
        <>
            {hasSelection ? (
                <SelectionCard selection={state.selection} graph={state.graph} dispatch={freeSelectionDispatch} />
            ) : (
                <div className='p-4 text-default-500 text-center'>
                    <h3 className='text-lg font-semibold'>No Selection</h3>
                    <p>Select an object or morphism to edit.</p>
                </div>
            )}
        </>
    );
}

/**
 * Props for the reusable editor form component.
 *
 * @interface EditorFormProps
 * @property children - Additional form fields or content.
 * @property onSubmit - Function to call when submitting the form.
 * @property onCancel - Function to call when canceling the form.
 * @property isSubmitDisabled - Whether the submit button should be disabled.
 */
type EditorFormProps = {
    children: React.ReactNode;
    onSubmit: () => void;
    onCancel: () => void;
    isSubmitDisabled: boolean;
};

/**
 * Reusable form component for editing objects and morphisms with consistent button behavior.
 *
 * @param props - The form component props.
 * @returns A React component rendering the form buttons.
 */
function EditorForm({ children, onSubmit, onCancel, isSubmitDisabled }: EditorFormProps) {
    return (
        <>
            {children}
            <div className='grid grid-cols-2 gap-2'>
                <Button onClick={onCancel}>Cancel</Button>
                <Button color='primary' onClick={onSubmit} isDisabled={isSubmitDisabled}>
                    Apply
                </Button>
            </div>
        </>
    );
}

/**
 * Renders a form to update a selected schema object’s label and position.
 *
 * @param props - The component props with state and dispatch.
 * @returns A React component for updating a schema object.
 */
function UpdateObjexDisplay({ state, dispatch }: StateDispatchProps) {
    const { selectedNode } = useSelection(state);
    const [ label, setLabel ] = useState(selectedNode?.metadata.label ?? '');
    const [ position, setPosition ] = useState<FormPosition>(
        selectedNode ? { x: selectedNode.x, y: selectedNode.y } : { x: 0, y: 0 },
    );

    // Sync form state when selection or node data changes
    useEffect(() => {
        if (selectedNode) {
            setLabel(selectedNode.metadata.label);
            setPosition({ x: selectedNode.x, y: selectedNode.y });
        }
    }, [ selectedNode ]);

    /**
     * Updates the schema object with new label and position, then refreshes the graph.
     */
    function handleApply() {
        if (!selectedNode || !label) 
            return; // Guard against invalid state
        state.evocat.updateObjex(selectedNode.schema.key, {
            label,
            position: toPosition(position),
        });
        const graph = categoryToGraph(state.evocat.category);
        dispatch({ type: 'rightPanelMode', mode: RightPanelMode.updateObjex, graph });
    }

    // Guard against rendering if no node is selected
    if (!selectedNode) 
        return <div className='p-4 text-danger-500'>Error: No object selected</div>;
    

    return (
        <div className='p-3 flex flex-col gap-3'>
            <h3 className='text-lg font-semibold'>Update Object</h3>

            <p>
                <strong>Key:</strong> {selectedNode.schema.key.toString()}
            </p>

            <EditorForm onSubmit={handleApply} onCancel={() => resetToDefaultMode(dispatch)} isSubmitDisabled={!label}>
                <Input
                    label='Label'
                    value={label}
                    onChange={e => setLabel(e.target.value)}
                    placeholder='Enter object label'
                />
                <div className='grid grid-cols-2 gap-2'>
                    <Input
                        label='Position x'
                        type='number'
                        value={toNumber(position.x).toFixed(0)}
                        onChange={e => setPosition({ ...position, x: toFormNumber(e.target.value) })}
                    />
                    <Input
                        label='Position y'
                        type='number'
                        value={toNumber(position.y).toFixed(0)}
                        onChange={e => setPosition({ ...position, y: toFormNumber(e.target.value) })}
                    />
                </div>
            </EditorForm>
        </div>
    );
}

/**
 * Renders a form to update a selected morphism’s cardinality.
 *
 * @param props - The component props with state and dispatch.
 * @returns A React component for updating a morphism.
 */
export function UpdateMorphismDisplay({ state, dispatch }: StateDispatchProps) {
    const { selectedMorphism } = useSelection(state);
    const [ minCardinality, setMinCardinality ] = useState<Min>(selectedMorphism?.schema.min ?? Cardinality.Zero);

    // Sync cardinality when morphism changes
    useEffect(() => {
        if (selectedMorphism) 
            setMinCardinality(selectedMorphism.schema.min);
        
    }, [ selectedMorphism ]);

    /**
     * Updates the morphism’s cardinality and refreshes the graph.
     */
    function handleApply() {
        if (!selectedMorphism) 
            return; // Guard against invalid state
        state.evocat.updateMorphism(selectedMorphism.schema, { min: minCardinality });
        const graph = categoryToGraph(state.evocat.category);
        dispatch({ type: 'rightPanelMode', mode: RightPanelMode.updateMorphism, graph });
    }

    /**
     * Handles changes to the cardinality radio group.
     */
    function handleCardinalityChange(event: React.ChangeEvent<HTMLInputElement>) {
        setMinCardinality(event.target.value as Min);
    }

    // Guard against rendering if no morphism is selected
    if (!selectedMorphism) 
        return <div className='p-4 text-danger-500'>Error: No morphism selected</div>;

    return (
        <div className='p-3 flex flex-col gap-3'>
            <h3 className='text-lg font-semibold'>Update Morphism</h3>
            <p>
                <strong>Signature:</strong> {selectedMorphism.schema.signature.toString()}
            </p>
            <EditorForm
                onSubmit={handleApply}
                onCancel={() => resetToDefaultMode(dispatch)}
                isSubmitDisabled={false}
            >
                <div>
                    <p>Minimum Cardinality:</p>
                    <RadioGroup
                        value={minCardinality}
                        onChange={handleCardinalityChange}
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