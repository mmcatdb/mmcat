import type { PathGraph, PathGraphProvider } from './PathGraph';

export type PathSelectionAction = {
    /** Create the initial path. */
    operation: 'start';
    nodeId: string;
} | {
    /** Remove the last element. */
    operation: 'remove';
} | {
    /** Add some nodes (and the same number of edges). */
    operation: 'add';
    nodeIds: string[];
    edgeIds: string[];
};

/**
 * Represents a sequence of nodes selected by the user that forms a continuous path.
 * There are edges between them (because there might be multiple paths, so we want to prevent any ambiguity).
 */
export class PathSelection {
    private constructor(
        readonly provider: PathGraphProvider,
        /** This array can't be empty. */
        readonly nodeIds: readonly string[],
        /** There are always n-1 edges for n nodes. */
        readonly edgeIds: readonly string[],
    ) {}

    static create(provider: PathGraphProvider, nodeIds: string[] = [], edgeIds: string[] = []): PathSelection {
        // This also checks that the nodeIds aren't empty.
        if (nodeIds.length !== edgeIds.length + 1)
            throw new Error('There must be n-1 edges for n nodes.');

        return new PathSelection(provider, [ ...nodeIds ], [ ...edgeIds ]);
    }

    values(): readonly string[] {
        return this.nodeIds;
    }

    get isEmpty(): boolean {
        return this.nodeIds.length === 1;
    }

    get firstNodeId(): string {
        return this.nodeIds[0];
    }

    get lastNodeId(): string {
        return this.nodeIds[this.nodeIds.length - 1];
    }

    update(action: PathSelectionAction): PathSelection {
        if (action.operation === 'start')
            return PathSelection.create(this.provider, [ action.nodeId ]);
        if (action.operation === 'remove')
            return PathSelection.create(this.provider, this.nodeIds.slice(0, -1), this.edgeIds.slice(0, -1));

        return this.add(action.nodeIds, action.edgeIds);
    }

    add(nodeIds: string[], edgeIds: string[]): PathSelection {
        if (nodeIds.length !== edgeIds.length)
            throw new Error('Can\'t add different number of nodes and edges.');

        return new PathSelection(this.provider, [ ...this.nodeIds, ...nodeIds ], [ ...this.edgeIds, ...edgeIds ]);
    }

    /**
     * Cached structure for finding paths for the selection.
     * Should be valid for the lifetime of this selection (if the category changes, a new selection should be created anyway).
     */
    private _pathGraph: PathGraph | undefined;

    get pathGraph(): PathGraph {
        if (!this._pathGraph)
            this._pathGraph = this.provider.computePathGraph(this.lastNodeId);

        return this._pathGraph;
    }
}
