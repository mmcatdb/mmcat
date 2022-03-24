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
