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
    public readonly signature: Signature;
    public readonly originalMetadata: MetadataMorphism;

    constructor(
        readonly category: Category,
        public schema: SchemaMorphism,
        public metadata: MetadataMorphism,
        public from: Objex,
        public to: Objex,
    ) {
        this.signature = schema.signature;
        this.originalMetadata = metadata;
    }

    static fromResponse(category: Category, schema: SchemaMorphismResponse, metadata: MetadataMorphismResponse): Morphism {
        const schemaMorphism = SchemaMorphism.fromResponse(schema);

        return new Morphism(
            category,
            schemaMorphism,
            MetadataMorphism.fromResponse(metadata),
            category.getObjex(schemaMorphism.domKey),
            category.getObjex(schemaMorphism.codKey),
        );
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
