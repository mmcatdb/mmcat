import type { Iri } from "@/types/integration/";
import { Key, Signature, type KeyFromServer, type SignatureFromServer } from "../identifiers";
import type { SchemaObject } from "./SchemaObject";
import type { Optional } from "@/utils/common";

export type SchemaMorphismFromServer = {
    signature: SignatureFromServer;
    label?: string;
    domKey: KeyFromServer;
    codKey: KeyFromServer;
    min: Min;
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
    label: string;
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

    get sortBaseValue(): number {
        const baseValue = this.signature.baseValue;
        return Math.abs(baseValue ? baseValue : 0);
    }

    private constructor(signature: Signature, domKey: Key, codKey: Key, min: Min, isNew: boolean, label: string, iri: Iri | undefined, pimIri: Iri | undefined, tags: Tag[]) {
        this.signature = signature;
        this.domKey = domKey;
        this.codKey = codKey;
        this.min = min;
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
            false,
            input.label || '',
            input.iri,
            input.pimIri,
            input.tags ? input.tags : [],
        );
    }

    static createNew(signature: Signature, def: MorphismDefinition): SchemaMorphism {
        const [ iri, pimIri ] = 'iri' in def
            ? [ def.iri, def.pimIri ]
            : [ undefined, undefined ];

        return new SchemaMorphism(
            signature,
            def.dom.key,
            def.cod.key,
            def.min,
            true,
            def.label ?? '',
            iri,
            pimIri,
            def.tags ?? [],
        );
    }

    update(domKey: Key, codKey: Key, min: Min, label: string) {
        this.domKey = domKey;
        this.codKey = codKey;
        this.min = min;
        this.label = label;
    }

    toServer(): SchemaMorphismFromServer {
        return {
            signature: this.signature.toServer(),
            domKey: this.domKey.toServer(),
            codKey: this.codKey.toServer(),
            min: this.min,
            label: this.label,
            iri: this.iri,
            pimIri: this.pimIri,
            tags: this.tags,
        };
    }

    equals(other: SchemaMorphism | null | undefined): boolean {
        return !!other && this.signature.equals(other.signature);
    }
}

export type MorphismDefinition = {
    dom: SchemaObject;
    cod: SchemaObject;
    min: Min;
    label?: string;
    tags?: Tag[];
} & Optional<{
    iri: Iri;
    pimIri: Iri;
}>;
