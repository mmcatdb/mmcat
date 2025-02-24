import type { Entity, Id, VersionId } from '../id';
import type { MMOFromServer } from '../evocat/metadata/mmo';
import { type SMO, type SMOFromServer, smoFromServer } from '../evocat/schema';

export type SchemaUpdateFromServer = {
    id: Id;
    categoryId: Id;
    prevVersion: VersionId;
    nextVersion: VersionId;
    schema: SMOFromServer[];
};

export class SchemaUpdate implements Entity {
    private constructor(
        readonly id: Id,
        readonly categoryId: Id,
        readonly prevVersion: VersionId,
        readonly nextVersion: VersionId,
        readonly schema: SMO[],
    ) {}

    static fromServer(input: SchemaUpdateFromServer): SchemaUpdate {
        return new SchemaUpdate(
            input.id,
            input.categoryId,
            input.prevVersion,
            input.nextVersion,
            input.schema.map(smoFromServer),
        );
    }
}

export type SchemaUpdateInit = {
    readonly prevVersion: VersionId;
    readonly schema: SMOFromServer[];
    readonly metadata: MMOFromServer[];
};
