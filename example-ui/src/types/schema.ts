import type { Position } from "cytoscape";
import { Signature } from "./identifiers";

export class ComparablePosition implements Position {
    public x!: number;
    public y!: number;

    public constructor(input: Position) {
        Object.assign(this, input);
    }

    public equals(object?: Position) : boolean {
        return !!object && this.x === object.x && this.y === object.y;
    }
}
/*
export class SchemaObject {
    public key: number | undefined;
    public label: number | undefined;

    public static fromServer(input: any): SchemaObject {
        const object = new SchemaObject();

        object.key = input.key.value;
        object.label = input.label;

        return object;
    }
}

export class SchemaObjectFromServer {

}

export class SchemaMorphism {
    public domKey: number | undefined;
    public codKey: number | undefined;

    public static fromServer(input: any): SchemaMorphism {
        const morphism = new SchemaMorphism();

        morphism.domKey = input.domIdentifier.value;
        morphism.codKey = input.codIdentifier.value;

        return morphism;
    }
}
*/

export class SchemaObject {
    //public key: number | undefined;
    //public label: number | undefined;

    public id!: number;
    public label!: string;
    public jsonValue!: string;
    public position?: ComparablePosition;
    private originalPosition?: ComparablePosition;

    private constructor() {}

    public static fromServer(input: SchemaObjectFromServer): SchemaObject {
        const object = new SchemaObject();

        //object.key = input.key.value;
        //object.label = input.label;
        object.label = JSON.parse(input.jsonValue).label;
        object.id = input.id;
        object.jsonValue = input.jsonValue;
        if (input.position) {
            object.position = new ComparablePosition(input.position);
            object.originalPosition = new ComparablePosition(input.position);
        }

        return object;
    }

    public toPositionUpdateToServer(): PositionUpdateToServer | null {
        return this.position?.equals(this.originalPosition) ? null : new PositionUpdateToServer({ schemaObjectId: this.id, position: this.position });
    }
}

export class SchemaObjectFromServer {
    public id!: number;
    public jsonValue!: string;
    public position?: Position;
}

export class PositionUpdateToServer {
    public schemaObjectId!: number;
    public position!: Position;

    public constructor(input?: Partial<PositionUpdateToServer>) {
        Object.assign(this, input);
    }
}

export enum Cardinality {
    Zero = 'ZERO',
    One = 'ONE',
    Star = 'STAR'
}

export class SchemaMorphism {
    //public domKey: number | undefined;
    //public codKey: number | undefined;
    public id!: number;
    public domId!: number;
    public codId!: number;
    public signature!: Signature;
    public min!: Cardinality.Zero | Cardinality.One;
    public max!: Cardinality.One | Cardinality.Star;

    //public domObject!: SchemaObject;
    //public codObject!: SchemaObject;

    //private jsonValue!: string;

    public get isBase(): boolean {
        return this.signature.isBase;
    }

    //public static fromServer(input: SchemaMorphismFromServer, objects: SchemaObject[]): SchemaMorphism {
    public static fromServer(input: SchemaMorphismFromServer): SchemaMorphism {
        const morphism = new SchemaMorphism();

        //morphism.domKey = input.domIdentifier.value;
        //morphism.codKey = input.codIdentifier.value;

        morphism.id = input.id;
        morphism.domId = input.domId;
        morphism.codId = input.codId;

        const parsedJson = JSON.parse(input.jsonValue);
        morphism.signature = Signature.fromJSON(parsedJson.signature);
        morphism.min = parsedJson.min;
        morphism.max = parsedJson.max;

        /*
        const domObject = objects.find(object => object.id === morphism.domId);
        if (!domObject)
            throw new Error(`Domain object with id ${morphism.domId} not found for morphism ${morphism.signature.toString()}.`);
        morphism.domObject = domObject;

        const codObject = objects.find(object => object.id === morphism.codId);
        if (!codObject)
            throw new Error(`Codomain object with id ${morphism.codId} not found for morphism ${morphism.signature.toString()}.`);
        morphism.codObject = codObject;

        if (morphism.isBase) {
            domObject.addNeighbour(codObject, morphism);
            codObject.addNeighbour(domObject, morphism);
        }
        */

        //morphism.jsonValue = input.jsonValue;

        return morphism;
    }
}

export class SchemaMorphismFromServer {
    public id!: number;
    public domId!: number;
    public codId!: number;
    public jsonValue!: string;
}

export class SchemaCategory {
    public id!: number;
    public jsonValue!: string;
    public objects!: SchemaObject[];
    public morphisms!: SchemaMorphism[];

    private constructor() {}

    public static fromServer(input: SchemaCategoryFromServer): SchemaCategory {
        const category = new SchemaCategory;
        category.id = input.id;
        category.jsonValue = input.jsonValue;
        category.objects = input.objects.map(object => SchemaObject.fromServer(object));
        //category.morphisms = input.morphisms.map(morphism => SchemaMorphism.fromServer(morphism, category.objects));
        category.morphisms = input.morphisms.map(morphism => SchemaMorphism.fromServer(morphism));

        return category;
    }
}

export class SchemaCategoryFromServer {
    public id!: number;
    public jsonValue!: string;
    public objects!: SchemaObjectFromServer[];
    public morphisms!: SchemaMorphismFromServer[];
}
