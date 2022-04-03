import { Signature } from "../identifiers";
import type { SchemaMorphism } from "../schema";
import { AvailabilityStatus, type NodeSchemaData } from "./NodeSchemaData";

export class NodeSequence {
    private nodes: NodeSchemaData[];
    private morphisms: SchemaMorphism[];

    private constructor(nodes: NodeSchemaData[], morphisms: SchemaMorphism[]) {
        this.nodes = nodes;
        this.morphisms = morphisms;
    }

    public static withRootNode(rootNode: NodeSchemaData): NodeSequence {
        const sequence = new NodeSequence([ rootNode ], []);
        sequence.rootNode.select(AvailabilityStatus.Root);
        return sequence;
    }

    public get rootNode(): NodeSchemaData {
        return this.nodes[0];
    }

    public get lastNode(): NodeSchemaData {
        return this.nodes[this.nodes.length - 1];
    }
    /*
    public get allNodes(): NodeSchemaData[] {
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

    public tryAddNode(node: NodeSchemaData): boolean {
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

    public tryRemoveNode(node: NodeSchemaData): boolean {
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
