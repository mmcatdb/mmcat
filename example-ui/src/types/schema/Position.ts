import type { Position } from "cytoscape";
import type { Id } from "../id";

export class ComparablePosition implements Position {
    x!: number;
    y!: number;

    constructor(input: Position) {
        Object.assign(this, input);
    }

    equals(object?: Position) : boolean {
        return !!object && this.x === object.x && this.y === object.y;
    }
}

export type PositionUpdate = {
    schemaObjectId: Id;
    position: Position;
}
