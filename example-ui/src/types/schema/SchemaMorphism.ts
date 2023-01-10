import type { Iri } from "@/types/integration/";
import type { Id } from "../id";
import { Signature, type SignatureJSON } from "../identifiers";

export type SchemaMorphismJSON = {
    signature: SignatureJSON;
    min: Min;
    max: Max;
    label?: string;
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
    iri?: Iri;
    pimIri?: Iri;
    label: string;
    tags: Tag[];

    id: Id;
    domId: Id;
    codId: Id;
    signature: Signature;
    min: Min;
    max: Max;
    _dual!: SchemaMorphism;
    _isNew: boolean;


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

    private constructor(id: Id, domId: Id, codId: Id, signature: Signature, min: Min, max: Max, isNew: boolean, label: string, iri: Iri | undefined, pimIri: Iri | undefined, tags: Tag[]) {
        this.id = id;
        this.domId = domId;
        this.codId = codId;
        this.signature = signature;
        this.min = min;
        this.max = max;
        this._isNew = isNew;
        this.label = label;
        this.iri = iri;
        this.pimIri = pimIri;
        this.tags = [ ...tags ];
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
            parsedJson.iri,
            parsedJson.pimIri,
            parsedJson.tags ? parsedJson.tags : []
        );
    }

    static createNew(id: Id, domId: Id, codId: Id, signature: Signature, min: Min, max: Max, label: string, tags: Tag[]): SchemaMorphism {
        return new SchemaMorphism(id, domId, codId, signature, min, max, true, label, undefined, undefined, tags);
    }

    static createNewFromDualWithoutTags(id: Id, dual: SchemaMorphism, signature: Signature, min: Min, max: Max): SchemaMorphism {
        return new SchemaMorphism(id, dual.codId, dual.domId, signature, min, max, true, '', '', undefined, []);
    }

    update(domId: Id, codId: Id, min: Min, max: Max, label: string) {
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
            iri: this.iri,
            pimIri: this.pimIri,
            tags: this.tags
        };
    }
}

export type SchemaMorphismUpdate = {
    domId?: Id;
    codId?: Id;
    temporaryDomId?: Id;
    temporaryCodId?: Id;
    jsonValue: string;
};

export type SchemaMorphismFromServer = {
    id: Id;
    domId: Id;
    codId: Id;
    jsonValue: string;
};
