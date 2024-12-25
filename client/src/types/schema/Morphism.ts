import { Key, Signature, type KeyFromServer, type SignatureFromServer } from '../identifiers';
import { isArrayEqual } from '@/types/utils/common';

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
    private constructor(
        readonly label: string,
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

    static create(label: string): MetadataMorphism {
        return new MetadataMorphism(
            label,
        );
    }

    toServer(signature: Signature): MetadataMorphismFromServer {
        return {
            signature: signature.toServer(),
            label: this.label,
        };
    }
}

export class Morphism {
    public readonly originalMetadata: MetadataMorphism;

    private constructor(
        readonly signature: Signature,
        private _metadata: MetadataMorphism,
    ) {
        this.originalMetadata = _metadata;
    }

    static fromServer(input: SchemaMorphismFromServer, metadata: MetadataMorphismFromServer): Morphism {
        const output = new Morphism(
            Signature.fromServer(input.signature),
            MetadataMorphism.fromServer(metadata),
        );
        output.current = SchemaMorphism.fromServer(input);

        return output;
    }

    static create(signature: Signature): Morphism {
        return new Morphism(
            signature,
            MetadataMorphism.createDefault(),
        );
    }

    private _current?: SchemaMorphism;

    get current(): SchemaMorphism | undefined {
        return this._current;
    }

    set current(value: SchemaMorphism | undefined) {
        this._current = value;
    }

    get metadata(): MetadataMorphism {
        return this._metadata;
    }

    set metadata(value: MetadataMorphism) {
        this._metadata = value;
    }
}
