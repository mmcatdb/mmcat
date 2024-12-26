import type { Category } from '../Category';
import { type MetadataObjex, Objex, SchemaObjex, type SchemaObjexFromServer } from '../Objex';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type CreateObjexFromServer = SMOFromServer<SMOType.CreateObjex> & {
    object: SchemaObjexFromServer;
};

export class CreateObjex implements SMO<SMOType.CreateObjex> {
    readonly type = SMOType.CreateObjex;

    constructor(
        readonly schema: SchemaObjex,
        readonly metadata: MetadataObjex,
    ) {}

    static fromServer(input: CreateObjexFromServer): CreateObjex {
        return new CreateObjex(
            SchemaObjex.fromServer(input.object),
            null, // FIXME
        );
    }

    toServer(): CreateObjexFromServer {
        return {
            type: SMOType.CreateObjex,
            object: this.schema.toServer(),
        };
    }

    up(category: Category): void {
        const key = this.schema.key;
        category.objexes.set(key, new Objex(key, this.schema, this.metadata));
    }

    down(category: Category): void {
        category.objexes.delete(this.schema.key);
    }
}
