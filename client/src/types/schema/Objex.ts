import { idsAreEqual, Key, ObjexIds, type KeyResponse, type ObjexIdsResponse } from '../identifiers';
import { type Category } from './Category';
import { SchemaCategoryInvalidError } from './Error';
import { type Morphism } from './Morphism';

/**
 * An objex from the {@link Category}.
 * It contains references to neighboring objexes and morphisms so all graph algorithms should be implemented here.
 * It's mutable but it shouldn't be modified directly. Use {@link Evocat} and SMOs to change it.
 */
export class Objex {
    public readonly key: Key;
    public readonly originalMetadata: MetadataObjex;

    constructor(
        private readonly category: Category,
        public schema: SchemaObjex,
        public metadata: MetadataObjex,
    ) {
        this.key = schema.key;
        this.originalMetadata = metadata;
    }

    static fromResponse(category: Category, schema: SchemaObjexResponse, metadata: MetadataObjexResponse): Objex {
        const schemaObjex = SchemaObjex.fromResponse(schema);

        return new Objex(
            category,
            schemaObjex,
            MetadataObjex.fromResponse(metadata),
        );
    }

    equals(other: Objex): boolean {
        return this.key.equals(other.key);
    }

    // TODO This should be probably optimized by keeping a list of neighbors.
    findNeighborMorphisms(): Morphism[] {
        return [ ...this.category.morphisms.values() ].filter(morphism => morphism.from.key.equals(this.key) || morphism.to.key.equals(this.key));
    }
}

export type SchemaObjexResponse = {
    key: KeyResponse;
    ids?: ObjexIdsResponse;
};

/**
 * An immutable, serializable version of all schema-related data from the {@link Objex}.
 */
export class SchemaObjex {
    private constructor(
        readonly key: Key,
        readonly ids: ObjexIds | undefined,
        private _isNew: boolean,
    ) {}

    static fromResponse(schema: SchemaObjexResponse): SchemaObjex {
        return new SchemaObjex(
            Key.fromResponse(schema.key),
            schema.ids ? ObjexIds.fromResponse(schema.ids) : undefined,
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
    update({ ids }: { ids?: ObjexIds | null }): SchemaObjex | undefined {
        if (ids === null && this.ids)
            return SchemaObjex.createNew(this.key, {});

        if (ids && !idsAreEqual(ids, this.ids))
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
            ids: this.ids?.toServer(),
        };
    }

    equals(other: SchemaObjex | null | undefined): boolean {
        return !!other && this.key.equals(other.key);
    }
}

export type ObjexDefinition = {
    label: string;
    position: Position;
    ids?: ObjexIds;
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
