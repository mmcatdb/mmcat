import { type GraphSelectEvent, type Graph } from './graphEngine';

export type FreeSelectionAction = {
    operation: 'set' | 'add' | 'remove' | 'toggle';
    nodeId: string;
} | {
    operation: 'set' | 'add' | 'remove' | 'toggle';
    edgeId: string;
} | {
    operation: 'clear';
    range: 'nodes' | 'edges' | 'all';
};

/**
 * Represents a selection of nodes and edges in the graph. The order doesn't matter.
 */
export class FreeSelection {
    private constructor(
        readonly nodeIds: Set<string>,
        readonly edgeIds: Set<string>,
    ) {}

    static create(nodeIds: string[] = [], edgeIds: string[] = []): FreeSelection {
        return new FreeSelection(new Set(nodeIds), new Set(edgeIds));
    }

    /**
     * Handle high-level selection, e.g., in some UI menu.
     */
    updateFromAction(action: FreeSelectionAction): FreeSelection {
        const { operation } = action;

        if (operation === 'clear') {
            const nodeIds = (action.range === 'nodes' || action.range === 'all') ? new Set<string>() : this.nodeIds;
            const edgeIds = (action.range === 'edges' || action.range === 'all') ? new Set<string>() : this.edgeIds;
            return new FreeSelection(nodeIds, edgeIds);
        }

        if (operation === 'set') {
            return 'nodeId' in action
                ? FreeSelection.create([ action.nodeId ], [])
                : FreeSelection.create([], [ action.edgeId ]);
        }

        if ('nodeId' in action) {
            const nodeId = action.nodeId;
            const selected = new Set(this.nodeIds);

            if (operation === 'add')
                selected.add(nodeId);
            else if (operation === 'remove')
                selected.delete(nodeId);
            else if (operation === 'toggle')
                selected.has(nodeId) ? selected.delete(nodeId) : selected.add(nodeId);

            return new FreeSelection(selected, this.edgeIds);
        }
        else {
            const edgeId = action.edgeId;
            const selected = new Set(this.edgeIds);

            if (operation === 'add')
                selected.add(edgeId);
            else if (operation === 'remove')
                selected.delete(edgeId);
            else if (operation === 'toggle')
                selected.has(edgeId) ? selected.delete(edgeId) : selected.add(edgeId);

            return new FreeSelection(this.nodeIds, selected);
        }
    }

    /**
     * Handle low-level graph events.
     */
    updateFromGraphEvent(event: GraphSelectEvent): FreeSelection {
        if (!event.isSpecialKey)
        // The default case - we select exactly the given nodes.
            return new FreeSelection(new Set(event.nodeIds), new Set(event.edgeIds));

        // A special key is pressed - we toggle the given nodes and edges.
        const nodeIds = new Set(this.nodeIds);
        event.nodeIds.forEach(nodeId => {
            nodeIds.has(nodeId) ? nodeIds.delete(nodeId) : nodeIds.add(nodeId);
        });

        const edgeIds = new Set(this.edgeIds);
        event.edgeIds.forEach(edgeId => {
            edgeIds.has(edgeId) ? edgeIds.delete(edgeId) : edgeIds.add(edgeId);
        });

        return new FreeSelection(nodeIds, edgeIds);
    }

    /**
     * Propagates changes made on the graph to the selection reactive state. I.e., makes sure the new selection is compatible with the new graph.
     */
    updateFromGraph(graph: Graph): FreeSelection {
        let nodeIds = this.nodeIds;

        // Just few optimizations (no need to search for the nodes if there are none selected, and keep the old state if it didn't change).
        if (this.nodeIds.size > 0) {
            const filtered = this.nodeIds.values()
                .filter(nodeId => graph.nodes.has(nodeId))
                .toArray();

            if (filtered.length !== nodeIds.size)
                nodeIds = new Set(filtered);
        }

        let edgeIds = this.edgeIds;

        // The same optimizations as above.
        if (this.edgeIds.size > 0) {
            const filtered = this.edgeIds.values()
                .filter(edgeId => !!graph.edges.get(edgeId))
                .toArray();

            if (filtered.length !== edgeIds.size)
                edgeIds = new Set(filtered);
        }

        if (nodeIds === this.nodeIds && edgeIds === this.edgeIds)
            return this;

        return new FreeSelection(nodeIds, edgeIds);
    }
}
