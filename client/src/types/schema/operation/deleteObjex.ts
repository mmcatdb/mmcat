import type { SchemaCategory } from '../SchemaCategory';
import { SchemaObjex, type SchemaObjexFromServer } from '../Objex';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type DeleteObjexFromServer = SMOFromServer<SMOType.DeleteObjex> & {
    object: SchemaObjexFromServer;
};

export class DeleteObjex implements SMO<SMOType.DeleteObjex> {
    readonly type = SMOType.DeleteObjex;

    private constructor(
        readonly objex: SchemaObjex,
    ) {}

    static fromServer(input: DeleteObjexFromServer): DeleteObjex {
        return new DeleteObjex(
            SchemaObjex.fromServer(input.object),
        );
    }

    static create(objex: SchemaObjex): DeleteObjex {
        return new DeleteObjex(
            objex,
        );
    }

    toServer(): DeleteObjexFromServer {
        return {
            type: SMOType.DeleteObjex,
            object: this.objex.toServer(),
        };
    }

    up(category: SchemaCategory): void {
        category.getObjex(this.objex.key).current = undefined;
    }

    down(category: SchemaCategory): void {
        category.getObjex(this.objex.key).current = this.objex;
    }
}
