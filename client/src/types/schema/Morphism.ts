import { Key, Signature, type KeyFromServer, type SignatureFromServer } from '../identifiers';
import { isArrayEqual } from '@/types/utils/common';
import { type Objex } from './Objex';
import { type Category } from './Category';

export class Morphism {
    public readonly originalMetadata: MetadataMorphism;

    constructor(
        readonly signature: Signature,
        public schema: SchemaMorphism,
        public metadata: MetadataMorphism,
        public from: Objex,
        public to: Objex,
    ) {
        this.originalMetadata = metadata;
    }

    static fromServer(schema: SchemaMorphismFromServer, metadata: MetadataMorphismFromServer, category: Category): Morphism {
        const schemaMorphism = SchemaMorphism.fromServer(schema);

        return new Morphism(
            schemaMorphism.signature,
            schemaMorphism,
            MetadataMorphism.fromServer(metadata),
            category.getObjex(schemaMorphism.domKey),
            category.getObjex(schemaMorphism.codKey),
        );
    }
}

export type SchemaMorphismFromServer = {
    signature: SignatureFromServer;
    domKey: KeyFromServer;
    codKey: KeyFromServer;
    min: Min;
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
        readonly tags: Tag[],
        private _isNew: boolean,
    ) {}

    static fromServer(schema: SchemaMorphismFromServer): SchemaMorphism {
        return new SchemaMorphism(
            Signature.fromServer(schema.signature),
            Key.fromServer(schema.domKey),
            Key.fromServer(schema.codKey),
            schema.min,
            schema.tags ? schema.tags : [],
            false,
        );
    }

    static createNew(signature: Signature, def: MorphismDefinition): SchemaMorphism {
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
        const update: Partial<Omit<MorphismDefinition, 'label'>> = {};
        if (domKey && !this.domKey.equals(domKey))
            update.domKey = domKey;
        if (codKey && !this.codKey.equals(codKey))
            update.codKey = codKey;
        if (min && this.min !== min)
            update.min = min;
        if (tags && !isArrayEqual(this.tags, tags))
            update.tags = tags;

        if (Object.keys(update).length === 0)
            return;

        return SchemaMorphism.createNew(this.signature, {
            domKey: update.domKey ?? this.domKey,
            codKey: update.codKey ?? this.codKey,
            min: update.min ?? this.min,
            tags: update.tags ?? this.tags,
        });
    }

    toServer(): SchemaMorphismFromServer {
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
    domKey: Key;
    codKey: Key;
    min: Min;
    label?: string;
    tags?: Tag[];
};

export type MetadataMorphismFromServer = {
    signature: SignatureFromServer;
    label: string;
};

export class MetadataMorphism {
    constructor(
        readonly label: string | undefined,
    ) {}

    static fromServer(input: MetadataMorphismFromServer): MetadataMorphism {
        return new MetadataMorphism(
            input.label,
        );
    }

    static createDefault(): MetadataMorphism {
        return new MetadataMorphism(
            '',
        );
    }

    toServer(signature: Signature): MetadataMorphismFromServer {
        return {
            signature: signature.toServer(),
            label: this.label, // FIXME
        };
    }
}
