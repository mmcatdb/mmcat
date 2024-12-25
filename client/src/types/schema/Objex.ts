import { idsAreEqual, Key, ObjexIds, SignatureId, type KeyFromServer, type ObjexIdsFromServer, type SignatureIdFromServer } from '../identifiers';
import { SchemaCategoryInvalidError } from './Error';

export class Objex {
    public readonly originalMetadata: MetadataObjex;

    private constructor(
        readonly key: Key,
        private _metadata: MetadataObjex,
    ) {
        this.originalMetadata = _metadata;
    }

    static fromServer(input: SchemaObjexFromServer, metadata: MetadataObjexFromServer): Objex {
        const output = new Objex(
            Key.fromServer(input.key),
            MetadataObjex.fromServer(metadata),
        );
        output.current = SchemaObjex.fromServer(input);

        return output;
    }

    static create(key: Key): Objex {
        return new Objex(
            key,
            MetadataObjex.createDefault(),
        );
    }

    private _current?: SchemaObjex;

    get current(): SchemaObjex | undefined {
        return this._current;
    }

    set current(value: SchemaObjex | undefined) {
        this._current = value;
    }

    get metadata(): MetadataObjex {
        return this._metadata;
    }

    set metadata(value: MetadataObjex) {
        this._metadata = value;
    }

    private readonly groupIds = new Set<string>();

    addGroup(id: string) {
        this.groupIds.add(id);
    }
}

export type SchemaObjexFromServer = {
    key: KeyFromServer;
    ids?: ObjexIdsFromServer;
    superId: SignatureIdFromServer;
};

export class SchemaObjex {
    private constructor(
        readonly key: Key,
        readonly ids: ObjexIds | undefined,
        readonly superId: SignatureId,
        private _isNew: boolean,
    ) {}

    static fromServer(schema: SchemaObjexFromServer): SchemaObjex {
        const objex = new SchemaObjex(
            Key.fromServer(schema.key),
            schema.ids ? ObjexIds.fromServer(schema.ids) : undefined,
            SignatureId.fromServer(schema.superId),
            false,
        );

        return objex;
    }

    static createNew(key: Key, def: Omit<ObjexDefinition, 'label'>): SchemaObjex {
        const objex = new SchemaObjex(
            key,
            def.ids,
            def.ids?.generateDefaultSuperId() ?? SignatureId.union([]),
            true,
        );

        return objex;
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

export class MetadataObjex {
    private constructor(
        readonly label: string,
        readonly position: Position,
    ) {}

    static fromServer(input: MetadataObjexFromServer): MetadataObjex {
        return new MetadataObjex(
            input.label,
            input.position,
        );
    }

    static createDefault(): MetadataObjex {
        return new MetadataObjex(
            '',
            { x: 0, y: 0 },
        );
    }

    static create(label: string, position: Position): MetadataObjex {
        return new MetadataObjex(
            label,
            position,
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
