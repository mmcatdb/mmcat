import { smoFromServer } from '.';
import type { Category } from '../Category';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type CompositeFromServer = SMOFromServer<SMOType.Composite> & {
    name: string;
    children: SMOFromServer[];
};

export class Composite implements SMO<SMOType.Composite> {
    readonly type = SMOType.Composite;

    constructor(
        readonly name: string,
        readonly children: SMO[],
    ) {}

    static fromServer(input: CompositeFromServer): Composite {
        return new Composite(
            input.name,
            input.children.map(smoFromServer),
        );
    }

    toServer(): CompositeFromServer {
        return {
            type: SMOType.Composite,
            name: this.name,
            children: this.children.map(smo => smo.toServer()),
        };
    }

    up(category: Category): void {
        this.children.forEach(child => child.up(category));
    }

    down(category: Category): void {
        [ ...this.children ].reverse().forEach(child => child.down(category));
    }
}
