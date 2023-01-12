import { Node, NodeSequence, type FilterFunction } from "@/types/categoryGraph";
import type { Signature } from "@/types/identifiers";

export class SequenceSignature {
    readonly sequence: NodeSequence;

    private constructor(input: Node | NodeSequence) {
        this.sequence = input instanceof Node ? NodeSequence.fromRootNode(input) : input.copy();
    }

    copy(): SequenceSignature {
        return new SequenceSignature(this.sequence);
    }

    static empty(rootNode: Node): SequenceSignature {
        return new SequenceSignature(rootNode);
    }

    static fromSignature(signature: Signature, rootNode: Node): SequenceSignature {
        const output = new SequenceSignature(rootNode);
        output.sequence.addSignature(signature);

        return output;
    }

    get isEmpty(): boolean {
        return this.sequence.lengthOfMorphisms === 0;
    }

    toSignature(): Signature {
        return this.sequence.toSignature();
    }

    toString(): string {
        return this.toSignature().toString();
    }

    equals(signature: SequenceSignature): boolean {
        return this.sequence.equals(signature.sequence);
    }

    markAvailablePaths(filters: FilterFunction | FilterFunction[]): void {
        if (this.sequence.lengthOfMorphisms === 0) {
            this.sequence.rootNode.unselect();
            this.sequence.rootNode.selectNext();
        }

        this.sequence.lastNode.markAvailablePaths(filters);
    }
}
