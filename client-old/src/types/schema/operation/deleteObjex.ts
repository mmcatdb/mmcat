import type { Category } from '../Category';
import { MetadataObjex, type MetadataObjexFromServer, SchemaObjex, type SchemaObjexFromServer } from '../Objex';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type DeleteObjexFromServer = SMOFromServer<SMOType.DeleteObjex> & {
    schema: SchemaObjexFromServer;
    metadata: MetadataObjexFromServer;
};

export class DeleteObjex implements SMO<SMOType.DeleteObjex> {
    readonly type = SMOType.DeleteObjex;

    constructor(
        readonly schema: SchemaObjex,
        readonly metadata: MetadataObjex,
    ) {}

    static fromServer(input: DeleteObjexFromServer): DeleteObjex {
        return new DeleteObjex(
            SchemaObjex.fromServer(input.schema),
            MetadataObjex.fromServer(input.metadata),
        );
    }

    toServer(): DeleteObjexFromServer {
        return {
            type: SMOType.DeleteObjex,
            schema: this.schema.toServer(),
            metadata: this.metadata.toServer(this.schema.key),
        };
    }

    up(category: Category): void {
        category.getObjex(this.schema.key).current = undefined;
    }

    down(category: Category): void {
        category.getObjex(this.schema.key).current = this.schema;
    }
}
