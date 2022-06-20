import { Signature } from "../identifiers";
import type { Edge } from "./Edge";
import { AvailabilityStatus, type Node } from "./Node";

export class NodeSequence {
    _nodes: Node[];
    _edges: Edge[];

    private constructor(nodes: Node[], edges: Edge[]) {
        this._nodes = [ ...nodes ];
        this._edges = [ ...edges ];
    }

    copy(): NodeSequence {
        return new NodeSequence(this._nodes, this._edges);
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
        return this._edges.length;
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
        for (const [ node, edge ] of this.lastNode.neighbours.entries()) {
            if (edge.schemaMorphism.signature.equals(baseSignature)) {
                this._edges.push(edge);
                this._nodes.push(node);
                node.selectNext();
                return;
            }
        }
    }

    unselectAll(): void {
        this._nodes.forEach(node => node.unselect());
    }

    selectAll(): void {
        this.rootNode.unselect();
        this._nodes.forEach(node => node.selectNext());
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
        this._edges.pop();

        return true;
    }

    toSignature(): Signature {
        let output = Signature.empty;
        this._edges.forEach(edge => output = output.concatenate(edge.schemaMorphism.signature));

        return output;
    }

    equals(sequence: NodeSequence): boolean {
        if (this.rootNode !== sequence.rootNode)
            return false;

        if (this._edges.length !== sequence._edges.length)
            return false;

        for (let i = 0; i < this._edges.length; i++) {
            if (!this._edges[i].equals(sequence._edges[i]))
                return false;
        }

        return true;
    }
}
