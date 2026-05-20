/**
 * A distinct class from {@link FreeSelection} because we might want to add more complex highlighting (e.g., custom colors) later.
 */
export class GraphHighlights {
    private constructor(
        readonly nodeIds: Set<string>,
        readonly edgeIds: Set<string>,
    ) {}

    static create(nodeIds: string[] = [], edgeIds: string[] = []): GraphHighlights {
        return new GraphHighlights(new Set(nodeIds), new Set(edgeIds));
    }
}
