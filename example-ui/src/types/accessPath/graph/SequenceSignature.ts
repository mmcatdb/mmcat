import { Node, NodeSequence } from "@/types/categoryGraph";
import type { DatabaseConfiguration } from "@/types/database";
import { Signature } from "@/types/identifiers";

export class SequenceSignature {
    readonly sequence: NodeSequence;
    readonly isNull: boolean;

    private constructor(input: Node | NodeSequence, isNull = false) {
        this.sequence = input instanceof Node ? NodeSequence.fromRootNode(input) : input.copy();
        this.isNull = isNull;
    }

    copy(): SequenceSignature {
        return new SequenceSignature(this.sequence, this.isNull);
    }

    static empty(rootNode: Node): SequenceSignature {
        return new SequenceSignature(rootNode);
    }

    static null(rootNode: Node): SequenceSignature {
        return new SequenceSignature(rootNode, true);
    }

    static fromSignature(signature: Signature, rootNode: Node): SequenceSignature {
        const output = new SequenceSignature(rootNode, signature.isNull);

        if (!signature.isNull)
            output.sequence.addSignature(signature);

        return output;
    }

    get isEmpty(): boolean {
        return this.sequence.lengthOfMorphisms === 0;
    }

    toSignature(): Signature {
        return this.isNull ? Signature.null : this.sequence.toSignature();
    }

    toString(): string {
        return this.toSignature().toString();
    }

    equals(signature: SequenceSignature): boolean {
        return this.isNull === signature.isNull && this.sequence.equals(signature.sequence);
    }

    markAvailablePaths(configuration: DatabaseConfiguration): void {
        this.sequence.lastNode.markAvailablePaths(configuration, this.sequence.lastNode !== this.sequence.rootNode);
    }
}
