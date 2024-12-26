import { type Dispatch } from 'react';
import { type GraphAction } from '../graph/graphEngine';
import { type CategoryGraph, categoryToGraph } from './categoryGraph';
import { type Evocat } from '@/types/evocat/Evocat';

export type EditCategoryState = {
    graph: CategoryGraph;
    selectedNodeIds: Set<string>;
    selectedEdgeIds: Set<string>;
    editor: PhasedEditorState;
};

export function createInitialState(evocat: Evocat): EditCategoryState {
    const graph = categoryToGraph(evocat.category);

    return {
        graph,
        selectedNodeIds: new Set(),
        selectedEdgeIds: new Set(),
        editor: { phase: EditorPhase.default },
    };
}

/** Utility function to make sure we correctly propagate all evocat changes to the reactive state. */
function propagateEvocat(graph: CategoryGraph | undefined, state: EditCategoryState): EditCategoryState {
    if (!graph)
        return state;

    let selectedNodeIds = new Set<string>();
    // Just few optimizations (no need to search for the nodes if there are none selected, and keep the old state if it didn't change).
    if (state.selectedNodeIds.size > 0) {
        graph.nodes
            .filter(node => state.selectedNodeIds.has(node.id))
            .forEach(node => selectedNodeIds.add(node.id));
    }
    if (state.selectedNodeIds.size === selectedNodeIds.size)
        selectedNodeIds = state.selectedNodeIds;

    let selectedEdgeIds = new Set<string>();
    // The same optimizations as above.
    if (state.selectedEdgeIds.size > 0) {
        graph.edges
            .filter(edge => state.selectedEdgeIds.has(edge.id))
            .forEach(edge => selectedEdgeIds.add(edge.id));
    }
    if (state.selectedEdgeIds.size === selectedEdgeIds.size)
        selectedEdgeIds = state.selectedEdgeIds;

    return { ...state, graph, selectedNodeIds, selectedEdgeIds };
}

export type EditCategoryDispatch = Dispatch<EditCategoryAction>;

type EditCategoryAction = GraphAction | SelectAction | PhaseAction;

export function editCategoryReducer(state: EditCategoryState, action: EditCategoryAction): EditCategoryState {
    console.log('REDUCE', state.editor.phase, action, state);
    switch (action.type) {
    case 'graph': return graph(state, action);
    case 'select': return select(state, action);
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
            return { ...state, selectedNodeIds: new Set(action.nodeIds), selectedEdgeIds: new Set(action.edgeIds) };
        }

        // A special key is pressed - we toggle the given nodes and edges.
        const selectedNodeIds = new Set(state.selectedNodeIds);
        action.nodeIds.forEach(nodeId =>
            selectedNodeIds.has(nodeId) ? selectedNodeIds.delete(nodeId) : selectedNodeIds.add(nodeId),
        );

        const selectedEdgeIds = new Set(state.selectedEdgeIds);
        action.edgeIds.forEach(edgeId =>
            selectedEdgeIds.has(edgeId) ? selectedEdgeIds.delete(edgeId) : selectedEdgeIds.add(edgeId),
        );

        return { ...state, selectedNodeIds, selectedEdgeIds };
    }
    }
}

// Selection

type SelectAction = {
    type: 'select';
} & ({
    operation: 'set' | 'add' | 'remove' | 'toggle';
    nodeId: string;
} | {
    operation: 'set' | 'add' | 'remove' | 'toggle';
    edgeId: string;
} | {
    operation: 'clear';
    range: 'nodes' | 'edges' | 'all';
});

function select(state: EditCategoryState, action: SelectAction): EditCategoryState {
    const operation = action.operation;
    if (operation === 'clear') {
        const selectedNodeIds = (action.range === 'nodes' || action.range === 'all') ? new Set<string>() : state.selectedNodeIds;
        const selectedEdgeIds = (action.range === 'edges' || action.range === 'all') ? new Set<string>() : state.selectedEdgeIds;
        return { ...state, selectedNodeIds, selectedEdgeIds };
    }

    if (operation === 'set') {
        return 'nodeId' in action
            ? { ...state, selectedNodeIds: new Set([ action.nodeId ]), selectedEdgeIds: new Set() }
            : { ...state, selectedEdgeIds: new Set([ action.edgeId ]), selectedNodeIds: new Set() };
    }

    if ('nodeId' in action) {
        const nodeId = action.nodeId;
        const selectedNodeIds = new Set(state.selectedNodeIds);

        if (operation === 'add')
            selectedNodeIds.add(nodeId);
        else if (operation === 'remove')
            selectedNodeIds.delete(nodeId);
        else if (operation === 'toggle')
            selectedNodeIds.has(nodeId) ? selectedNodeIds.delete(nodeId) : selectedNodeIds.add(nodeId);

        return { ...state, selectedNodeIds };
    }
    else {
        const edgeId = action.edgeId;
        const selectedEdgeIds = new Set(state.selectedEdgeIds);

        if (operation === 'add')
            selectedEdgeIds.add(edgeId);
        else if (operation === 'remove')
            selectedEdgeIds.delete(edgeId);
        else if (operation === 'toggle')
            selectedEdgeIds.has(edgeId) ? selectedEdgeIds.delete(edgeId) : selectedEdgeIds.add(edgeId);

        return { ...state, selectedEdgeIds };
    }
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
    return propagateEvocat(action.graph, { ...state, editor: { ...state.editor, phase: action.phase } });
}
