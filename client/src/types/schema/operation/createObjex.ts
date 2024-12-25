import type { SchemaCategory } from '../SchemaCategory';
import { SchemaObjex, type SchemaObjexFromServer } from '../Objex';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type CreateObjexFromServer = SMOFromServer<SMOType.CreateObjex> & {
    object: SchemaObjexFromServer;
};

export class CreateObjex implements SMO<SMOType.CreateObjex> {
    readonly type = SMOType.CreateObjex;

    private constructor(
        readonly objex: SchemaObjex,
    ) {}

    static fromServer(input: CreateObjexFromServer): CreateObjex {
        return new CreateObjex(
            SchemaObjex.fromServer(input.object),
        );
    }

    static create(objex: SchemaObjex): CreateObjex {
        return new CreateObjex(
            objex,
        );
    }

    toServer(): CreateObjexFromServer {
        return {
            type: SMOType.CreateObjex,
            object: this.objex.toServer(),
        };
    }

    up(category: SchemaCategory): void {
        category.getObjex(this.objex.key).current = this.objex;
    }

    down(category: SchemaCategory): void {
        category.getObjex(this.objex.key).current = undefined;
    }
}
