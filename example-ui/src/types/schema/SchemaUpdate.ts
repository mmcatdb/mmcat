import type { Entity, Id, VersionId } from "../id";
import { VersionedSMO, type VersionedSMOFromServer } from "./VersionedSMO";

export type SchemaUpdateFromServer = {
    id: Id;
    categoryId: Id;
    beforeVersion: VersionId;
    afterVersion: VersionId;
    operations: VersionedSMOFromServer[];
};

export class SchemaUpdate implements Entity {
    private constructor(
        readonly id: Id,
        readonly categoryId: Id,
        readonly beforeVersion: VersionId,
        readonly afterVersion: VersionId,
        readonly operations: VersionedSMO[],
    ) {}

    static fromServer(input: SchemaUpdateFromServer): SchemaUpdate {
        return new SchemaUpdate(
            input.id,
            input.categoryId,
            input.beforeVersion,
            input.afterVersion,
            input.operations.map(VersionedSMO.fromServer),
        );
    }
}

export type SchemaUpdateInit = {
    readonly beforeVersion: VersionId;
    readonly operations: VersionedSMOFromServer[];
};
