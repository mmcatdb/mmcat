import { type Dispatch } from 'react';
import { type SchemaCategory } from '@/types/schema';
import { type GraphAction, type GraphValue } from '../useGraphEngine';

type EditCategoryState = {
    graph: GraphValue;
    // TODO add more
};

export function createInitialState(category: SchemaCategory): EditCategoryState {
    const nodes = category.getObjects().map(object => ({
        id: '' + object.key.value,
        label: object.metadata.label,
        position: { ...object.metadata.position },
    }));

    const edges = category.getMorphisms().map(morphism => ({
        id: '' + morphism.signature.baseValue,
        label: morphism.metadata.label,
        from: '' + morphism.current?.domKey.value,
        to: '' + morphism.current?.codKey.value,
    }));

    return {
        graph: { nodes, edges, selectedNodes: {} },
    };
}

export type EditCategoryDispatch = Dispatch<EditCategoryAction>;

type EditCategoryAction = GraphAction; // TODO add more

export function editCategoryReducer(state: EditCategoryState, action: EditCategoryAction): EditCategoryState {
    switch (action.type) {
    case 'nodeMove':
        return { ...state, graph: { ...state.graph, nodes: state.graph.nodes.map(node => node.id === action.nodeId ? { ...node, position: action.position } : node) } };
    case 'nodeSelect':
        // TODO
        return state;
    default:
        return state;
    }
}
