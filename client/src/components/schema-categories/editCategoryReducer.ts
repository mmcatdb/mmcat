import { type Dispatch } from 'react';
import { type GraphAction } from '../graph/graphEngine';
import { type CategoryGraph, categoryToGraph } from './categoryGraph';
import { type Evocat } from '@/types/evocat/Evocat';
import { createInitialEditorState, type PhasedEditorAction, phasedEditorReducer, type PhasedEditorState } from './phasedEditorReducer';

export type EditCategoryState = {
    graph: CategoryGraph;
    selectedNodeIds: Set<string>;
    evocat: Evocat;
    editor: PhasedEditorState;
};

export function createInitialState(evocat: Evocat): EditCategoryState {
    const graph = categoryToGraph(evocat.current);

    return {
        graph,
        selectedNodeIds: new Set(),
        evocat,
        editor: createInitialEditorState(),
    };
}

export type EditCategoryDispatch = Dispatch<EditCategoryAction>;

type EditCategoryAction = GraphAction | SelectNodeAction | PhasedEditorAction;

export function editCategoryReducer(state: EditCategoryState, action: EditCategoryAction): EditCategoryState {
    console.log('REDUCE', state.editor.phase, action, state);
    switch (action.type) {
    case 'graph': return graph(state, action);
    case 'selectNode': return selectNode(state, action);
    default: return phasedEditorReducer(state, action);
    }
}

function graph(state: EditCategoryState, action: GraphAction): EditCategoryState {
    switch (action.operation) {
    case 'move': {
        const nodes = state.graph.nodes.map(node => node.id === action.nodeId ? { ...node, position: action.position } : node);
        return { ...state, graph: { ...state.graph, nodes } };
    }
    case 'select': {
        if (!action.isSpecialKey) {
            // The default case - we select exactly the given nodes.
            return { ...state, selectedNodeIds: new Set(action.nodeIds) };
        }

        // The special key is pressed - we toggle the given nodes.
        const selectedNodeIds = new Set(state.selectedNodeIds);
        action.nodeIds.forEach(nodeId =>
            selectedNodeIds.has(nodeId) ? selectedNodeIds.delete(nodeId) : selectedNodeIds.add(nodeId),
        );

        return { ...state, selectedNodeIds };
    }
    }
}

type SelectNodeAction = {
    type: 'selectNode';
} & ({
    nodeId: string;
    operation: 'set' | 'add' | 'remove' | 'toggle';
} | {
    operation: 'clear';
});

function selectNode(state: EditCategoryState, action: SelectNodeAction): EditCategoryState {
    if (action.operation === 'clear')
        return { ...state, selectedNodeIds: new Set() };

    const { nodeId, operation } = action;
    if (operation === 'set')
        return { ...state, selectedNodeIds: new Set([ nodeId ]) };

    const selectedNodeIds = new Set(state.selectedNodeIds);
    if (operation === 'add')
        selectedNodeIds.add(nodeId);
    else if (operation === 'remove')
        selectedNodeIds.delete(nodeId);
    else if (operation === 'toggle')
        selectedNodeIds.has(nodeId) ? selectedNodeIds.delete(nodeId) : selectedNodeIds.add(nodeId);

    return { ...state, selectedNodeIds };
}
