export type SequenceSelectionAction = {
    operation: 'set' | 'add' | 'remove' | 'toggle';
    nodeId: string;
} | {
    operation: 'clear';
};

/**
 * Represents a sequence of nodes selected by the user. The order matters.
 */
export class SequenceSelection {
    private constructor(
        readonly nodeIds: readonly string[],
        private readonly nodeIdsSet: Set<string>,
    ) {}

    static create(nodeIds: string[] = []): SequenceSelection {
        return new SequenceSelection(nodeIds, new Set(nodeIds));
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

    has(nodeId: string): boolean {
        return this.nodeIdsSet.has(nodeId);
    }

    private addUnchecked(nodeId: string): SequenceSelection {
        const set = new Set(this.nodeIdsSet);
        set.add(nodeId);
        return new SequenceSelection([ ...this.nodeIds, nodeId ], set);
    }

    private removeUnchecked(nodeId: string): SequenceSelection {
        const set = new Set(this.nodeIdsSet);
        set.delete(nodeId);
        return new SequenceSelection(this.nodeIds.filter(id => id !== nodeId), set);
    }

    update(action: SequenceSelectionAction): SequenceSelection {
        const { operation } = action;

        if (operation === 'clear')
            return SequenceSelection.create();

        const nodeId = action.nodeId;

        switch (operation) {
        case 'set':
            return SequenceSelection.create([ nodeId ]);
        case 'add':
            return this.nodeIdsSet.has(nodeId) ? this : this.addUnchecked(nodeId);
        case 'remove':
            return this.nodeIdsSet.has(nodeId) ? this.removeUnchecked(nodeId) : this;
        case 'toggle':
            return this.nodeIdsSet.has(nodeId) ? this.removeUnchecked(nodeId) : this.addUnchecked(nodeId);
        }
    }
}
