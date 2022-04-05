import { Signature } from "../identifiers";
import type { SchemaMorphism } from "../schema";
import { AvailabilityStatus, type Node } from "./Node";

export class NodeSequence {
    private nodes: Node[];
    private morphisms: SchemaMorphism[];

    private constructor(nodes: Node[], morphisms: SchemaMorphism[]) {
        this.nodes = nodes;
        this.morphisms = morphisms;
    }

    public static withRootNode(rootNode: Node): NodeSequence {
        const sequence = new NodeSequence([ rootNode ], []);
        sequence.rootNode.select(AvailabilityStatus.Root);
        return sequence;
    }

    public get rootNode(): Node {
        return this.nodes[0];
    }

    public get lastNode(): Node {
        return this.nodes[this.nodes.length - 1];
    }
    /*
    public get allNodes(): Node[] {
        return this.nodes;
    }
    */
    public addSignature(signature: Signature): void {
        signature.toBase().reverse().forEach(baseSignature => this.addBaseSignature(baseSignature));
    }

    public addBaseSignature(baseSignature: Signature): void {
        for (const [ node, morphism ] of this.lastNode.neighbours.entries())
            if (morphism.signature.equals(baseSignature)) {
                this.morphisms.push(morphism);
                this.nodes.push(node);
                node.select();
                return;
            }
    }

    public unselectAll(): void {
        this.nodes.forEach(node => node.unselect());
    }

    public tryAddNode(node: Node): boolean {
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

    public tryRemoveNode(node: Node): boolean {
        if (!this.lastNode.equals(node) || this.rootNode.equals(node))
            return false;

        this.lastNode.unselect();
        this.nodes.pop();
        this.morphisms.pop();

        return true;
    }

    public toSignature(): Signature {
        let output = Signature.empty;
        this.morphisms.forEach(morphism => output = output.concatenate(morphism.signature));

        return output;
    }
}
