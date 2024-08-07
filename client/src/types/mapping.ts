import { RootProperty } from '@/types/accessPath/basic';
import type { RootPropertyFromServer } from './accessPath/serverTypes';
import type { Entity, Id, VersionId } from './id';
import { Key, SignatureId, type KeyFromServer, type SignatureIdFromServer } from './identifiers';

export type MappingFromServer = {
    id: Id;
    logicalModelId: Id;
    rootObjectKey: KeyFromServer;
    primaryKey: SignatureIdFromServer;
    kindName: string;
    accessPath: RootPropertyFromServer;
    version: VersionId;
    categoryVersionn: VersionId;
};

export class Mapping implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly kindName: string,
        public readonly logicalModelId: Id,
        //public readonly rootObject: SchemaObject,
        public readonly rootObjectKey: Key,
        public readonly primaryKey: SignatureId,
        public readonly accessPath: RootProperty,
        public readonly version: VersionId,
        public readonly categoryVersionnId: VersionId,
    ) {}

    static fromServer(input: MappingFromServer): Mapping {
        return new Mapping(
            input.id,
            input.kindName,
            input.logicalModelId,
            Key.fromServer(input.rootObjectKey),
            SignatureId.fromServer(input.primaryKey),
            RootProperty.fromServer(input.accessPath),
            input.version,
            input.categoryVersionn,
        );
    }
}

export type MappingInit = Omit<MappingFromServer, 'id' | 'rootObject' | 'version'> & {
    rootObjectKey: Key;
};

export type MappingInfoFromServer = {
    id: Id;
    kindName: string;
    version: VersionId;
    categoryVersion: VersionId;
};

export class MappingInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly kindName: string,
        public readonly version: VersionId,
        public readonly categoryVersion: VersionId,
    ) {}

    static fromServer(input: MappingInfoFromServer): MappingInfo {
        return new MappingInfo(
            input.id,
            input.kindName,
            input.version,
            input.categoryVersion,
        );
    }
}
