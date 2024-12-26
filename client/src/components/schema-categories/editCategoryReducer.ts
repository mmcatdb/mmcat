import { type Dispatch } from 'react';
import { type GraphAction } from '../graph/graphEngine';
import { type CategoryGraph, categoryToGraph } from './categoryGraph';
import { type Evocat } from '@/types/evocat/Evocat';

export type EditCategoryState = {
    graph: CategoryGraph;
    selectedNodeIds: Set<string>;
    editor: PhasedEditorState;
};

export function createInitialState(evocat: Evocat): EditCategoryState {
    const graph = categoryToGraph(evocat.category);

    return {
        graph,
        selectedNodeIds: new Set(),
        editor: { phase: EditorPhase.default },
    };
}

export type EditCategoryDispatch = Dispatch<EditCategoryAction>;

type EditCategoryAction = GraphAction | SelectNodeAction | PhaseAction;

export function editCategoryReducer(state: EditCategoryState, action: EditCategoryAction): EditCategoryState {
    console.log('REDUCE', state.editor.phase, action, state);
    switch (action.type) {
    case 'graph': return graph(state, action);
    case 'selectNode': return selectNode(state, action);
    case 'phase': return phase(state, action);
    }
}

// Low-level graph library events

function graph(state: EditCategoryState, action: GraphAction): EditCategoryState {
    switch (action.operation) {
    case 'move': {
        // TODO Change this to some 'move' operation on Evocat (done in the graph display component) and then propagate the change to the state.
        // Use useCallback to create dispatch with access to evocat for the graph library.
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

// Selection

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

// Editor phases

export enum EditorPhase {
    default = 'default',
    createObject = 'createObject',
}

type PhasedEditorState = {
    phase: EditorPhase;
};

export type PhaseAction = {
    type: 'phase';
    /** The phase we want to switch to. */
    phase: EditorPhase;
    /** If defined, the graph state should be updated by this value. */
    graph?: CategoryGraph;
}

function phase(state: EditCategoryState, action: PhaseAction): EditCategoryState {
    return { ...state, editor: { ...state.editor, phase: action.phase }, graph: action.graph ?? state.graph };
}
