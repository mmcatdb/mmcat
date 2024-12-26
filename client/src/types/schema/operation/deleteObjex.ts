import type { Category } from '../Category';
import { type MetadataObjex, Objex, SchemaObjex, type SchemaObjexFromServer } from '../Objex';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type DeleteObjexFromServer = SMOFromServer<SMOType.DeleteObjex> & {
    object: SchemaObjexFromServer;
};

export class DeleteObjex implements SMO<SMOType.DeleteObjex> {
    readonly type = SMOType.DeleteObjex;

    constructor(
        readonly schema: SchemaObjex,
        readonly metadata: MetadataObjex,
    ) {}

    static fromServer(input: DeleteObjexFromServer): DeleteObjex {
        return new DeleteObjex(
            SchemaObjex.fromServer(input.object),
            null, // FIXME
        );
    }

    toServer(): DeleteObjexFromServer {
        return {
            type: SMOType.DeleteObjex,
            object: this.schema.toServer(),
        };
    }

    up(category: Category): void {
        category.objexes.delete(this.schema.key);
    }

    down(category: Category): void {
        category.objexes.set(this.schema.key, new Objex(category, this.schema, this.metadata));
    }
}
