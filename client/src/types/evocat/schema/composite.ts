import { smoFromResponse } from '.';
import type { Category } from '@/types/schema';
import { type SMO, type SMOResponse, SMOType } from './smo';

export type CompositeResponse = SMOResponse<SMOType.Composite> & {
    name: string;
    children: SMOResponse[];
};

export class Composite implements SMO<SMOType.Composite> {
    readonly type = SMOType.Composite;

    constructor(
        readonly name: string,
        readonly children: SMO[],
    ) {}

    static fromResponse(input: CompositeResponse): Composite {
        return new Composite(
            input.name,
            input.children.map(smoFromResponse),
        );
    }

    toServer(): CompositeResponse {
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
