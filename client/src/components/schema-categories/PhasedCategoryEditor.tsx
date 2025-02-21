import { useState } from 'react';
import { Button, Input } from '@nextui-org/react';
import { EditorPhase, type EditCategoryDispatch, type EditCategoryState } from './editCategoryReducer';
import { cn } from '../utils';
import { type FormPosition, toFormNumber, toPosition } from '@/types/utils/common';
import { type CategoryNode, categoryToGraph } from './categoryGraph';
import { Cardinality } from '@/types/schema/Morphism';
import { Key } from '@/types/identifiers/Key';

type StateDispatchProps = Readonly<{
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

type PhasedEditorProps = StateDispatchProps & Readonly<{
    className?: string;
}>;

export function PhasedEditor({ state, dispatch, className }: PhasedEditorProps) {
    return (
        <div className={cn('border p-3 flex flex-col gap-3 bg-background', className)}>
            {components[state.phase]({ state, dispatch })}
        </div>
    );
}

const components: Record<EditorPhase, (props: StateDispatchProps) => JSX.Element> = {
    [EditorPhase.default]: DefaultDisplay,
    [EditorPhase.createObjex]: CreateObjexDisplay,
    [EditorPhase.createMorphism]: CreateMorphismDisplay,
};

function DefaultDisplay({ state, dispatch }: StateDispatchProps) {
    const singleSelectedNode = (state.selection.nodeIds.size === 1 && state.selection.edgeIds.size === 0)
        ? state.graph.nodes.find(node => state.selection.nodeIds.has(node.id))
        : undefined;

    const twoSelectedNodes = (state.selection.nodeIds.size === 2 && state.selection.edgeIds.size === 0)
        ? state.graph.nodes.find(node => state.selection.nodeIds.has(node.id))
        : undefined;

    function deleteObjex(node: CategoryNode) {
        state.evocat.deleteObjex(node.schema.key);
        const graph = categoryToGraph(state.evocat.category);

        dispatch({ type: 'phase', phase: EditorPhase.default, graph });
    }

    return (<>
        <h3>Default</h3>

        <Button onClick={() => dispatch({ type: 'phase', phase: EditorPhase.createObjex })}>Create object</Button>

        {twoSelectedNodes && (
            <Button onClick={() => dispatch({ type: 'phase', phase: EditorPhase.createMorphism })}
            >
                Create Morphism
            </Button>
        )}

        {singleSelectedNode && (<>
            <div>
                Selected: <span className='font-semibold'>{singleSelectedNode.metadata.label}</span>
            </div>

            <Button color='danger' onClick={() => deleteObjex(singleSelectedNode)}>Delete</Button>
        </>)}
    </>);
}

function CreateObjexDisplay({ state, dispatch }: StateDispatchProps) {
    const [ label, setLabel ] = useState('');
    const [ position, setPosition ] = useState<FormPosition>({ x: 0, y: 0 });

    function createObjex() {
        state.evocat.createObjex({ label, position: toPosition(position) });
        const graph = categoryToGraph(state.evocat.category);

        dispatch({ type: 'phase', phase: EditorPhase.default, graph });
    }

    return (<>
        <h3>Create object</h3>

        <Input
            label='Label'
            value={label}
            onChange={e => setLabel(e.target.value)}
        />

        <div className='grid grid-cols-2 gap-2'>
            <Input
                label='Position x'
                type='number'
                value={'' + position.x}
                onChange={e => setPosition({ ...position, x: toFormNumber(e.target.value) })}
            />

            <Input
                label='Position y'
                type='number'
                value={'' + position.y}
                onChange={e => setPosition({ ...position, y: toFormNumber(e.target.value) })}
            />
        </div>

        <div className='grid grid-cols-2 gap-2'>
            <Button onClick={() => dispatch({ type: 'phase', phase: EditorPhase.default })}>
                Cancel
            </Button>

            <Button color='primary' onClick={createObjex} isDisabled={label === ''}>
                Finish
            </Button>
        </div>
    </>);
}

export function CreateMorphismDisplay({ state, dispatch }: StateDispatchProps) {
    const [ label, setLabel ] = useState('');

    // Extract selected nodes (should be exactly 2)
    const selectedNodes = Array.from(state.selection.nodeIds);
    const isValidSelection = selectedNodes.length === 2;  // only two nodes can be selected for morphism creation

    function createMorphism() {
        if (!isValidSelection) 
            return;

        const domKey = Key.createNew(Number(selectedNodes[0]));
        const codKey = Key.createNew(Number(selectedNodes[1]));
    
        state.evocat.createMorphism({
            domKey, // Source object
            codKey, // Target object
            min: Cardinality.One, // TODO: add button for selecting cardinality
            label,
        });
    
        const graph = categoryToGraph(state.evocat.category);
    
        dispatch({ type: 'phase', phase: EditorPhase.default, graph });
    }

    return (
        <>
            <h3>Create Morphism</h3>

            <div>
                <p>Selected Nodes:</p>
                <ul>
                    {selectedNodes.map(nodeId => (
                        <li key={nodeId}>Node ID: {nodeId}</li>
                    ))}
                </ul>
            </div>

            <Input
                label='Label'
                value={label}
                onChange={e => setLabel(e.target.value)}
            />

            <div className='grid grid-cols-2 gap-2'>
                <Button onClick={() => dispatch({ type: 'phase', phase: EditorPhase.default })}>
                    Cancel
                </Button>

                <Button color='primary' onClick={createMorphism} isDisabled={!isValidSelection || label === ''}>
                    Finish
                </Button>
            </div>
        </>
    );
}
