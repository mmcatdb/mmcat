import { RootProperty, type RootPropertyFromServer } from '@/types/accessPath';
import type { Entity, Id, VersionId } from './id';
import { Key, SignatureId, type KeyFromServer, type SignatureIdFromServer } from './identifiers';

export type MappingFromServer = {
    id: Id;
    categoryId: Id;
    datasourceId: Id;
    rootObjectKey: KeyFromServer;
    primaryKey: SignatureIdFromServer;
    kindName: string;
    accessPath: RootPropertyFromServer;
    version: VersionId;
};

export class Mapping implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly kindName: string,
        public readonly categoryId: Id,
        public readonly datasourceId: Id,
        public readonly rootObjexKey: Key,
        public readonly primaryKey: SignatureId,
        public readonly accessPath: RootProperty,
        public readonly version: VersionId,
    ) {}

    static fromServer(input: MappingFromServer): Mapping {
        return new Mapping(
            input.id,
            input.kindName,
            input.categoryId,
            input.datasourceId,
            Key.fromServer(input.rootObjectKey),
            SignatureId.fromServer(input.primaryKey),
            RootProperty.fromServer(input.accessPath),
            input.version,
        );
    }
}

export type MappingInit = {
    categoryId: Id;
    datasourceId: Id;
    rootObjectKey: KeyFromServer;
    primaryKey: SignatureIdFromServer;
    kindName: string;
    accessPath: RootPropertyFromServer;
};

export type MappingInfoFromServer = {
    id: Id;
    kindName: string;
    version: VersionId;
};

export class MappingInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly kindName: string,
        public readonly version: VersionId,
    ) {}

    static fromServer(input: MappingInfoFromServer): MappingInfo {
        return new MappingInfo(
            input.id,
            input.kindName,
            input.version,
        );
    }
}
