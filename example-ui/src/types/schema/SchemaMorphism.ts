import { Signature, type SignatureJSON } from "../identifiers";

export type SchemaMorphismJSON = {
    _class: 'SchemaMorphism',
    signature: SignatureJSON,
    min: Min,
    max: Max
}

export enum Cardinality {
    Zero = 'ZERO',
    One = 'ONE',
    Star = 'STAR'
}

export type Min = Cardinality.Zero | Cardinality.One;
export type Max = Cardinality.One | Cardinality.Star;

export class SchemaMorphism {
    public id!: number;
    public domId!: number;
    public codId!: number;
    public signature!: Signature;
    public min!: Min;
    public max!: Max;

    public get isBase(): boolean {
        return this.signature.isBase;
    }

    private constructor() {}

    public static fromServer(input: SchemaMorphismFromServer): SchemaMorphism {
        const morphism = new SchemaMorphism();

        morphism.id = input.id;
        morphism.domId = input.domId;
        morphism.codId = input.codId;

        const parsedJson = JSON.parse(input.jsonValue) as SchemaMorphismJSON;
        morphism.signature = Signature.fromJSON(parsedJson.signature);
        morphism.min = parsedJson.min;
        morphism.max = parsedJson.max;

        return morphism;
    }

    public static createNew(id: number, domId: number, codId: number, signature: Signature, min: Min, max: Max): SchemaMorphism {
        // TODO
        const morphism = new SchemaMorphism();

        morphism.id = id;
        morphism.domId = domId;
        morphism.codId = codId;
        morphism.signature = signature;
        morphism.min = min;
        morphism.max = max;

        return morphism;
    }

    public static fromDual(id: number, dualMorphism: SchemaMorphism, signature: Signature, min: Min, max: Max): SchemaMorphism {
        // TODO
        const morphism = new SchemaMorphism();

        morphism.id = id;
        morphism.domId = dualMorphism.codId;
        morphism.codId = dualMorphism.domId;
        morphism.signature = signature;
        morphism.min = min;
        morphism.max = max;

        return morphism;
    }

    public toJSON(): SchemaMorphismJSON {
        return {
            _class: 'SchemaMorphism',
            signature: this.signature.toJSON(),
            min: this.min,
            max: this.max
        };
    }
}

export class SchemaMorphismFromServer {
    public id!: number;
    public domId!: number;
    public codId!: number;
    public jsonValue!: string;
}
