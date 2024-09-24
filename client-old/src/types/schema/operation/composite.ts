import { smoFromServer } from '.';
import type { SchemaCategory } from '../SchemaCategory';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type CompositeFromServer = SMOFromServer<SMOType.Composite> & {
    name: string;
    children: SMOFromServer[];
};

export class Composite implements SMO<SMOType.Composite> {
    readonly type = SMOType.Composite;

    private constructor(
        readonly name: string,
        readonly children: SMO[],
    ) {}

    static fromServer(input: CompositeFromServer): Composite {
        return new Composite(
            input.name,
            input.children.map(smoFromServer),
        );
    }

    static create(name: string, children: SMO[]): Composite {
        return new Composite(
            name,
            children,
        );
    }

    toServer(): CompositeFromServer {
        return {
            type: SMOType.Composite,
            name: this.name,
            children: this.children.map(smo => smo.toServer()),
        };
    }

    up(category: SchemaCategory): void {
        this.children.forEach(child => child.up(category));
    }

    down(category: SchemaCategory): void {
        [ ...this.children ].reverse().forEach(child => child.down(category));
    }
}
