import { type Category, MetadataObjex, type MetadataObjexResponse, Objex, SchemaObjex, type SchemaObjexResponse } from '@/types/schema';
import { type SMO, type SMOResponse, SMOType } from './smo';

export type CreateObjexResponse = SMOResponse<SMOType.CreateObjex> & {
    schema: SchemaObjexResponse;
    metadata: MetadataObjexResponse;
};

export class CreateObjex implements SMO<SMOType.CreateObjex> {
    readonly type = SMOType.CreateObjex;

    constructor(
        readonly schema: SchemaObjex,
        readonly metadata: MetadataObjex,
    ) {}

    static fromResponse(input: CreateObjexResponse): CreateObjex {
        return new CreateObjex(
            SchemaObjex.fromResponse(input.schema),
            MetadataObjex.fromResponse(input.metadata),
        );
    }

    toServer(): CreateObjexResponse {
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
