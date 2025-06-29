import type { Entity, Id, VersionId } from '../id';
import type { MMOResponse } from '../evocat/metadata/mmo';
import { type SMO, type SMOResponse, smoFromResponse } from '../evocat/schema';

export type SchemaUpdateResponse = {
    id: Id;
    categoryId: Id;
    prevVersion: VersionId;
    nextVersion: VersionId;
    schema: SMOResponse[];
};

export class SchemaUpdate implements Entity {
    private constructor(
        readonly id: Id,
        readonly categoryId: Id,
        readonly prevVersion: VersionId,
        readonly nextVersion: VersionId,
        readonly schema: SMO[],
    ) {}

    static fromResponse(input: SchemaUpdateResponse): SchemaUpdate {
        return new SchemaUpdate(
            input.id,
            input.categoryId,
            input.prevVersion,
            input.nextVersion,
            input.schema.map(smoFromResponse),
        );
    }
}

export type SchemaUpdateInit = {
    readonly prevVersion: VersionId;
    readonly schema: SMOResponse[];
    readonly metadata: MMOResponse[];
};
