import { Key, Signature, type KeyResponse, type SignatureResponse } from '../identifiers';
import { isArrayEqual } from '@/types/utils/common';
import { type Objex } from './Objex';
import { type Category } from './Category';

/**
 * A morphism from the {@link Category}.
 * It contains references to neighboring objexes and morphisms so all graph algorithms should be implemented here.
 * It's mutable but it shouldn't be modified directly. Use {@link Evocat} and SMOs to change it.
 */
export class Morphism {
    readonly signature: Signature;
    readonly originalMetadata: MetadataMorphism;

    constructor(
        readonly category: Category,
        private _schema: SchemaMorphism,
        public metadata: MetadataMorphism,
    ) {
        this.signature = _schema.signature;
        this.originalMetadata = metadata;

        this.dom = this.category.getObjex(_schema.domKey);
        this.cod = this.category.getObjex(_schema.codKey);
    }

    static fromResponse(category: Category, schema: SchemaMorphismResponse, metadata: MetadataMorphismResponse): Morphism {
        return new Morphism(
            category,
            SchemaMorphism.fromResponse(schema),
            MetadataMorphism.fromResponse(metadata),
        );
    }

    get schema(): SchemaMorphism {
        return this._schema;
    }

    set schema(schema: SchemaMorphism) {
        if (schema.domKey !== this._schema.domKey)
            this.dom = this.category.getObjex(schema.domKey);
        if (schema.codKey !== this._schema.codKey)
            this.cod = this.category.getObjex(schema.codKey);

        this._schema = schema;
    }

    private _dom: Objex | undefined;

    get dom(): Objex {
        return this._dom!;
    }

    private set dom(objex: Objex | undefined) {
        this._dom?.morphismsFrom.delete(this.signature);
        this._dom = objex;
        this._dom?.morphismsFrom.set(this.signature, this);
    }

    private _cod: Objex | undefined;

    get cod(): Objex {
        return this._cod!;
    }

    private set cod(objex: Objex | undefined) {
        this._cod?.morphismsTo.delete(this.signature);
        this._cod = objex;
        this._cod?.morphismsTo.set(this.signature, this);
    }

    delete(): void {
        this.dom = undefined;
        this.cod = undefined;
    }

    equals(other: Morphism): boolean {
        return this.signature.equals(other.signature);
    }
}

export type SchemaMorphismResponse = {
    signature: SignatureResponse;
    domKey: KeyResponse;
    codKey: KeyResponse;
    min: Min;
    tags?: Tag[];
};

export enum Cardinality {
    Zero = 'ZERO',
    One = 'ONE',
    Star = 'STAR',
}

export type Min = Cardinality.Zero | Cardinality.One;
export type Max = Cardinality.One | Cardinality.Star;

export enum Tag {
    Isa = 'isa',
    Role = 'role',
}

/**
 * An immutable, serializable version of all schema-related data from the {@link Morphism}.
 */
export class SchemaMorphism {
    private constructor(
        readonly signature: Signature,
        readonly domKey: Key,
        readonly codKey: Key,
        readonly min: Min,
        readonly tags: Tag[],
        private _isNew: boolean,
    ) {}

    static fromResponse(schema: SchemaMorphismResponse): SchemaMorphism {
        return new SchemaMorphism(
            Signature.fromResponse(schema.signature),
            Key.fromResponse(schema.domKey),
            Key.fromResponse(schema.codKey),
            schema.min,
            schema.tags ? schema.tags : [],
            false,
        );
    }

    static createNew(signature: Signature, def: Omit<MorphismDefinition, 'label'>): SchemaMorphism {
        return new SchemaMorphism(
            signature,
            def.domKey,
            def.codKey,
            def.min,
            def.tags ?? [],
            true,
        );
    }

    /** If there is nothing to update, undefined will be returned. */
    update({ domKey, codKey, min, tags }: Partial<Omit<MorphismDefinition, 'label'>>): SchemaMorphism | undefined {
        const edit: Partial<Omit<MorphismDefinition, 'label'>> = {};
        if (domKey && !this.domKey.equals(domKey))
            edit.domKey = domKey;
        if (codKey && !this.codKey.equals(codKey))
            edit.codKey = codKey;
        if (min && this.min !== min)
            edit.min = min;
        if (tags && !isArrayEqual(this.tags, tags))
            edit.tags = tags;

        if (Object.keys(edit).length === 0)
            return;

        return SchemaMorphism.createNew(this.signature, {
            domKey: edit.domKey ?? this.domKey,
            codKey: edit.codKey ?? this.codKey,
            min: edit.min ?? this.min,
            tags: edit.tags ?? this.tags,
        });
    }

    toServer(): SchemaMorphismResponse {
        return {
            signature: this.signature.toServer(),
            domKey: this.domKey.toServer(),
            codKey: this.codKey.toServer(),
            min: this.min,
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
        return Math.abs(baseValue ?? 0);
    }

    equals(other: SchemaMorphism | null | undefined): boolean {
        return !!other && this.signature.equals(other.signature);
    }
}

export type MorphismDefinition = {
    domKey: Key;   // domain (source) objex
    codKey: Key;   // codomain (target) objex
    min: Min;
    label: string;
    tags?: Tag[];
};

export type MetadataMorphismResponse = {
    signature: SignatureResponse;
    label: string;
};

/**
 * An immutable, serializable version of all metadata-related data from the {@link Morphism}.
 */
export class MetadataMorphism {
    constructor(
        readonly label: string,
    ) {}

    static fromResponse(input: MetadataMorphismResponse): MetadataMorphism {
        return new MetadataMorphism(
            input.label,
        );
    }

    toServer(signature: Signature): MetadataMorphismResponse {
        return {
            signature: signature.toServer(),
            label: this.label,
        };
    }
}
