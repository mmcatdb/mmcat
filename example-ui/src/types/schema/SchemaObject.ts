import type { Position } from "cytoscape";
import { Key, SchemaId, type KeyJSON, type SchemaIdJSON } from "../identifiers";
import { ComparablePosition, PositionUpdateToServer } from "./Position";

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

    schemaIds!: SchemaId[];
    superId!: SchemaId;
    label!: string;
    _jsonValue!: string;
    position?: ComparablePosition;
    _originalPosition?: ComparablePosition;
    _isNew!: boolean;
    databases!: string[];

    key!: Key;

    private constructor() {}

    static fromServer(input: SchemaObjectFromServer): SchemaObject {
        const object = new SchemaObject();

        //object.key = input.key.value;
        //object.label = input.label;
        const jsonObject = JSON.parse(input.jsonValue) as SchemaObjectJSON;
        object.key = Key.fromServer(jsonObject.key);
        object.label = jsonObject.label;
        object.id = input.id;
        object.schemaIds = jsonObject.ids.map((schemaId: SchemaIdJSON) => SchemaId.fromJSON(schemaId));
        object.superId = SchemaId.fromJSON(jsonObject.superId);
        object._jsonValue = input.jsonValue;
        object._isNew = false;
        object.databases = jsonObject.databases || [];
        if (input.position) { // This should be mandatory since all objects should have defined position.
            object.position = new ComparablePosition(input.position);
            object._originalPosition = new ComparablePosition(input.position);
        }

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
        object.databases = [];

        return object;
    }

    addSchemaId(id: SchemaId): void {
        this.schemaIds.push(id);
        this.superId = SchemaId.union([ this.superId, id ]);
    }

    get canBeSimpleProperty(): boolean {
        if (this.schemaIds.length < 1)
            return false;

        for (const id of this.schemaIds) {
            if (id.signatures.length < 2)
                return true;
        }

        return false;
    }

    get isNew(): boolean {
        return this._isNew;
    }

    setLabel(label: string) {
        this.label = label;
    }

    toPositionUpdateToServer(): PositionUpdateToServer | null {
        return this.position?.equals(this._originalPosition) ? null : new PositionUpdateToServer({ schemaObjectId: this.id, position: this.position });
    }

    toJSON(): SchemaObjectJSON {
        return {
            label: this.label,
            key: this.key.toJSON(),
            ids: this.schemaIds.map(id => id.toJSON()),
            superId: this.superId.toJSON(),
            databases: this.databases
        };
    }
}

export type SchemaObjectFromServer = {
    id: number;
    jsonValue: string;
    position?: Position;
}
