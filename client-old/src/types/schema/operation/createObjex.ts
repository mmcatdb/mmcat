import type { Category } from '../Category';
import { MetadataObjex, type MetadataObjexFromServer, SchemaObjex, type SchemaObjexFromServer } from '../Objex';
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
        category.getObjex(this.schema.key).current = this.schema;
    }

    down(category: Category): void {
        category.getObjex(this.schema.key).current = undefined;
    }
}
