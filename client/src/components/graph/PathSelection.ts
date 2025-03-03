/**
 * Represents a sequence of nodes selected by the user that forms a continuous path.
 * There are edges between them (because there might be multiple paths, so we want to prevent any ambiguity).
 */
export class PathSelection {
    private constructor(
        readonly nodeIds: string[],
        /** There are always n-1 edges for n nodes. */
        readonly edgeIds: string[],
    ) {}

    static create(nodeIds: string[] = [], edgeIds: string[] = []): PathSelection {
        if (nodeIds.length !== edgeIds.length + 1)
            throw new Error('There must be n-1 edges for n nodes.');

        return new PathSelection(nodeIds, edgeIds);
    }

    /** Returns a new path with the added node and edge. */
    add(nodeId: string, edgeId: string): PathSelection {
        return new PathSelection([ ...this.nodeIds, nodeId ], [ ...this.edgeIds, edgeId ]);
    }

    /** Updates the path with the added node and edge. */
    addMutable(nodeId: string, edgeId: string) {
        this.nodeIds.push(nodeId);
        this.edgeIds.push(edgeId);
    }

    clone(): PathSelection {
        return new PathSelection([ ...this.nodeIds ], [ ...this.edgeIds ]);
    }

    /** Returns a new path with the last node and edge removed. */
    remove(): PathSelection {
        return new PathSelection(this.nodeIds.slice(0, -1), this.edgeIds.slice(0, -1));
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
}
