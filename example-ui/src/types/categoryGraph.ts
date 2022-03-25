import type { NodeSingular } from "cytoscape";
import { Signature } from "./identifiers";
import type { SchemaMorphism, SchemaObject } from "./schema";

export class NodeSequence {
    private nodeSequence: NodeSchemaData[];
    private morphismSequence = [] as SchemaMorphism[];

    public constructor(rootNode: NodeSchemaData) {
        this.nodeSequence = [ rootNode ];
    }

    private get rootNode(): NodeSchemaData {
        return this.nodeSequence[0];
    }

    private get lastNode(): NodeSchemaData {
        return this.nodeSequence[this.nodeSequence.length - 1];
    }

    public get allNodes(): NodeSchemaData[] {
        return this.nodeSequence;
    }

    public tryAddNode(node: NodeSchemaData): boolean {
        const morphism = this.lastNode.neighbours.get(node);

        if (!morphism || this.nodeSequence.find(o => o === node))
            return false;

        this.nodeSequence.push(node);
        this.morphismSequence.push(morphism);

        return true;
    }

    public tryRemoveNode(node: NodeSchemaData): boolean {
        if (this.lastNode !== node || this.rootNode === node)
            return false;

        this.nodeSequence.pop();
        this.morphismSequence.pop();

        return true;
    }

    public toCompositeSignature(): Signature {
        let output = Signature.empty;
        this.morphismSequence.forEach(morphism => output = output.concatenate(morphism.signature));

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

    public neighbours = new Map() as Map<NodeSchemaData, SchemaMorphism>;

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
