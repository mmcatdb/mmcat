import type { Position } from 'cytoscape';
import type { Entity, Id, VersionId } from '../id';
import type { KeyFromServer } from '../identifiers';
import { VersionedSMO, type VersionedSMOFromServer } from './VersionedSMO';

export type SchemaUpdateFromServer = {
    id: Id;
    categoryId: Id;
    prevVersion: VersionId;
    nextVersion: VersionId;
    operations: VersionedSMOFromServer[];
};

export class SchemaUpdate implements Entity {
    private constructor(
        readonly id: Id,
        readonly categoryId: Id,
        readonly prevVersion: VersionId,
        readonly nextVersion: VersionId,
        readonly operations: VersionedSMO[],
    ) {}

    static fromServer(input: SchemaUpdateFromServer): SchemaUpdate {
        return new SchemaUpdate(
            input.id,
            input.categoryId,
            input.prevVersion,
            input.nextVersion,
            input.operations.map(VersionedSMO.fromServer),
        );
    }
}

export type MetadataUpdate = {
    key: KeyFromServer;
    position: Position;
};

export type SchemaUpdateInit = {
    readonly prevVersion: VersionId;
    readonly operations: VersionedSMOFromServer[];
    readonly metadata: MetadataUpdate[];
};
