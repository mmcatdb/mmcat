import type { Iri } from "@/types/integration/";
import type { Id } from "../id";
import { Key, Signature, type KeyJSON, type SignatureFromServer } from "../identifiers";

export type SchemaMorphismFromServer = {
    signature: SignatureFromServer;
    label?: string;
    domKey: KeyJSON;
    codKey: KeyJSON;
    min: Min;
    max: Max;
    iri?: Iri;
    pimIri?: Iri;
    tags?: Tag[];
};

export enum Cardinality {
    Zero = 'ZERO',
    One = 'ONE',
    Star = 'STAR'
}

export type Min = Cardinality.Zero | Cardinality.One;
export type Max = Cardinality.One | Cardinality.Star;

export enum Tag {
    Isa = 'isa',
    Role = 'role'
}

export class SchemaMorphism {
    signature: Signature;
    domKey: Key;
    codKey: Key;
    min: Min;
    max: Max;
    label: string;
    _dual!: SchemaMorphism;
    _isNew: boolean;

    iri?: Iri;
    pimIri?: Iri;
    tags: Tag[];

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

    private constructor(signature: Signature, domKey: Key, codKey: Key, min: Min, max: Max, isNew: boolean, label: string, iri: Iri | undefined, pimIri: Iri | undefined, tags: Tag[]) {
        this.signature = signature;
        this.domKey = domKey;
        this.codKey = codKey;
        this.min = min;
        this.max = max;
        this._isNew = isNew;
        this.label = label;
        this.iri = iri;
        this.pimIri = pimIri;
        this.tags = [ ...tags ];
    }

    static fromServer(input: SchemaMorphismFromServer): SchemaMorphism {
        return new SchemaMorphism(
            Signature.fromServer(input.signature),
            Key.fromServer(input.domKey),
            Key.fromServer(input.codKey),
            input.min,
            input.max,
            false,
            input.label || '',
            input.iri,
            input.pimIri,
            input.tags ? input.tags : []
        );
    }

    static createNew(signature: Signature, domKey: Key, codKey: Key, min: Min, max: Max, label: string, tags: Tag[]): SchemaMorphism {
        return new SchemaMorphism(signature, domKey, codKey, min, max, true, label, undefined, undefined, tags);
    }

    static createNewFromDualWithoutTags(dual: SchemaMorphism, signature: Signature, min: Min, max: Max): SchemaMorphism {
        return new SchemaMorphism(signature, dual.codKey, dual.domKey, min, max, true, '', '', undefined, []);
    }

    update(domKey: Key, codKey: Key, min: Min, max: Max, label: string) {
        this.domKey = domKey;
        this.codKey = codKey;
        this.min = min;
        this.max = max;
        this.label = label;
    }

    toJSON(): SchemaMorphismFromServer {
        return {
            signature: this.signature.toJSON(),
            domKey: this.domKey.toJSON(),
            codKey: this.codKey.toJSON(),
            min: this.min,
            max: this.max,
            label: this.label,
            iri: this.iri,
            pimIri: this.pimIri,
            tags: this.tags
        };
    }

    equals(other: SchemaMorphism | null | undefined): boolean {
        return !!other && this.signature.equals(other.signature);
    }
}

export type SchemaMorphismUpdate = {
    domId?: Id;
    codId?: Id;
    temporaryDomId?: Id;
    temporaryCodId?: Id;
    jsonValue: string;
};
