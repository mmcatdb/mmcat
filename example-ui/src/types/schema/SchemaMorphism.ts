import type { Iri } from "@/types/integration/";
import { Signature, type SignatureJSON } from "../identifiers";

export type SchemaMorphismJSON = {
    signature: SignatureJSON,
    min: Min,
    max: Max,
    label?: string,
    iri?: Iri
}

export enum Cardinality {
    Zero = 'ZERO',
    One = 'ONE',
    Star = 'STAR'
}

export type Min = Cardinality.Zero | Cardinality.One;
export type Max = Cardinality.One | Cardinality.Star;

export class SchemaMorphism {
    iri?: Iri;
    label: string;

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

    private constructor(id: number, domId: number, codId: number, signature: Signature, min: Min, max: Max, isNew: boolean, label: string, iri?: Iri) {
        this.id = id;
        this.domId = domId;
        this.codId = codId;
        this.signature = signature;
        this.min = min;
        this.max = max;
        this._isNew = isNew;
        this.label = label;
        this.iri = iri;
    }

    static fromServer(input: SchemaMorphismFromServer): SchemaMorphism {
        const parsedJson = JSON.parse(input.jsonValue) as SchemaMorphismJSON;

        return new SchemaMorphism(
            input.id,
            input.domId,
            input.codId,
            Signature.fromJSON(parsedJson.signature),
            parsedJson.min,
            parsedJson.max,
            false,
            parsedJson.label || '',
            parsedJson.iri
        );
    }

    static createNew(id: number, domId: number, codId: number, signature: Signature, min: Min, max: Max, label: string): SchemaMorphism {
        return new SchemaMorphism(id, domId, codId, signature, min, max, true, label);
    }

    static createNewFromDual(id: number, dual: SchemaMorphism, signature: Signature, min: Min, max: Max): SchemaMorphism {
        return new SchemaMorphism(id, dual.codId, dual.domId, signature, min, max, true, '');
    }

    update(domId: number, codId: number, min: Min, max: Max, label: string) {
        this.domId = domId;
        this.codId = codId;
        this.min = min;
        this.max = max;
        this.label = label;
    }

    toJSON(): SchemaMorphismJSON {
        return {
            signature: this.signature.toJSON(),
            min: this.min,
            max: this.max,
            label: this.label,
            iri: this.iri
        };
    }
}

export type SchemaMorphismUpdate = {
    domId?: number;
    codId?: number;
    temporaryDomId?: number;
    temporaryCodId?: number;
    jsonValue: string;
}

export class SchemaMorphismFromServer {
    id!: number;
    domId!: number;
    codId!: number;
    jsonValue!: string;
}
