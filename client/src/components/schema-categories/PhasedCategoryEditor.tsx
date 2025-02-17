import { useState } from 'react';
import { Button, Input } from '@nextui-org/react';
import { EditorPhase, type EditCategoryDispatch, type EditCategoryState } from './editCategoryReducer';
import { cn } from '../utils';
import { type FormPosition, toFormNumber, toPosition } from '@/types/utils/common';
import { type CategoryNode, categoryToGraph } from './categoryGraph';
import { type Evocat } from '@/types/evocat/Evocat';

type StateDispatchProps = Readonly<{
    evocat: Evocat;
    state: EditCategoryState;
    dispatch: EditCategoryDispatch;
}>;

type PhasedEditorProps = StateDispatchProps & Readonly<{
    className?: string;
}>;

export function PhasedEditor({ evocat, state, dispatch, className }: PhasedEditorProps) {
    const phase = state.editor.phase;

    return (
        <div className={cn('border p-3 flex flex-col gap-3 bg-background', className)}>
            {components[phase]({ evocat, state, dispatch })}
        </div>
    );
}

const components: Record<EditorPhase, (props: StateDispatchProps) => JSX.Element> = {
    default: Default,
    createObject: CreateObject,
};

function Default({ evocat, state, dispatch }: StateDispatchProps) {
    const singleSelectedNode = (state.selectedNodeIds.size === 1 && state.selectedEdgeIds.size === 0)
        ? state.graph.nodes.find(node => state.selectedNodeIds.has(node.id))
        : undefined;

    function deleteNode(node: CategoryNode) {
        evocat.deleteObjex(node.schema.key);
        const graph = categoryToGraph(evocat.category);

        dispatch({ type: 'phase', phase: EditorPhase.default, graph });
    }

    return (<>
        <h3>Default</h3>
        <Button onClick={() => dispatch({ type: 'phase', phase: EditorPhase.createObject })}>Create object</Button>
        {singleSelectedNode && (<>
            <div>
                Selected: <span className='font-semibold'>{singleSelectedNode.metadata.label}</span>
            </div>
            <Button color='danger' onClick={() => deleteNode(singleSelectedNode)}>Delete</Button>
        </>)}
    </>);
}

function CreateObject({ evocat, dispatch }: StateDispatchProps) {
    const [ label, setLabel ] = useState('');
    const [ position, setPosition ] = useState<FormPosition>({ x: 0, y: 0 });

    function finish() {
        evocat.createObjex({ label, position: toPosition(position) });
        const graph = categoryToGraph(evocat.category);

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

            <Button color='primary' onClick={finish} disabled={label === ''}>
                Finish
            </Button>
        </div>
    </>);
}
