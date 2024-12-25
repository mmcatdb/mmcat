import { type Dispatch } from 'react';
import { type SchemaCategory } from '@/types/schema';
import { type GraphAction, type GraphValue } from '../graph/graphEngine';

export type EditCategoryState = {
    graph: GraphValue;
    selectedNodeIds: Set<string>;
};

export function createInitialState(category: SchemaCategory): EditCategoryState {
    const nodes = category.getObjexes().map(objex => ({
        id: '' + objex.key.value,
        label: objex.metadata.label,
        position: { ...objex.metadata.position },
    }));

    const edges = category.getMorphisms().map(morphism => ({
        id: '' + morphism.signature.baseValue,
        label: morphism.metadata.label,
        from: '' + morphism.current?.domKey.value,
        to: '' + morphism.current?.codKey.value,
    }));

    return {
        graph: { nodes, edges },
        selectedNodeIds: new Set(),
    };
}

export type EditCategoryDispatch = Dispatch<EditCategoryAction>;

type EditCategoryAction = GraphAction | SelectNodeAction;

export function editCategoryReducer(state: EditCategoryState, action: EditCategoryAction): EditCategoryState {
    switch (action.type) {
    case 'graph': return graph(state, action);
    case 'selectNode': return selectNode(state, action);
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
