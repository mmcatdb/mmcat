import { type GraphSelectEvent, type Graph } from './graphEngine';

export type GraphSelection = {
    nodeIds: Set<string>;
    edgeIds: Set<string>;
};

export function createDefaultGraphSelection(): GraphSelection {
    return {
        nodeIds: new Set(),
        edgeIds: new Set(),
    };
}

/** Handle low-level graph events. */
export function updateSelectionFromGraphEvent(prev: GraphSelection, event: GraphSelectEvent): GraphSelection {
    if (!event.isSpecialKey)
        // The default case - we select exactly the given nodes.
        return { nodeIds: new Set(event.nodeIds), edgeIds: new Set(event.edgeIds) };

    // A special key is pressed - we toggle the given nodes and edges.
    const nodeIds = new Set(prev.nodeIds);
    event.nodeIds.forEach(nodeId => {
        nodeIds.has(nodeId) ? nodeIds.delete(nodeId) : nodeIds.add(nodeId);
    });

    const edgeIds = new Set(prev.edgeIds);
    event.edgeIds.forEach(edgeId => {
        edgeIds.has(edgeId) ? edgeIds.delete(edgeId) : edgeIds.add(edgeId);
    });

    return { nodeIds, edgeIds };
}

export type UserSelectAction = {
    operation: 'set' | 'add' | 'remove' | 'toggle';
    nodeId: string;
} | {
    operation: 'set' | 'add' | 'remove' | 'toggle';
    edgeId: string;
} | {
    operation: 'clear';
    range: 'nodes' | 'edges' | 'all';
};

/** Handle high-level selection, e.g., in some UI menu. */
export function updateSelectionFromUserAction(prev: GraphSelection, action: UserSelectAction): GraphSelection {
    const { operation } = action;

    if (operation === 'clear') {
        return {
            nodeIds: (action.range === 'nodes' || action.range === 'all') ? new Set<string>() : prev.nodeIds,
            edgeIds: (action.range === 'edges' || action.range === 'all') ? new Set<string>() : prev.edgeIds,
        };
    }

    if (operation === 'set') {
        return 'nodeId' in action
            ? { nodeIds: new Set([ action.nodeId ]), edgeIds: new Set() }
            : { edgeIds: new Set([ action.edgeId ]), nodeIds: new Set() };
    }

    if ('nodeId' in action) {
        const nodeId = action.nodeId;
        const selected = new Set(prev.nodeIds);

        if (operation === 'add')
            selected.add(nodeId);
        else if (operation === 'remove')
            selected.delete(nodeId);
        else if (operation === 'toggle')
            selected.has(nodeId) ? selected.delete(nodeId) : selected.add(nodeId);

        return { ...prev, nodeIds: selected };
    }
    else {
        const edgeId = action.edgeId;
        const selected = new Set(prev.edgeIds);

        if (operation === 'add')
            selected.add(edgeId);
        else if (operation === 'remove')
            selected.delete(edgeId);
        else if (operation === 'toggle')
            selected.has(edgeId) ? selected.delete(edgeId) : selected.add(edgeId);

        return { ...prev, edgeIds: selected };
    }
}

/**
 * Propagates changes made on the graph to the selection reactive state. I.e., makes sure the new selection is compatible with the new graph.
 */
export function updateSelectionFromGraph(prev: GraphSelection, graph: Graph): GraphSelection {
    const next = {
        nodeIds: new Set(),
        edgeIds: new Set(),
    } satisfies GraphSelection;

    // Just few optimizations (no need to search for the nodes if there are none selected, and keep the old state if it didn't change).
    if (prev.nodeIds.size > 0) {
        graph.nodes
            .filter(node => prev.nodeIds.has(node.id))
            .forEach(node => next.nodeIds.add(node.id));
    }
    if (prev.nodeIds.size === next.nodeIds.size)
        next.nodeIds = prev.nodeIds;

    // The same optimizations as above.
    if (prev.edgeIds.size > 0) {
        graph.edges
            .filter(edge => prev.edgeIds.has(edge.id))
            .forEach(edge => next.edgeIds.add(edge.id));
    }
    if (prev.edgeIds.size === next.edgeIds.size)
        next.edgeIds = prev.edgeIds;

    return next;
}
