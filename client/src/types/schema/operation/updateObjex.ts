import type { Category } from '../Category';
import { SchemaObjex, type SchemaObjexFromServer } from '../Objex';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type UpdateObjexFromServer = SMOFromServer<SMOType.UpdateObjex> & {
    newObject: SchemaObjexFromServer;
    oldObject: SchemaObjexFromServer;
};

export class UpdateObjex implements SMO<SMOType.UpdateObjex> {
    readonly type = SMOType.UpdateObjex;

    private constructor(
        readonly newObjex: SchemaObjex,
        readonly oldObjex: SchemaObjex,
    ) {}

    static fromServer(input: UpdateObjexFromServer): UpdateObjex {
        return new UpdateObjex(
            SchemaObjex.fromServer(input.newObject),
            SchemaObjex.fromServer(input.oldObject),
        );
    }

    static create(newObjex: SchemaObjex, oldObjex: SchemaObjex): UpdateObjex {
        if (!newObjex.key.equals(oldObjex.key))
            throw new Error('Cannot edit object\'s key.');

        return new UpdateObjex(
            newObjex,
            oldObjex,
        );
    }

    toServer(): UpdateObjexFromServer {
        return {
            type: SMOType.UpdateObjex,
            newObject: this.newObjex.toServer(),
            oldObject: this.oldObjex.toServer(),
        };
    }

    up(category: Category): void {
        category.getObjex(this.newObjex.key).schema = this.newObjex;
    }

    down(category: Category): void {
        category.getObjex(this.oldObjex.key).schema = this.oldObjex;
    }
}
