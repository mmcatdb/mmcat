/**
 * Represents a sequence of nodes selected by the user. The order matters.
 */
export class SequenceSelection {
    private constructor(
        readonly nodeIds: string[],
    ) {}

    static create(nodeIds: string[] = []): SequenceSelection {
        return new SequenceSelection(nodeIds);
    }

    /** Returns a new sequence with the added node. */
    add(nodeId: string): SequenceSelection {
        return new SequenceSelection([ ...this.nodeIds, nodeId ]);
    }

    /** Updates the path with the added node. */
    addMutable(nodeId: string) {
        this.nodeIds.push(nodeId);
    }

    clone(): SequenceSelection {
        return new SequenceSelection([ ...this.nodeIds ]);
    }

    /** Returns a new sequnce with the last node removed. */
    remove(): SequenceSelection {
        return new SequenceSelection(this.nodeIds.slice(0, -1));
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
