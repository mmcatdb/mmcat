import { ComparableSet } from "@/utils/ComparableSet";
import type { Position } from "cytoscape";
import type { DatabaseWithConfiguration } from "../database";
import { Key, SchemaId, type KeyJSON, type SchemaIdJSON } from "../identifiers";
import { ComparablePosition, type PositionUpdate } from "./Position";

export type SchemaObjectJSON = {
    label: string,
    key: KeyJSON,
    ids: SchemaIdJSON[],
    superId: SchemaIdJSON,
    databases?: string[]
}

export class SchemaObject {
    //key: number | undefined;
    //label: number | undefined;

    id!: number;
    label!: string;
    key!: Key;
    schemaIds!: SchemaId[];
    superId!: SchemaId;
    position!: ComparablePosition;
    _isNew!: boolean;

    _originalPosition?: ComparablePosition;

    _databases = new ComparableSet<DatabaseWithConfiguration, number>(database => database.id);

    private constructor() {}

    static fromServer(input: SchemaObjectFromServer): SchemaObject {
        const object = new SchemaObject();

        const jsonObject = JSON.parse(input.jsonValue) as SchemaObjectJSON;
        object.id = input.id;
        object.label = jsonObject.label;
        object.key = Key.fromServer(jsonObject.key);
        object.schemaIds = jsonObject.ids.map(SchemaId.fromJSON);
        object.superId = SchemaId.fromJSON(jsonObject.superId);
        object._isNew = false;
        object.position = new ComparablePosition(input.position);
        object._originalPosition = new ComparablePosition(input.position);

        return object;
    }

    static createNew(id: number, label: string, key: Key, schemaIds: SchemaId[]): SchemaObject {
        const object = new SchemaObject();

        object.id = id;
        object.label = label;
        object.key = key;
        object.schemaIds = schemaIds;
        object.superId = SchemaId.union(schemaIds);

        object.position = new ComparablePosition({ x: 0, y: 0});
        object._isNew = true;

        return object;
    }

    addSchemaId(id: SchemaId): void {
        this.schemaIds.push(id);
        this.superId = SchemaId.union([ this.superId, id ]);
    }

    get canBeSimpleProperty(): boolean {
        if (this.schemaIds.length < 1)
            return true; // This shouldn't happen since all properties should have at least one identifier

        for (const id of this.schemaIds) {
            if (id.signatures.length < 2)
                return true;
        }

        return false;
    }

    get isNew(): boolean {
        return this._isNew;
    }

    get databases(): DatabaseWithConfiguration[] {
        return [ ...this._databases.values() ];
    }

    setDatabase(database: DatabaseWithConfiguration) {
        this._databases.add(database);
    }

    setLabel(label: string) {
        this.label = label;
    }

    toPositionUpdate(): PositionUpdate | null {
        return this.position.equals(this._originalPosition) ? null : { schemaObjectId: this.id, position: this.position };
    }

    toJSON(): SchemaObjectJSON {
        return {
            label: this.label,
            key: this.key.toJSON(),
            ids: this.schemaIds.map(id => id.toJSON()),
            superId: this.superId.toJSON(),
        };
    }
}

export type SchemaObjectUpdate = {
    temporaryId: number;
    position: Position;
    jsonValue: string;
}

export type SchemaObjectFromServer = {
    id: number;
    jsonValue: string;
    position: Position;
}
