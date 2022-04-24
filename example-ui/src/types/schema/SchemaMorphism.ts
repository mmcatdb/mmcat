import { Signature } from "../identifiers";

export enum Cardinality {
    Zero = 'ZERO',
    One = 'ONE',
    Star = 'STAR'
}

export class SchemaMorphism {
    public id!: number;
    public domId!: number;
    public codId!: number;
    public signature!: Signature;
    public min!: Cardinality.Zero | Cardinality.One;
    public max!: Cardinality.One | Cardinality.Star;

    public get isBase(): boolean {
        return this.signature.isBase;
    }

    private constructor() {}

    public static fromServer(input: SchemaMorphismFromServer): SchemaMorphism {
        const morphism = new SchemaMorphism();

        morphism.id = input.id;
        morphism.domId = input.domId;
        morphism.codId = input.codId;

        const parsedJson = JSON.parse(input.jsonValue);
        morphism.signature = Signature.fromJSON(parsedJson.signature);
        morphism.min = parsedJson.min;
        morphism.max = parsedJson.max;

        return morphism;
    }

    public static createNew(id: number, domId: number, codId: number): SchemaMorphism {
        // TODO
        const morphism = new SchemaMorphism();

        morphism.id = id;
        morphism.domId = domId;
        morphism.codId = codId;

        return morphism;
    }

    public static fromDual(id: number, dualMorphism: SchemaMorphism): SchemaMorphism {
        // TODO
        const morphism = new SchemaMorphism();

        morphism.id = id;
        morphism.domId = dualMorphism.codId;
        morphism.codId = dualMorphism.domId;

        return morphism;
    }
}

export class SchemaMorphismFromServer {
    public id!: number;
    public domId!: number;
    public codId!: number;
    public jsonValue!: string;
}
