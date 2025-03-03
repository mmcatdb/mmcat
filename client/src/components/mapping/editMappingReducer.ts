import { type Dispatch } from 'react';
import { FreeSelection, type FreeSelectionAction, PathSelection } from '../graph/graphSelection';
import { type CategoryGraph, type CategoryNode, categoryToGraph } from '../category/categoryGraph';
import { type GraphEvent } from '../graph/graphEngine';
import { type Category } from '@/types/schema';
import { type Mapping } from '@/types/mapping';
import { computePathsFromObjex, type PathEdge, type PathNode, type PathGraph } from '@/types/schema/PathMarker';

export type EditMappingState = {
    category: Category;
    graph: CategoryGraph;
    selection: FreeSelection;
    mapping: Mapping;
    // phase: EditorPhase;
    path?: PathState;
    isPathInput: boolean;
};

export function createInitialState({ category, mapping }: { category: Category, mapping: Mapping }): EditMappingState {
    return {
        category,
        graph: categoryToGraph(category),
        selection: FreeSelection.create(),
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

        // const node = state.graph.nodes.get(event.nodeId);
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
            selection: state.selection.updateFromGraphEvent(event),
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
} & FreeSelectionAction;

function select(state: EditMappingState, action: SelectAction): EditMappingState {
    const newState: EditMappingState = {
        ...state,
        selection: state.selection.updateFromAction(action),
        path: undefined,
    };

    return newState;
}

type PathState = {
    graph: PathGraph;
    selection: PathSelection;
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

    const node = state.graph.nodes.get(selection.lastNodeId)!;
    const objex = state.category.getObjex(node.schema.key);

    return {
        ...state,
        path: {
            selection,
            graph: computePathsFromObjex(objex),
        },
    };
}

function pathSelection(state: EditMappingState, action: PathAction): PathSelection {
    const path = state.path;

    if ('node' in action) {
        if (!path)
            return PathSelection.create([ action.node.id ]);

        if (path.selection.lastNodeId !== action.node.id)
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
    const fromNodeId = pathEdge.traversableDirection ? pathEdge.from : pathEdge.to;
    const fromPathNode = path.graph.nodes.get(fromNodeId);

    addPathToNode(fromPathNode!, output, state.graph, path.graph);

    const toNodeId = pathEdge.traversableDirection ? pathEdge.to : pathEdge.from;

    output.addMutable(toNodeId, pathEdge.id);

    return output;
}

function addPathToNode(pathNode: PathNode, selection: PathSelection, graph: CategoryGraph, pathGraph: PathGraph): void {
    const nodes: PathNode[] = [];
    const edges: PathEdge[] = [];

    let current = pathNode;

    while (current.pathSegmentTo) {
        nodes.push(current);
        edges.push(current.pathSegmentTo.edge);
        current = pathGraph.nodes.get(current.pathSegmentTo.from.key.toString())!;
    }

    for (let i = nodes.length - 1; i >= 0; i--)
        selection.addMutable(nodes[i].id, edges[i].id);
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
//         selection: state.selection.updateFromGraph(graph),
//         phase,
//     };
// }
