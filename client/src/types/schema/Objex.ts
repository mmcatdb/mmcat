import { Key, ObjexIds, type Signature, type KeyResponse, type ObjexIdsResponse } from '../identifiers';
import { ComparableMap } from '../utils/ComparableMap';
import { type Category } from './Category';
import { SchemaCategoryInvalidError } from './Error';
import { type Morphism } from './Morphism';

/**
 * An objex from the {@link Category}.
 * It contains references to neighboring objexes and morphisms so all graph algorithms should be implemented here.
 * It's mutable but it shouldn't be modified directly. Use {@link Evocat} and SMOs to change it.
 */
export class Objex {
    readonly key: Key;
    readonly originalMetadata: MetadataObjex;

    constructor(
        private readonly category: Category,
        public schema: SchemaObjex,
        public metadata: MetadataObjex,
    ) {
        this.key = schema.key;
        this.originalMetadata = metadata;
    }

    static fromResponse(category: Category, schema: SchemaObjexResponse, metadata: MetadataObjexResponse): Objex {
        return new Objex(
            category,
            SchemaObjex.fromResponse(schema),
            MetadataObjex.fromResponse(metadata),
        );
    }

    /**
     * There are two types of objexes - entites and properties.
     * Entities have outgoing morphisms, properties do not.
     * Entities have either signature identifier(s) or a generated identifier. Properties are identified by their value.
     */
    get isEntity(): boolean {
        return this.morphismsFrom.size > 0;
    }

    /** All base morphisms starting in this objex. Managed by {@link Morphism}. */
    readonly morphismsFrom = new ComparableMap<Signature, string, Morphism>(signature => signature.value);
    /** All base morphisms ending in this objex. Managed by {@link Morphism}. */
    readonly morphismsTo = new ComparableMap<Signature, string, Morphism>(signature => signature.value);

    get neighborMorphisms(): Morphism[] {
        return [ ...this.morphismsFrom.values(), ...this.morphismsTo.values() ];
    }

    equals(other: Objex): boolean {
        return this.key.equals(other.key);
    }
}

export type SchemaObjexResponse = {
    key: KeyResponse;
    ids: ObjexIdsResponse;
};

/**
 * An immutable, serializable version of all schema-related data from the {@link Objex}.
 */
export class SchemaObjex {
    private constructor(
        readonly key: Key,
        readonly ids: ObjexIds,
        private _isNew: boolean,
    ) {}

    static fromResponse(schema: SchemaObjexResponse): SchemaObjex {
        return new SchemaObjex(
            Key.fromResponse(schema.key),
            ObjexIds.fromResponse(schema.ids),
            false,
        );
    }

    static createNew(key: Key, def: Omit<ObjexDefinition, 'label' | 'position'>): SchemaObjex {
        return new SchemaObjex(
            key,
            def.ids,
            true,
        );
    }

    /** If there is nothing to update, undefined will be returned. */
    update({ ids }: { ids?: ObjexIds }): SchemaObjex | undefined {
        if (ids && !this.ids.equals(ids))
            return SchemaObjex.createNew(this.key, { ids });

        return undefined;
    }

    get isNew(): boolean {
        return this._isNew;
    }

    get idsChecked(): ObjexIds {
        if (!this.ids)
            throw new SchemaCategoryInvalidError(`Objex: ${this.key.toString()} doesn't have ids.`);

        return this.ids;
    }

    toServer(): SchemaObjexResponse {
        return {
            key: this.key.toServer(),
            ids: this.ids.toServer(),
        };
    }

    equals(other: SchemaObjex | null | undefined): boolean {
        return !!other && this.key.equals(other.key);
    }
}

export type ObjexDefinition = {
    label: string;
    position: Position;
    ids: ObjexIds;
};

export type MetadataObjexResponse = {
    key: KeyResponse;
    label: string;
    position: Position;
};

export type Position = {
    x: number;
    y: number;
};

export function isPositionEqual(a: Position, b: Position): boolean {
    return a.x === b.x && a.y === b.y;
}

/**
 * An immutable, serializable version of all metadata-related data from the {@link Objex}.
 */
export class MetadataObjex {
    constructor(
        readonly label: string,
        readonly position: Position,
    ) {}

    static fromResponse(input: MetadataObjexResponse): MetadataObjex {
        return new MetadataObjex(
            input.label,
            input.position,
        );
    }

    toServer(key: Key): MetadataObjexResponse {
        return {
            key: key.toServer(),
            label: this.label,
            position: this.position,
        };
    }
}
