import { type Dispatch } from 'react';
import { type GraphSelection, type UserSelectAction, createDefaultGraphSelection, updateSelectionFromGraphEvent, updateSelectionFromUserAction } from '../graph/graphSelection';
import { type CategoryEdge, type CategoryGraph, type CategoryNode, categoryToGraph } from '../category/categoryGraph';
import { type GraphEvent } from '../graph/graphEngine';
import { type Category } from '@/types/schema';
import { type Mapping } from '@/types/mapping';
import { computePathsFromObjex, type PathEdge, type PathNode, type PathGraph } from '@/types/schema/PathMarker';
import { GraphPath } from '../graph/graphPath';

export type EditMappingState = {
    category: Category;
    graph: CategoryGraph;
    selection: GraphSelection;
    mapping: Mapping;
    // phase: EditorPhase;
    path?: PathState;
    isPathInput: boolean;
};

export function createInitialState({ category, mapping }: { category: Category, mapping: Mapping }): EditMappingState {
    return {
        category,
        graph: categoryToGraph(category),
        selection: createDefaultGraphSelection(),
        mapping,
        // phase: EditorPhase.default,
        isPathInput: true,
    };
}

export type EditMappingDispatch = Dispatch<EditMappingAction>;

// type EditMappingAction = GraphAction | SelectAction | PhaseAction;
type EditMappingAction = GraphAction | SelectAction | PathAction;

export function editMappingReducer(state: EditMappingState, action: EditMappingAction): EditMappingState {
    console.log('REDUCE', action, state);

    switch (action.type) {
    case 'graph': return graph(state, action);
    case 'select': return select(state, action);
    // case 'phase': return phase(state, action);
    case 'path': return path(state, action);
    }
}

// Low-level graph library events

type GraphAction = {
    type: 'graph';
    event: GraphEvent;
};

function graph(state: EditMappingState, { event }: GraphAction): EditMappingState {
    switch (event.type) {
    case 'move': {
        // TODO This is not supported, alghough it should be. Probably would require a new way how to handle metadata ...
        return state;

        // const node = state.graph.nodes.find(node => node.id === event.nodeId);
        // if (!node)
        //     return state;

        // state.evocat.updateObjex(node.schema.key, { position: event.position });

        // return {
        //     ...state,
        //     graph: categoryToGraph(state.evocat.category),
        // };
    }
    case 'select': {
        const newState = {
            ...state,
            selection: updateSelectionFromGraphEvent(state.selection, event),
        };

        // If the canvas was clicked, we clear the path selection.
        if (newState.selection.nodeIds.size === 0 && newState.selection.edgeIds.size === 0)
            newState.path = undefined;

        return newState;
    }
    }
}

// Selection

type SelectAction = {
    type: 'select';
} & UserSelectAction;

function select(state: EditMappingState, action: SelectAction): EditMappingState {
    const newState: EditMappingState = {
        ...state,
        selection: updateSelectionFromUserAction(state.selection, action),
        path: undefined,
    };

    return newState;
}

type PathState = {
    graph: PathGraph;
    selection: GraphPath<CategoryNode, CategoryEdge>;
};

type PathAction = {
    type: 'path';
} & ({
    /** For creating the initial path / removing the last node. */
    node: CategoryNode;
} | {
    pathNode: PathNode;
} | {
    pathEdge: PathEdge;
});

function path(state: EditMappingState, action: PathAction): EditMappingState {
    const selection = pathSelection(state, action);

    if (selection.isEmpty)
        return { ...state, path: undefined };

    const objex = state.category.getObjex(selection.lastNode.schema.key);

    return {
        ...state,
        path: {
            selection,
            graph: computePathsFromObjex(objex),
        },
    };
}

function pathSelection(state: EditMappingState, action: PathAction): GraphPath<CategoryNode, CategoryEdge> {
    const path = state.path;

    if ('node' in action) {
        if (!path)
            return GraphPath.create<CategoryNode, CategoryEdge>(action.node);

        if (path.selection.lastNode.id !== action.node.id)
            throw new Error('Invalid node.');

        return path.selection.remove();
    }

    if (!path)
        throw new Error('Missing path.');

    if ('pathNode' in action) {
        const output = path.selection.clone();
        addPathToNode(action.pathNode, output, state.graph, path.graph);
        return output;
    }

    const output = path.selection.clone();

    const pathEdge = action.pathEdge;
    const fromObjex = pathEdge.traversableDirection ? pathEdge.morphism.from : pathEdge.morphism.to;
    const fromPathNode = path.graph.nodes.get(fromObjex.key.toString());

    addPathToNode(fromPathNode!, output, state.graph, path.graph);

    const toObjex = pathEdge.traversableDirection ? pathEdge.morphism.to : pathEdge.morphism.from;
    const toNode = state.graph.nodes.find(node => node.id === toObjex.key.toString());
    const edge = state.graph.edges.find(edge => edge.id === pathEdge.morphism.signature.toString());

    output.addMutable(toNode!, edge!);

    return output;
}

function addPathToNode(pathNode: PathNode, selection: GraphPath<CategoryNode, CategoryEdge>, graph: CategoryGraph, pathGraph: PathGraph): void {
    const nodes: PathNode[] = [];
    const edges: PathEdge[] = [];

    let current = pathNode;

    while (current.pathSegmentTo) {
        nodes.push(current);
        edges.push(current.pathSegmentTo.edge);
        current = pathGraph.nodes.get(current.pathSegmentTo.from.key.toString())!;
    }

    for (let i = nodes.length - 1; i >= 0; i--) {
        selection.addMutable(
            graph.nodes.find(node => node.id === nodes[i].id)!,
            graph.edges.find(edge => edge.id === edges[i].id)!,
        );
    }
}


// TODO This

// Editor phases

// export enum EditorPhase {
//     default = 'default',
// }

// export type PhaseAction = {
//     type: 'phase';
//     /** The phase we want to switch to. */
//     phase: EditorPhase;
//     /** If defined, the graph state should be updated by this value. */
//     graph?: CategoryGraph;
// }

// function phase(state: EditMappingState, { phase, graph }: PhaseAction): EditMappingState {
//     if (!graph)
//         return { ...state, phase };

//     return {
//         ...state,
//         graph,
//         selection: updateSelectionFromGraph(state.selection, graph),
//         phase,
//     };
// }
