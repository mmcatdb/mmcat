import { type Category, MetadataObjex, type MetadataObjexFromServer, Objex, SchemaObjex, type SchemaObjexFromServer } from '@/types/schema';
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
        category.objexes.delete(this.schema.key);
    }

    down(category: Category): void {
        category.objexes.set(this.schema.key, new Objex(category, this.schema, this.metadata));
    }
}
