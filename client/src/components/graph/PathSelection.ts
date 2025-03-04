import { type GraphSelection } from './graphSelection';

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
export class PathSelection implements GraphSelection {
    private constructor(
        readonly nodeIds: readonly string[],
        /** There are always n-1 edges for n nodes. Except for when the path is empty. */
        readonly edgeIds: readonly string[],
    ) {}

    static create(nodeIds: string[] = [], edgeIds: string[] = []): PathSelection {
        if (nodeIds.length !== edgeIds.length + 1 && (nodeIds.length > 0 || edgeIds.length > 0))
            throw new Error('There must be n-1 edges for n nodes.');

        return new PathSelection([ ...nodeIds ], [ ...edgeIds ]);
    }

    values(): readonly string[] {
        return this.nodeIds;
    }

    get isEmpty(): boolean {
        return this.nodeIds.length === 0;
    }

    get firstNodeId(): string {
        return this.nodeIds[0];
    }

    get lastNodeId(): string {
        return this.nodeIds[this.nodeIds.length - 1];
    }

    updateFromAction(action: PathSelectionAction): PathSelection {
        if (action.operation === 'start')
            return PathSelection.create([ action.nodeId ]);
        if (action.operation === 'remove')
            return new PathSelection(this.nodeIds.slice(0, -1), this.edgeIds.slice(0, -1));

        const { nodeIds, edgeIds } = action;
        if (nodeIds.length !== edgeIds.length)
            throw new Error('Can\'t add different number of nodes and edges.');
        if (this.isEmpty)
            throw new Error('Can\'t add to an empty path.');

        return new PathSelection([ ...this.nodeIds, ...nodeIds ], [ ...this.edgeIds, ...edgeIds ]);
    }
}
