import type { Category } from '../Category';
import { SchemaObjex, type SchemaObjexFromServer } from '../Objex';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type UpdateObjexFromServer = SMOFromServer<SMOType.UpdateObjex> & {
    newObjex: SchemaObjexFromServer;
    oldObjex: SchemaObjexFromServer;
};

export class UpdateObjex implements SMO<SMOType.UpdateObjex> {
    readonly type = SMOType.UpdateObjex;

    private constructor(
        readonly newObjex: SchemaObjex,
        readonly oldObjex: SchemaObjex,
    ) {}

    static fromServer(input: UpdateObjexFromServer): UpdateObjex {
        return new UpdateObjex(
            SchemaObjex.fromServer(input.newObjex),
            SchemaObjex.fromServer(input.oldObjex),
        );
    }

    static create(newObjex: SchemaObjex, oldObjex: SchemaObjex): UpdateObjex {
        if (!newObjex.key.equals(oldObjex.key))
            throw new Error('Cannot edit objex\'s key.');

        return new UpdateObjex(
            newObjex,
            oldObjex,
        );
    }

    toServer(): UpdateObjexFromServer {
        return {
            type: SMOType.UpdateObjex,
            newObjex: this.newObjex.toServer(),
            oldObjex: this.oldObjex.toServer(),
        };
    }

    up(category: Category): void {
        category.getObjex(this.newObjex.key).current = this.newObjex;
    }

    down(category: Category): void {
        category.getObjex(this.oldObjex.key).current = this.oldObjex;
    }
}
