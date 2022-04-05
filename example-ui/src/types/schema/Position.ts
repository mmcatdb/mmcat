import type { Position } from "cytoscape";

export class ComparablePosition implements Position {
    public x!: number;
    public y!: number;

    public constructor(input: Position) {
        Object.assign(this, input);
    }

    public equals(object?: Position) : boolean {
        return !!object && this.x === object.x && this.y === object.y;
    }
}

export class PositionUpdateToServer {
    public schemaObjectId!: number;
    public position!: Position;

    public constructor(input?: Partial<PositionUpdateToServer>) {
        Object.assign(this, input);
    }
}
