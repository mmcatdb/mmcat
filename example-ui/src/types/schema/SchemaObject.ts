import type { Position } from "cytoscape";
import { Key, SchemaId, type SchemaIdJSON } from "../identifiers";
import { ComparablePosition, PositionUpdateToServer } from "./Position";

export class SchemaObject {
    //key: number | undefined;
    //label: number | undefined;

    id!: number;

    schemaIds!: SchemaId[];
    label!: string;
    jsonValue!: string;
    position?: ComparablePosition;
    _originalPosition?: ComparablePosition;

    key!: Key;

    private constructor() {}

    static fromServer(input: SchemaObjectFromServer): SchemaObject {
        const object = new SchemaObject();

        //object.key = input.key.value;
        //object.label = input.label;
        const jsonObject = JSON.parse(input.jsonValue);
        object.key = Key.fromServer(jsonObject.key);
        object.label = jsonObject.label;
        object.id = input.id;
        object.schemaIds = jsonObject.ids.map((schemaId: SchemaIdJSON) => SchemaId.fromJSON(schemaId));
        object.jsonValue = input.jsonValue;
        if (input.position) {
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

        return object;
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

    toPositionUpdateToServer(): PositionUpdateToServer | null {
        return this.position?.equals(this._originalPosition) ? null : new PositionUpdateToServer({ schemaObjectId: this.id, position: this.position });
    }
}

export class SchemaObjectFromServer {
    id!: number;
    jsonValue!: string;
    position?: Position;
}
