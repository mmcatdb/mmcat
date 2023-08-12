import type { Position } from 'cytoscape';

export class ComparablePosition implements Position {
    private constructor(
        readonly x: number,
        readonly y: number,
    ) {}

    static fromPosition(input: Position) {
        return new ComparablePosition(input.x, input.y);
    }

    static createDefault() {
        return new ComparablePosition(0, 0);
    }

    copy() {
        return new ComparablePosition(this.x, this.y);
    }

    equals(object?: Position) : boolean {
        return !!object && this.x === object.x && this.y === object.y;
    }
}
