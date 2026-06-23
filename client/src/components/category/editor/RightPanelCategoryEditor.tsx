import { type CategoryEditorDispatch, type CategoryEditorState } from './useCategoryEditor';
import { SelectionDisplay } from './SelectionDisplay';
import { cn } from '@/components/common/utils';
import { ObjexEditor } from './ObjexEditor';
import { MorphismEditor } from './MorphismEditor';
import { Button, Switch } from '@heroui/react';
import { layoutGraph, LayoutType } from '@/components/graph/graphLayout';
import { categoryToGraph, getNodeKey } from '../graph/categoryGraph';

type StateDispatchProps = {
    state: CategoryEditorState;
    dispatch: CategoryEditorDispatch;
};

type RightPanelEditorProps = StateDispatchProps & {
    className?: string;
};

export function RightPanelCategoryEditor({ state, dispatch, className }: RightPanelEditorProps) {
    const Component = getRightPanelComponent(state);

    function layout() {
        const graph = categoryToGraph(state.evocat.category);

        layoutGraph(graph, LayoutType.force);

        for (const node of graph.nodes.values()) {
            const key = getNodeKey(node.id);
            state.evocat.updateObjex(key, { position: { x: node.x, y: node.y } });
        }

        const nodes = graph.nodes.values().toArray();

        dispatch({ type: 'layout', nodes });
    }

    return (
        <div className={cn('h-full px-3 py-2 flex flex-col gap-2', className)}>
            <Component state={state} dispatch={dispatch} />

            <div className='grow' />

            <div className='py-1'>
                <Switch
                    isSelected={state.options.showEdgeLabels}
                    onValueChange={value => dispatch({ type: 'options', options: { ...state.options, showEdgeLabels: value } })}
                    size='sm'
                >
                    Edge labels
                </Switch>
            </div>

            <Button
                title='Layout graph'
                onPress={() => layout()}
                color='default'
                className='w-full'
            >
                Layout graph
            </Button>
        </div>
    );
}

function getRightPanelComponent(state: CategoryEditorState) {
    if (state.form?.type === 'objex')
        return ObjexEditor;

    if (state.form?.type === 'morphism')
        return MorphismEditor;

    return SelectionDisplay;
}
