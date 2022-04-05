import type { Position } from "cytoscape";
import { ComparablePosition, PositionUpdateToServer } from "./Position";

export class SchemaObject {
    //public key: number | undefined;
    //public label: number | undefined;

    public id!: number;
    public label!: string;
    public jsonValue!: string;
    public position?: ComparablePosition;
    private originalPosition?: ComparablePosition;

    private constructor() {}

    public static fromServer(input: SchemaObjectFromServer): SchemaObject {
        const object = new SchemaObject();

        //object.key = input.key.value;
        //object.label = input.label;
        object.label = JSON.parse(input.jsonValue).label;
        object.id = input.id;
        object.jsonValue = input.jsonValue;
        if (input.position) {
            object.position = new ComparablePosition(input.position);
            object.originalPosition = new ComparablePosition(input.position);
        }

        return object;
    }

    public toPositionUpdateToServer(): PositionUpdateToServer | null {
        return this.position?.equals(this.originalPosition) ? null : new PositionUpdateToServer({ schemaObjectId: this.id, position: this.position });
    }
}

export class SchemaObjectFromServer {
    public id!: number;
    public jsonValue!: string;
    public position?: Position;
}
