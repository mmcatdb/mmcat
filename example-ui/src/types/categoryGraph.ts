import type { NodeSingular } from "cytoscape";
import { Signature } from "./identifiers";
import type { SchemaMorphism, SchemaObject } from "./schema";

export class NodeSequence {
    private nodes: NodeSchemaData[];
    private morphisms: SchemaMorphism[];

    private constructor(nodes: NodeSchemaData[], morphisms: SchemaMorphism[]) {
        this.nodes = nodes;
        this.morphisms = morphisms;
    }

    public static withRootNode(rootNode: NodeSchemaData): NodeSequence {
        return new NodeSequence([ rootNode ], []);
    }

    public static copy(sequence: NodeSequence): NodeSequence {
        return new NodeSequence(sequence.nodes, sequence.morphisms);
    }

    private get rootNode(): NodeSchemaData {
        return this.nodes[0];
    }

    private get lastNode(): NodeSchemaData {
        return this.nodes[this.nodes.length - 1];
    }

    public get allNodes(): NodeSchemaData[] {
        return this.nodes;
    }

    public addSignature(signature: Signature): void {
        signature.toBase().reverse().forEach(baseSignature => this.addBaseSignature(baseSignature));
    }

    public addBaseSignature(baseSignature: Signature): void {
        for (const [ node, morphism ] of this.lastNode.neighbours.entries())
            if (morphism.signature.equals(baseSignature)) {
                this.morphisms.push(morphism);
                this.nodes.push(node);
                return;
            }
    }

    public tryAddNode(node: NodeSchemaData): boolean {
        const morphism = this.lastNode.neighbours.get(node);

        if (!morphism || this.nodes.find(o => o === node))
            return false;

        this.nodes.push(node);
        this.morphisms.push(morphism);

        return true;
    }

    public tryRemoveNode(node: NodeSchemaData): boolean {
        if (!this.lastNode.equals(node) || this.rootNode.equals(node))
            return false;

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

export enum NodeTag {
    Root,
    Selected
}

export class NodeSchemaData {
    public schemaObject: SchemaObject;
    public node!: NodeSingular;
    private tags = new Set() as Set<NodeTag>;

    public neighbours = new Map() as Map<NodeSchemaData, SchemaMorphism>; // TODO

    public constructor(schemaObject: SchemaObject) {
    //public constructor(schemaObject: SchemaObject, nodeObject: NodeSingular) {
        this.schemaObject = schemaObject;
        //this.nodeObject = nodeObject;

        //nodeObject.
    }

    public setNode(node: NodeSingular) {
        this.node = node;
    }

    public addNeighbour(object: NodeSchemaData, morphism: SchemaMorphism): void {
        this.neighbours.set(object, morphism);
    }

    /*
    public addTag(tag: NodeTag): void {
        this.tags.add(tag);
    }

    public removeTag(tag: NodeTag): void {
        this.tags.delete(tag);
    }

    public get style(): string {
        let output = '';

        if (this.tags.has(NodeTag.Root))
            output += 'background-color: red';

        return output;
    }
    */

    public select(): void {
        this.tags.add(NodeTag.Selected);
        this.node.addClass('selected');
    }

    public unselect(): void {
        this.tags.delete(NodeTag.Selected);
        this.node.removeClass('selected');
    }

    public becomeRoot(): void {
        this.tags.add(NodeTag.Root);
        this.node.addClass('root');
    }

    public equals(other: NodeSchemaData | null): boolean {
        return !!other && this.schemaObject.id === other.schemaObject.id;
    }
}
