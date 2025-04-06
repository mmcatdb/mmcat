import { useCallback, useEffect, useState } from 'react';
import { Button, Input, Radio, RadioGroup } from '@nextui-org/react';
import { RightPanelMode, type EditCategoryDispatch, type EditCategoryState } from './editCategoryReducer';
import { cn } from '../utils';
import { type FormPosition, toFormNumber, toNumber, toPosition } from '@/types/utils/common';
import { categoryToGraph } from './categoryGraph';
import { type FreeSelectionAction } from '../graph/FreeSelection';
import { SelectionCard } from './SelectionCard';
import { Cardinality, type Min } from '@/types/schema';

type StateDispatchProps = Readonly<{
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

type RightPanelEditorProps = StateDispatchProps & Readonly<{
    className?: string;
}>;

export function RightPanelCategoryEditor({ state, dispatch, className }: RightPanelEditorProps) {
    const Component = getRightPanelComponent(state);
    // const Component = components[state.rightPanelMode];

    return (
        <div className={cn('p-2 flex flex-col gap-3', className)}>
            <Component state={state} dispatch={dispatch} />
        </div>
    );
}

const components: Record<RightPanelMode, (props: StateDispatchProps) => JSX.Element> = {
    [RightPanelMode.default]: DefaultDisplay,
    [RightPanelMode.updateObjex]: UpdateObjexDisplay,
    [RightPanelMode.updateMorphism]: UpdateMorphismDisplay,
};

// Dynamic selection of display component
function getRightPanelComponent(state: EditCategoryState) {
    if (state.selection.nodeIds.size === 1 && state.selection.edgeIds.size === 0) 
        return UpdateObjexDisplay;
    
    if (state.selection.nodeIds.size === 0 && state.selection.edgeIds.size === 1) 
        return UpdateMorphismDisplay;
    
    return DefaultDisplay;
}

// Default selection screen
function DefaultDisplay({ state, dispatch }: StateDispatchProps) {
    const freeSelectionDispatch = useCallback((action: FreeSelectionAction) => {
        dispatch({ type: 'select', ...action });
    }, [ dispatch ]);

    const hasSelection = state.selection.nodeIds.size > 0 || state.selection.edgeIds.size > 0;

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

// Schema Object Edit Screen
function UpdateObjexDisplay({ state, dispatch }: StateDispatchProps) {
    const selectedNodeId = Array.from(state.selection.nodeIds)[0];
    const selectedNode = state.graph.nodes.get(selectedNodeId);

    const [ label, setLabel ] = useState(selectedNode!.metadata.label);
    const [ position, setPosition ] = useState<FormPosition>({ x: 0, y: 0 });
    
    function handleApply() {
        state.evocat.updateObjex(selectedNode!.schema.key, {
            label,
            position: toPosition(position),
        });

        const graph = categoryToGraph(state.evocat.category);
        dispatch({ type: 'rightPanelMode', mode: RightPanelMode.updateObjex, graph });
    }

    useEffect(() => {
        setLabel(selectedNode.metadata.label);
        setPosition({ x: selectedNode.x, y: selectedNode.y });
    }, [  selectedNodeId, state.graph.nodes ]); // Re-run effect when different object selected, or position changes

    return (
        <div className='p-3 flex flex-col gap-3'>
            <h3 className='text-lg font-semibold'>Update Object</h3>

            <p>
                <strong>Key:</strong> {selectedNode.schema.key.toString()}
            </p>

            <Input
                label='Label'
                value={label}
                onChange={e => setLabel(e.target.value)}
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

            <div className='grid grid-cols-2 gap-2'>
                <Button onClick={() => dispatch({ type: 'select', operation: 'clear', range: 'all' })}>
                    Cancel
                </Button>

                <Button color='primary' onClick={handleApply} isDisabled={!label}>
                    Apply
                </Button>
            </div>
        </div>
    );
}

// Morphism Edit Screen
export function UpdateMorphismDisplay({ state, dispatch }: StateDispatchProps) {
    const selectedEdgeId = Array.from(state.selection.edgeIds)[0];
    const selectedMorphism = state.graph.edges.get(selectedEdgeId);
    
    // set initial state for cardinality based on the selected morphism
    const [ minCardinality, setMinCardinality ] = useState<Min>(selectedMorphism?.schema.min ?? Cardinality.Zero);

    // const [ domUnlocked, setDomUnlocked ] = useState(false);
    // const [ codUnlocked, setCodUnlocked ] = useState(false);

    useEffect(() => {
        if (selectedMorphism) 
            setMinCardinality(selectedMorphism.schema.min);
        
    }, [ selectedMorphism ]);

    function handleCardinalityChange(event: React.ChangeEvent<HTMLInputElement>) {
        setMinCardinality(event.target.value as Min);
    }

    function handleApply() {
        state.evocat.updateMorphism(selectedMorphism!.schema, { min: minCardinality });
        const graph = categoryToGraph(state.evocat.category);
        dispatch({ type: 'rightPanelMode', mode: RightPanelMode.updateMorphism, graph });
    }

    return (
        <div className='p-3 flex flex-col gap-3'>
            <h3 className='text-lg font-semibold'>Update Morphism</h3>
            <p>
                <strong>Signature:</strong> {selectedMorphism!.schema.signature.toString()}
            </p>
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
            <div className='grid grid-cols-2 gap-2'>
                <Button
                    onClick={() => dispatch({ type: 'select', operation: 'clear', range: 'all' })}
                >
                    Cancel
                </Button>
                <Button onClick={handleApply} color='primary'>
                    Apply
                </Button>
            </div>
        </div>
    );
}
