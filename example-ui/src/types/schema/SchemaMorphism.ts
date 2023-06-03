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
    private constructor(
        readonly signature: Signature,
        readonly domKey: Key,
        readonly codKey: Key,
        readonly min: Min,
        readonly label: string,
        readonly iri: Iri | undefined,
        readonly pimIri: Iri | undefined,
        readonly tags: Tag[],
        private _isNew: boolean,
    ) {}

    static fromServer(input: SchemaMorphismFromServer): SchemaMorphism {
        return new SchemaMorphism(
            Signature.fromServer(input.signature),
            Key.fromServer(input.domKey),
            Key.fromServer(input.codKey),
            input.min,
            input.label ?? '',
            input.iri,
            input.pimIri,
            input.tags ? input.tags : [],
            false,
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
            def.label ?? '',
            iri,
            pimIri,
            def.tags ?? [],
            true,
        );
    }

    createCopy(def: MorphismDefinition): SchemaMorphism {
        return SchemaMorphism.createNew(this.signature, def);
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

    get isNew(): boolean {
        return this._isNew;
    }

    get isBase(): boolean {
        return this.signature.isBase;
    }

    get sortBaseValue(): number {
        const baseValue = this.signature.baseValue;
        return Math.abs(baseValue ? baseValue : 0);
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
