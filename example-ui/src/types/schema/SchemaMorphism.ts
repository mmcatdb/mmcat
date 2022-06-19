import { Signature, type SignatureJSON } from "../identifiers";

export type SchemaMorphismJSON = {
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
    id!: number;
    domId!: number;
    codId!: number;
    signature!: Signature;
    min!: Min;
    max!: Max;
    _dual!: SchemaMorphism;
    _isNew!: boolean;


    get isBase(): boolean {
        return this.signature.isBase;
    }

    get isNew(): boolean {
        return this._isNew;
    }

    get dual(): SchemaMorphism {
        return this._dual;
    }

    set dual(value: SchemaMorphism) {
        this._dual = value;
    }

    get sortBaseValue(): number {
        const baseValue = this.signature.baseValue;
        return Math.abs(baseValue ? baseValue : 0);
    }

    private constructor() {}

    static fromServer(input: SchemaMorphismFromServer): SchemaMorphism {
        const morphism = new SchemaMorphism();

        morphism.id = input.id;
        morphism.domId = input.domId;
        morphism.codId = input.codId;

        const parsedJson = JSON.parse(input.jsonValue) as SchemaMorphismJSON;
        morphism.signature = Signature.fromJSON(parsedJson.signature);
        morphism.min = parsedJson.min;
        morphism.max = parsedJson.max;
        morphism._isNew = false;

        return morphism;
    }

    static createNew(id: number, domId: number, codId: number, signature: Signature, min: Min, max: Max): SchemaMorphism {
        // TODO
        const morphism = new SchemaMorphism();

        morphism.id = id;
        morphism.domId = domId;
        morphism.codId = codId;
        morphism.signature = signature;
        morphism.min = min;
        morphism.max = max;
        morphism._isNew = true;

        return morphism;
    }

    static createNewFromDual(id: number, dualMorphism: SchemaMorphism, signature: Signature, min: Min, max: Max): SchemaMorphism {
        // TODO
        const morphism = new SchemaMorphism();

        morphism.id = id;
        morphism.domId = dualMorphism.codId;
        morphism.codId = dualMorphism.domId;
        morphism.signature = signature;
        morphism.min = min;
        morphism.max = max;
        morphism._isNew = true;

        return morphism;
    }

    update(domId: number, codId: number, min: Min, max: Max) {
        this.domId = domId;
        this.codId = codId;
        this.min = min;
        this.max = max;
    }

    toJSON(): SchemaMorphismJSON {
        return {
            signature: this.signature.toJSON(),
            min: this.min,
            max: this.max
        };
    }
}

export class SchemaMorphismFromServer {
    id!: number;
    domId!: number;
    codId!: number;
    jsonValue!: string;
}
