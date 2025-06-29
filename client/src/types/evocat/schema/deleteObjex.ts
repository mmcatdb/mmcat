import { type Category, MetadataObjex, type MetadataObjexResponse, Objex, SchemaObjex, type SchemaObjexResponse } from '@/types/schema';
import { type SMO, type SMOResponse, SMOType } from './smo';

export type DeleteObjexResponse = SMOResponse<SMOType.DeleteObjex> & {
    schema: SchemaObjexResponse;
    metadata: MetadataObjexResponse;
};

export class DeleteObjex implements SMO<SMOType.DeleteObjex> {
    readonly type = SMOType.DeleteObjex;

    constructor(
        readonly schema: SchemaObjex,
        readonly metadata: MetadataObjex,
    ) {}

    static fromResponse(input: DeleteObjexResponse): DeleteObjex {
        return new DeleteObjex(
            SchemaObjex.fromResponse(input.schema),
            MetadataObjex.fromResponse(input.metadata),
        );
    }

    toServer(): DeleteObjexResponse {
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
