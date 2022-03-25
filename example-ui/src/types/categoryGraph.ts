import type { NodeSingular } from "cytoscape";
import { Signature } from "./identifiers";
import type { SchemaMorphism, SchemaObject } from "./schema";

export class SchemaObjectSequence {
    private objectSequence = [] as SchemaObject[];
    private morphismSequence = [] as SchemaMorphism[];

    public constructor(rootObject: SchemaObject) {
        this.objectSequence.push(rootObject);
    }

    public get lastObject(): SchemaObject | null {
        return this.objectSequence[this.objectSequence.length - 1];
    }

    public canAddObject(object: SchemaObject): boolean {
        return !!this.lastObject!.neighbours.get(object) && !this.objectSequence.find(o => o === object);
    }

    public tryAddObject(object: SchemaObject): boolean {
        const morphism = this.lastObject!.neighbours.get(object);

        if (!morphism || this.objectSequence.find(o => o === object))
            return false;

        this.objectSequence.push(object);
        this.morphismSequence.push(morphism);

        return true;
    }

    public toCompositeSignature(): Signature {
        let output = Signature.empty;
        this.morphismSequence.forEach(morphism => output = output.concatenate(morphism.signature));

        return output;
    }

    public get objectIds(): string[] {
        return this.objectSequence.map(object => object.id.toString());
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

    public constructor(schemaObject: SchemaObject) {
    //public constructor(schemaObject: SchemaObject, nodeObject: NodeSingular) {
        this.schemaObject = schemaObject;
        //this.nodeObject = nodeObject;

        //nodeObject.
    }

    public setNode(node: NodeSingular) {
        this.node = node;
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
