import { type Category, MetadataObjex, type MetadataObjexFromServer, Objex, SchemaObjex, type SchemaObjexFromServer } from '@/types/schema';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type CreateObjexFromServer = SMOFromServer<SMOType.CreateObjex> & {
    schema: SchemaObjexFromServer;
    metadata: MetadataObjexFromServer;
};

export class CreateObjex implements SMO<SMOType.CreateObjex> {
    readonly type = SMOType.CreateObjex;

    constructor(
        readonly schema: SchemaObjex,
        readonly metadata: MetadataObjex,
    ) {}

    static fromServer(input: CreateObjexFromServer): CreateObjex {
        return new CreateObjex(
            SchemaObjex.fromServer(input.schema),
            MetadataObjex.fromServer(input.metadata),
        );
    }

    toServer(): CreateObjexFromServer {
        return {
            type: SMOType.CreateObjex,
            schema: this.schema.toServer(),
            metadata: this.metadata.toServer(this.schema.key),
        };
    }

    up(category: Category): void {
        category.objexes.set(this.schema.key, new Objex(category, this.schema, this.metadata));
    }

    down(category: Category): void {
        category.objexes.delete(this.schema.key);
    }
}
