import { idsAreEqual, Key, ObjexIds, SignatureId, type KeyFromServer, type ObjexIdsFromServer, type SignatureIdFromServer } from '../identifiers';
import { type Category } from './Category';
import { SchemaCategoryInvalidError } from './Error';
import { type Morphism } from './Morphism';

/**
 * An object from the {@link Category}.
 * It contains references to neighbouring objects and morphisms so all graph algorithms should be implemented here.
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

    static fromServer(category: Category, schema: SchemaObjexFromServer, metadata: MetadataObjexFromServer): Objex {
        const schemaObjex = SchemaObjex.fromServer(schema);

        return new Objex(
            category,
            schemaObjex,
            MetadataObjex.fromServer(metadata),
        );
    }

    equals(other: Objex): boolean {
        return this.key.equals(other.key);
    }

    private readonly groupIds = new Set<string>();

    addGroup(id: string) {
        this.groupIds.add(id);
    }

    findNeighbourMorphisms(): Morphism[] {
        return [ ...this.category.morphisms.values() ].filter(morphism => morphism.from.key.equals(this.key) || morphism.to.key.equals(this.key));
    }
}

export type SchemaObjexFromServer = {
    key: KeyFromServer;
    ids?: ObjexIdsFromServer;
    superId: SignatureIdFromServer;
};

/**
 * An immutable, serializable version of all schema-related data from the {@link Objex}.
 */
export class SchemaObjex {
    private constructor(
        readonly key: Key,
        readonly ids: ObjexIds | undefined,
        readonly superId: SignatureId,
        private _isNew: boolean,
    ) {}

    static fromServer(schema: SchemaObjexFromServer): SchemaObjex {
        return new SchemaObjex(
            Key.fromServer(schema.key),
            schema.ids ? ObjexIds.fromServer(schema.ids) : undefined,
            SignatureId.fromServer(schema.superId),
            false,
        );
    }

    static createNew(key: Key, def: Omit<ObjexDefinition, 'label' | 'position'>): SchemaObjex {
        return new SchemaObjex(
            key,
            def.ids,
            def.ids?.generateDefaultSuperId() ?? SignatureId.union([]),
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
            throw new SchemaCategoryInvalidError(`Object: ${this.key.toString()} doesn't have ids.`);

        return this.ids;
    }

    toServer(): SchemaObjexFromServer {
        return {
            key: this.key.toServer(),
            ids: this.ids?.toServer(),
            superId: this.superId.toServer(),
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

export type MetadataObjexFromServer = {
    key: KeyFromServer;
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

    static fromServer(input: MetadataObjexFromServer): MetadataObjex {
        return new MetadataObjex(
            input.label,
            input.position,
        );
    }

    toServer(key: Key): MetadataObjexFromServer {
        return {
            key: key.toServer(),
            label: this.label,
            position: this.position,
        };
    }
}
