import { Node, NodeSequence } from "@/types/categoryGraph";
import type { DatabaseConfiguration } from "@/types/database";
import { Signature } from "@/types/identifiers";

export class SequenceSignature {
    public readonly sequence: NodeSequence;
    public readonly isNull: boolean;

    private constructor(input: Node | NodeSequence, isNull = false) {
        this.sequence = input instanceof Node ? NodeSequence.fromRootNode(input) : input.copy();
        this.isNull = isNull;
    }

    public copy(): SequenceSignature {
        return new SequenceSignature(this.sequence, this.isNull);
    }

    public static empty(rootNode: Node): SequenceSignature {
        return new SequenceSignature(rootNode);
    }

    public static null(rootNode: Node): SequenceSignature {
        return new SequenceSignature(rootNode, true);
    }

    public static fromSignature(signature: Signature, rootNode: Node): SequenceSignature {
        const output = new SequenceSignature(rootNode, signature.isNull);

        if (!signature.isNull)
            output.sequence.addSignature(signature);

        return output;
    }

    public toSignature(): Signature {
        return this.isNull ? Signature.null : this.sequence.toSignature();
    }

    public toString(): string {
        return this.toSignature().toString();
    }

    public equals(signature: SequenceSignature): boolean {
        return this.isNull === signature.isNull && this.sequence.equals(signature.sequence);
    }

    public markAvailablePaths(configuration: DatabaseConfiguration): void {
        this.sequence.lastNode.markAvailablePaths(configuration, this.sequence.lastNode !== this.sequence.rootNode);
    }
}
