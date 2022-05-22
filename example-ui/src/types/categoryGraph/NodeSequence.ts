import { Signature } from "../identifiers";
import type { SchemaMorphism } from "../schema";
import { AvailabilityStatus, type Node } from "./Node";

export class NodeSequence {
    _nodes: Node[];
    _morphisms: SchemaMorphism[];

    private constructor(nodes: Node[], morphisms: SchemaMorphism[]) {
        this._nodes = [ ...nodes ];
        this._morphisms = [ ...morphisms ];
    }

    copy(): NodeSequence {
        return new NodeSequence(this._nodes, this._morphisms);
    }

    static fromRootNode(rootNode: Node): NodeSequence {
        const sequence = new NodeSequence([ rootNode ], []);
        return sequence;
    }

    get rootNode(): Node {
        return this._nodes[0];
    }

    get lastNode(): Node {
        return this._nodes[this._nodes.length - 1];
    }

    get lengthOfMorphisms(): number {
        return this._morphisms.length;
    }
    /*
    get allNodes(): Node[] {
        return this.nodes;
    }
    */
    addSignature(signature: Signature): void {
        signature.toBases().reverse().forEach(baseSignature => this.addBaseSignature(baseSignature));
    }

    addBaseSignature(baseSignature: Signature): void {
        for (const [ node, morphism ] of this.lastNode.neighbours.entries()) {
            if (morphism.signature.equals(baseSignature)) {
                this._morphisms.push(morphism);
                this._nodes.push(node);
                node.selectNext();
                return;
            }
        }
    }

    unselectAll(): void {
        this._nodes.forEach(node => node.unselect());
    }

    tryAddNode(node: Node): boolean {
        /*
        const morphism = this.lastNode.neighbours.get(node);

        if (!morphism || this.nodes.find(o => o === node))
            return false;

        this.nodes.push(node);
        this.morphisms.push(morphism);
        */

        if (node.availabilityStatus !== AvailabilityStatus.Available && node.availabilityStatus !== AvailabilityStatus.CertainlyAvailable)
            return false;

        if (!node.availablePathData)
            return false;

        this.addSignature(node.availablePathData.signature);

        return true;
    }

    tryRemoveNode(node: Node): boolean {
        if (!this.lastNode.equals(node) || this._nodes.length === 1)
            return false;

        this.lastNode.unselectPrevious();
        this._nodes.pop();
        this._morphisms.pop();

        return true;
    }

    toSignature(): Signature {
        let output = Signature.empty;
        this._morphisms.forEach(morphism => output = output.concatenate(morphism.signature));

        return output;
    }

    equals(sequence: NodeSequence): boolean {
        if (this.rootNode !== sequence.rootNode)
            return false;

        if (this._morphisms.length !== sequence._morphisms.length)
            return false;

        for (let i = 0; i < this._morphisms.length; i++) {
            if (!this._morphisms[i].signature.equals(sequence._morphisms[i].signature))
                return false;
        }

        return true;
    }
}
